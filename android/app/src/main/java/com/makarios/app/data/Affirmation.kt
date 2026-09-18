package com.makarios.app.data

enum class AffirmationTone {
    STILL,
    RESOLUTE,
    GENTLE
}

data class Affirmation(
    val id: String,
    val declaration: String,
    val scriptureText: String,
    val reference: String,
    val context: String,
    val category: String,
    val tone: AffirmationTone,
    val imageUrl: String,
    val isFavorite: Boolean = false
)
