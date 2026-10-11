package com.makarios.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KeptDao {
    @Query("SELECT * FROM kept ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<KeptEntity>>

    @Query("SELECT * FROM kept WHERE sourceType = 'authored' ORDER BY createdAt DESC")
    fun getMineFlow(): Flow<List<KeptEntity>>

    @Query("SELECT * FROM kept WHERE sourceType != 'authored' ORDER BY createdAt DESC")
    fun getDeclarationsFlow(): Flow<List<KeptEntity>>

    @Query("SELECT * FROM kept WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): KeptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KeptEntity)

    @Delete
    suspend fun delete(entity: KeptEntity)

    @Query("DELETE FROM kept WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE kept SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT * FROM kept ORDER BY createdAt DESC")
    suspend fun getAllList(): List<KeptEntity>

    @Query("SELECT id FROM kept")
    fun getAllIdsFlow(): Flow<List<String>>
}
