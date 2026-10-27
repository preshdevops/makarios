package com.makarios.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KeptDao {
    @Query("SELECT * FROM kept WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<KeptEntity>>

    @Query("SELECT * FROM kept WHERE sourceType = 'authored' AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getMineFlow(): Flow<List<KeptEntity>>

    @Query("SELECT * FROM kept WHERE sourceType != 'authored' AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getDeclarationsFlow(): Flow<List<KeptEntity>>

    @Query("SELECT * FROM kept WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): KeptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KeptEntity)

    @Query("UPDATE kept SET isFavorite = :isFavorite, updatedAt = :at WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, at: Long)

    @Query("UPDATE kept SET styleId = :styleId, lightName = :lightName, format = :format, styleOptionsJson = :optionsJson, updatedAt = :at WHERE id = :id")
    suspend fun updateStyleMemory(id: String, styleId: String, lightName: String, format: String, optionsJson: String, at: Long)

    @Query("SELECT * FROM kept WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    suspend fun getAllList(): List<KeptEntity>

    @Query("SELECT * FROM kept ORDER BY createdAt DESC")
    suspend fun getAllIncludingDeleted(): List<KeptEntity>

    @Query("SELECT id FROM kept WHERE deletedAt IS NULL")
    fun getAllIdsFlow(): Flow<List<String>>

    @Query("UPDATE kept SET inWidget = :enabled WHERE id = :id")
    suspend fun setInWidget(id: String, enabled: Boolean)

    @Query("UPDATE kept SET inReminders = :enabled WHERE id = :id")
    suspend fun setInReminders(id: String, enabled: Boolean)

    @Query("SELECT * FROM kept WHERE deletedAt IS NULL ORDER BY inWidget DESC, createdAt DESC")
    suspend fun getAllOrderedByWidget(): List<KeptEntity>

    @Query("SELECT * FROM kept WHERE deletedAt IS NULL ORDER BY inReminders DESC, createdAt DESC")
    suspend fun getAllOrderedByReminders(): List<KeptEntity>

    @Query("SELECT * FROM kept WHERE inWidget = 1 AND deletedAt IS NULL ORDER BY createdAt DESC")
    suspend fun getWidgetFlagged(): List<KeptEntity>

    @Query("SELECT * FROM kept WHERE inReminders = 1 AND deletedAt IS NULL ORDER BY createdAt DESC")
    suspend fun getReminderFlagged(): List<KeptEntity>

    @Query("SELECT * FROM kept WHERE sourceType = 'authored' AND deletedAt IS NULL ORDER BY inReminders DESC, createdAt DESC")
    suspend fun getMineOrderedByReminders(): List<KeptEntity>

    @Query("UPDATE kept SET syncState = :state WHERE id = :id")
    suspend fun setSyncState(id: String, state: String)

    @Query("UPDATE kept SET deletedAt = :at, updatedAt = :at, syncState = :state WHERE id = :id")
    suspend fun softDelete(id: String, at: Long, state: String)

    @Query("UPDATE kept SET deletedAt = NULL, updatedAt = :at, syncState = :state WHERE id = :id")
    suspend fun restore(id: String, at: Long, state: String)

    @Query("SELECT COUNT(*) FROM kept WHERE deletedAt IS NULL")
    suspend fun activeCount(): Int

    @Query("SELECT COUNT(*) FROM kept WHERE deletedAt IS NOT NULL")
    suspend fun deletedCount(): Int

    @Query("SELECT COUNT(*) FROM kept WHERE syncState = 'PENDING' AND deletedAt IS NULL")
    suspend fun pendingCount(): Int
}


