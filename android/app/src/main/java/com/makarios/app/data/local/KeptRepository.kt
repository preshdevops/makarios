package com.makarios.app.data.local

import android.content.Context
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationTone
import kotlinx.coroutines.flow.Flow

object KeptRepository {
    private var database: AppDatabase? = null

    fun init(context: Context) {
        if (database == null) {
            database = AppDatabase.getInstance(context.applicationContext)
        }
    }

    private fun dao(): KeptDao {
        return database?.keptDao() ?: error("KeptRepository not initialized. Call init(context) in Application.onCreate")
    }

    fun getAllFlow(): Flow<List<KeptEntity>> = dao().getAllFlow()
    fun getMineFlow(): Flow<List<KeptEntity>> = dao().getMineFlow()
    fun getDeclarationsFlow(): Flow<List<KeptEntity>> = dao().getDeclarationsFlow()
    fun getAllIdsFlow(): Flow<List<String>> = dao().getAllIdsFlow()

    suspend fun save(entity: KeptEntity) {
        dao().insert(entity)
    }

    suspend fun delete(entity: KeptEntity) {
        dao().delete(entity)
    }

    suspend fun deleteById(id: String) {
        dao().deleteById(id)
    }

    suspend fun getById(id: String): KeptEntity? {
        return dao().getById(id)
    }

    suspend fun updateFavorite(id: String, isFavorite: Boolean) {
        dao().updateFavorite(id, isFavorite)
    }

    suspend fun getAllList(): List<KeptEntity> = dao().getAllList()
}

fun KeptEntity.toAffirmation(): Affirmation {
    return Affirmation(
        id = id,
        declaration = declaration,
        scriptureText = verseText,
        reference = reference,
        context = "",
        category = if (sourceType == "authored") "Mine" else "Declarations",
        tone = AffirmationTone.RESOLUTE,
        imageUrl = "",
        isFavorite = isFavorite,
        personalDeclaration = declaration
    )
}
