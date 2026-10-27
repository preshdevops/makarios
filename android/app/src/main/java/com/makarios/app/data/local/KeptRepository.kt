package com.makarios.app.data.local

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.makarios.app.MakariosApp
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber

object KeptRepository {
    /** No-op backwards compatibility stub. DB is now acquired lazily via MakariosApp.instance. */
    fun init(context: android.content.Context? = null) {}
    /**
     * Lazily resolved DAO — obtains the DB from the application-level singleton so that
     * widgets, receivers, and workers never trigger "not initialized" regardless of whether
     * Application.onCreate has run any explicit init.  MakariosApp.instance is assigned as
     * the very first line inside MakariosApp.onCreate, before any composition or background
     * work can start.
     */
    private fun dao(): KeptDao = resolvedDao()

    // Test seam — inject a fake DB in unit tests.
    private var testDatabase: AppDatabase? = null
    fun setDatabaseForTesting(db: AppDatabase?) { testDatabase = db }

    private fun resolvedDao(): KeptDao =
        testDatabase?.keptDao()
            ?: AppDatabase.getInstance(MakariosApp.instance).keptDao()

    private fun accountUid(): String? = runCatching {
        FirebaseAuth.getInstance().currentUser?.takeIf { !it.isAnonymous }?.uid
    }.getOrNull()

    fun getAllFlow(): Flow<List<KeptEntity>> = resolvedDao().getAllFlow()
        .catch { Timber.e(it, "KeptRepository.getAllFlow error"); emit(emptyList()) }

    fun getMineFlow(): Flow<List<KeptEntity>> = resolvedDao().getMineFlow()
        .catch { Timber.e(it, "KeptRepository.getMineFlow error"); emit(emptyList()) }

    fun getDeclarationsFlow(): Flow<List<KeptEntity>> = resolvedDao().getDeclarationsFlow()
        .catch { Timber.e(it, "KeptRepository.getDeclarationsFlow error"); emit(emptyList()) }

    fun getAllIdsFlow(): Flow<List<String>> = resolvedDao().getAllIdsFlow()
        .catch { Timber.e(it, "KeptRepository.getAllIdsFlow error"); emit(emptyList()) }

    suspend fun save(entity: KeptEntity) {
        val uid = accountUid()
        val row = entity.copy(updatedAt = System.currentTimeMillis(), syncState = if (uid == null) "LOCAL" else "PENDING")
        resolvedDao().insert(row)
        if (uid != null) backup(row, uid)
    }

    suspend fun getById(id: String): KeptEntity? = resolvedDao().getById(id)
    suspend fun applyRemote(entity: KeptEntity) = resolvedDao().insert(entity.copy(syncState = "SYNCED"))
    suspend fun getAllList(): List<KeptEntity> = resolvedDao().getAllList()
    suspend fun getAllIncludingDeleted(): List<KeptEntity> = resolvedDao().getAllIncludingDeleted()

    suspend fun updateFavorite(id: String, isFavorite: Boolean) {
        val row = getById(id) ?: return
        save(row.copy(isFavorite = isFavorite))
    }

    suspend fun updateStyleMemory(id: String, styleId: String, lightName: String, format: String, styleOptionsJson: String = "{}") {
        resolvedDao().updateStyleMemory(id, styleId, lightName, format, styleOptionsJson, System.currentTimeMillis())
    }

    suspend fun setInWidget(id: String, enabled: Boolean) = resolvedDao().setInWidget(id, enabled)
    suspend fun setInReminders(id: String, enabled: Boolean) = resolvedDao().setInReminders(id, enabled)
    suspend fun getWidgetFlagged(): List<KeptEntity> = resolvedDao().getWidgetFlagged()
    suspend fun getReminderFlagged(): List<KeptEntity> = resolvedDao().getReminderFlagged()
    suspend fun getAllOrderedByWidget(): List<KeptEntity> = resolvedDao().getAllOrderedByWidget()
    suspend fun getAllOrderedByReminders(): List<KeptEntity> = resolvedDao().getAllOrderedByReminders()
    suspend fun getMineOrderedByReminders(): List<KeptEntity> = resolvedDao().getMineOrderedByReminders()

    suspend fun softDelete(id: String, at: Long = System.currentTimeMillis()) {
        val uid = accountUid()
        resolvedDao().softDelete(id, at, if (uid == null) "LOCAL" else "PENDING")
        if (uid != null) getById(id)?.let { backup(it, uid) }
    }

    suspend fun restore(id: String, at: Long = System.currentTimeMillis()) {
        val uid = accountUid()
        resolvedDao().restore(id, at, if (uid == null) "LOCAL" else "PENDING")
        if (uid != null) getById(id)?.let { backup(it, uid) }
    }

    suspend fun activeCount(): Int = resolvedDao().activeCount()
    suspend fun deletedCount(): Int = resolvedDao().deletedCount()
    suspend fun pendingCount(): Int = resolvedDao().pendingCount()

    private fun backup(row: KeptEntity, uid: String) {
        val collection = if (row.sourceType == "authored") "declarations" else "kept"
        val payload = mutableMapOf<String, Any?>(
            "id" to row.id,
            "declaration" to row.declaration,
            "shortText" to row.declaration.take(200),
            "scriptureText" to row.verseText,
            "reference" to row.reference,
            "isFavorite" to row.isFavorite,
            "createdAt" to row.createdAt,
            "updatedAt" to row.updatedAt,
            "lightName" to row.lightName,
            "styleId" to row.styleId,
            "deletedAt" to row.deletedAt,
            "syncState" to row.syncState,
            "visibility" to row.visibility,
            "publicId" to row.publicId,
            "sourceType" to row.sourceType,
            "inWidget" to row.inWidget,
            "inReminders" to row.inReminders,
            "userId" to uid
        )
        if (collection == "kept") payload["kept"] = true
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .collection(collection).document(row.id).set(payload, SetOptions.merge())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    if (accountUid() == uid) resolvedDao().setSyncState(row.id, "SYNCED")
                }
            }
    }
}

fun KeptEntity.toAffirmation(): Affirmation = Affirmation(
    id = id,
    declaration = declaration,
    scriptureText = verseText,
    reference = reference,
    context = "",
    category = if (sourceType == "authored") "My declarations" else "Declarations",
    tone = AffirmationTone.RESOLUTE,
    imageUrl = "",
    isFavorite = isFavorite,
    personalDeclaration = declaration
)
