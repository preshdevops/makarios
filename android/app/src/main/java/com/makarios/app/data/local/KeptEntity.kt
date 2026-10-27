package com.makarios.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kept")
data class KeptEntity(
    @PrimaryKey val id: String,
    val declaration: String,
    val verseText: String,
    val reference: String,
    val lightName: String,
    val styleId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val sourceType: String = "authored",
    val inWidget: Boolean = false,
    val inReminders: Boolean = false,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = createdAt,
    val deletedAt: Long? = null,
    @ColumnInfo(defaultValue = "'LOCAL'") val syncState: String = "LOCAL",
    @ColumnInfo(defaultValue = "'PRIVATE'") val visibility: String = "PRIVATE",
    val publicId: String? = null,
    @ColumnInfo(defaultValue = "'Story'") val format: String = "Story",
    @ColumnInfo(defaultValue = "'{}'") val styleOptionsJson: String = "{}"
)
