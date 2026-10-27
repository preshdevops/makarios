package com.makarios.app.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.makarios.app.ui.wallpaper.ShareSettings
import com.makarios.app.util.ReminderManager
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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


            val now = System.currentTimeMillis()
            // 1. Fetch remote declarations to handle merging
            onProgress(0.30f, "Checking cloud declarations...")
            val remoteDeclSnap = declarationsCol.get().await()
            val remoteDeclMap = mutableMapOf<String, Map<String, Any?>>()
            for (doc in remoteDeclSnap.documents) {
                remoteDeclMap[doc.id] = doc.data.orEmpty()
            }

            // Sync remote declarations down to local repository if missing
            for ((id, data) in remoteDeclMap) {
                val existsLocally = KeptRepository.getById(id)
                val remoteUpdatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                if (existsLocally == null || remoteUpdatedAt > existsLocally.updatedAt) {
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
                    KeptRepository.applyRemote(KeptEntity(id, item.declaration, item.scriptureText, item.reference, data["lightName"] as? String ?: "Dawn", data["styleId"] as? String ?: "", createdAt = (data["createdAt"] as? Number)?.toLong() ?: now, isFavorite = data["isFavorite"] as? Boolean ?: true, updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: now, deletedAt = (data["deletedAt"] as? Number)?.toLong(), sourceType = "authored", visibility = data["visibility"] as? String ?: "PRIVATE", publicId = data["publicId"] as? String, syncState = "SYNCED"))
                }
            }

            // 2. Upload local personal declarations to Firestore
            onProgress(0.55f, "Backing up personal declarations...")
            val localDeclarations = KeptRepository.getMineFlow().firstOrNull() ?: emptyList()
            for (decl in localDeclarations) {
                val remoteData = remoteDeclMap[decl.id]
                val remoteTimestamp = (remoteData?.get("updatedAt") as? Number)?.toLong() ?: 0L

                // If local is newer or not yet remote, upload
                if (decl.updatedAt >= remoteTimestamp) {
                    val payload = hashMapOf(
                        "id" to decl.id,
                        "declaration" to decl.declaration,
                        
                        "scriptureText" to decl.verseText,
                        "reference" to decl.reference,
                        
                        
                        
                        
                        "updatedAt" to decl.updatedAt
                    )
                    declarationsCol.document(decl.id).set(payload, SetOptions.merge()).await()
                }
            }

            // 3. Kept items migration
            onProgress(0.75f, "Syncing saved declarations...")
            val remoteKeptSnap = keptCol.get().await()
            val remoteKeptIds = remoteKeptSnap.documents.map { it.id }.toSet()
            val localKept = KeptRepository.getAllIncludingDeleted()

            val localIds = localKept.map { it.id }.toSet()
            for (row in localKept) {
                val remoteData = remoteKeptSnap.documents.firstOrNull { it.id == row.id }?.data.orEmpty()
                val remoteTimestamp = (remoteData["updatedAt"] as? Number)?.toLong() ?: 0L
                if (row.updatedAt >= remoteTimestamp) {
                    keptCol.document(row.id).set(mapOf("id" to row.id, "declaration" to row.declaration, "shortText" to row.declaration.take(200), "scriptureText" to row.verseText, "reference" to row.reference, "isFavorite" to row.isFavorite, "kept" to true, "createdAt" to row.createdAt, "updatedAt" to row.updatedAt, "userId" to uid, "lightName" to row.lightName, "styleId" to row.styleId, "deletedAt" to row.deletedAt, "syncState" to "PENDING", "visibility" to row.visibility, "publicId" to row.publicId, "sourceType" to row.sourceType, "inWidget" to row.inWidget, "inReminders" to row.inReminders), SetOptions.merge()).await()
                }
            }
            for (doc in remoteKeptSnap.documents) {
                val data = doc.data.orEmpty()
                val remoteUpdatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                val local = KeptRepository.getById(doc.id)
                if (local == null || remoteUpdatedAt > local.updatedAt) {
                    KeptRepository.applyRemote(KeptEntity(
                        id = doc.id,
                        declaration = data["declaration"] as? String ?: "",
                        verseText = data["scriptureText"] as? String ?: "",
                        reference = data["reference"] as? String ?: "",
                        lightName = data["lightName"] as? String ?: "Dawn",
                        styleId = data["styleId"] as? String ?: "",
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: now,
                        isFavorite = data["isFavorite"] as? Boolean ?: false,
                        sourceType = data["sourceType"] as? String ?: "curated",
                        inWidget = data["inWidget"] as? Boolean ?: false,
                        inReminders = data["inReminders"] as? Boolean ?: false,
                        updatedAt = remoteUpdatedAt,
                        deletedAt = (data["deletedAt"] as? Number)?.toLong(),
                        visibility = data["visibility"] as? String ?: "PRIVATE",
                        publicId = data["publicId"] as? String,
                        syncState = "SYNCED"
                    ))
                }
            }            // 4. Preferences migration
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





