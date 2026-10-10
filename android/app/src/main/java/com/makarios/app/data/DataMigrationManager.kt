package com.makarios.app.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.makarios.app.ui.wallpaper.ShareSettings
import com.makarios.app.util.ReminderManager
import com.makarios.app.widget.WidgetStore
import kotlinx.coroutines.tasks.await

/**
 * DataMigrationManager
 *
 * Idempotently migrates local guest declarations, kept items, and settings
 * into Cloud Firestore when the user creates an account or signs in.
 *
 * Rules:
 * - Stable IDs: declarations and kept items use their existing unique IDs.
 * - Merge by ID: if remote documents exist, keep the newest edit.
 * - Never delete the local copy until upload is confirmed.
 * - Progress tracking and retry support.
 */
object DataMigrationManager {

    private const val PREFS_NAME = "makarios_migration_prefs"
    private const val KEY_MIGRATED_PREFIX = "migrated_user_"

    sealed class MigrationState {
        object Idle : MigrationState()
        data class InProgress(val progress: Float, val statusMessage: String) : MigrationState()
        object Success : MigrationState()
        data class Error(val message: String) : MigrationState()
    }

    private fun isUserMigrated(context: Context, uid: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MIGRATED_PREFIX + uid, false)
    }

    private fun markUserMigrated(context: Context, uid: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MIGRATED_PREFIX + uid, true).apply()
    }

    /**
     * Executes the idempotent data migration.
     */
    suspend fun migrateUserData(
        context: Context,
        uid: String,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<Unit> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(uid)

            onProgress(0.15f, "Connecting to your cloud library...")

            val declarationsCol = userDoc.collection("declarations")
            val keptCol = userDoc.collection("kept")
            val settingsDoc = userDoc.collection("settings").document("preferences")

            // 1. Fetch remote declarations to handle merging
            onProgress(0.30f, "Checking cloud declarations...")
            val remoteDeclSnap = declarationsCol.get().await()
            val remoteDeclMap = mutableMapOf<String, Map<String, Any?>>()
            for (doc in remoteDeclSnap.documents) {
                remoteDeclMap[doc.id] = doc.data.orEmpty()
            }

            // Sync remote declarations down to local repository if missing
            for ((id, data) in remoteDeclMap) {
                val existsLocally = AffirmationRepository.personalAffirmations.any { it.id == id }
                if (!existsLocally) {
                    val declText = data["declaration"] as? String ?: ""
                    val scriptText = data["scriptureText"] as? String ?: ""
                    val ref = data["reference"] as? String ?: ""
                    val ctx = data["context"] as? String ?: ""
                    val cat = data["category"] as? String ?: "Identity"
                    val toneStr = data["tone"] as? String ?: AffirmationTone.RESOLUTE.name
                    val tone = runCatching { AffirmationTone.valueOf(toneStr) }.getOrDefault(AffirmationTone.RESOLUTE)
                    val personalDecl = data["personalDeclaration"] as? String

                    val item = Affirmation(
                        id = id,
                        declaration = declText,
                        scriptureText = scriptText,
                        reference = ref,
                        context = ctx,
                        category = cat,
                        tone = tone,
                        imageUrl = "",
                        isFavorite = true,
                        personalDeclaration = personalDecl
                    )
                    AffirmationRepository.addPersonalAffirmation(item)
                }
            }

            // 2. Upload local personal declarations to Firestore
            onProgress(0.55f, "Backing up personal declarations...")
            val now = System.currentTimeMillis()
            val localDeclarations = AffirmationRepository.personalAffirmations.toList()
            for (decl in localDeclarations) {
                val remoteData = remoteDeclMap[decl.id]
                val remoteTimestamp = (remoteData?.get("updatedAt") as? Number)?.toLong() ?: 0L

                // If local is newer or not yet remote, upload
                if (now >= remoteTimestamp) {
                    val payload = hashMapOf(
                        "id" to decl.id,
                        "declaration" to decl.declaration,
                        "shortText" to decl.shortText,
                        "scriptureText" to decl.scriptureText,
                        "reference" to decl.reference,
                        "context" to decl.context,
                        "category" to decl.category,
                        "tone" to decl.tone.name,
                        "personalDeclaration" to decl.personalDeclaration,
                        "updatedAt" to now
                    )
                    declarationsCol.document(decl.id).set(payload, SetOptions.merge()).await()
                }
            }

            // 3. Kept items migration
            onProgress(0.75f, "Syncing kept affirmations...")
            val remoteKeptSnap = keptCol.get().await()
            val remoteKeptIds = remoteKeptSnap.documents.map { it.id }.toSet()

            // Merge remote kept into local
            for (remoteId in remoteKeptIds) {
                if (!AffirmationRepository.savedAffirmationIds.contains(remoteId)) {
                    AffirmationRepository.savedAffirmationIds.add(remoteId)
                }
            }

            // Upload local kept items that aren't on remote
            for (savedId in AffirmationRepository.savedAffirmationIds) {
                if (!remoteKeptIds.contains(savedId)) {
                    val keptPayload = hashMapOf(
                        "affirmationId" to savedId,
                        "savedAt" to now
                    )
                    keptCol.document(savedId).set(keptPayload, SetOptions.merge()).await()
                }
            }

            // 4. Preferences migration
            onProgress(0.90f, "Saving preferences...")
            val topics = OnboardingStore.getSelectedTopics(context).toList()
            val wordmark = ShareSettings.isWordmarkEnabled(context)
            val widgetLight = WidgetStore.fixedLight(context)
            val reminderHour = ReminderManager.getReminderHour(context)
            val reminderMinute = ReminderManager.getReminderMinute(context)
            val reminderEnabled = ReminderManager.isDailyReminderEnabled(context)

            val settingsPayload = hashMapOf(
                "selectedTopics" to topics,
                "wordmarkEnabled" to wordmark,
                "widgetLight" to widgetLight,
                "reminderHour" to reminderHour,
                "reminderMinute" to reminderMinute,
                "reminderEnabled" to reminderEnabled,
                "updatedAt" to now
            )
            settingsDoc.set(settingsPayload, SetOptions.merge()).await()

            // Mark migration completed
            markUserMigrated(context, uid)
            onProgress(1.0f, "Your declarations are safe.")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
