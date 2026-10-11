package com.makarios.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kept")
data class KeptEntity(
    @PrimaryKey
    val id: String,
    val declaration: String,
    val verseText: String,
    val reference: String,
    val lightName: String,
    val styleId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val sourceType: String = "authored" // "authored", "bank", or "bible"
)
