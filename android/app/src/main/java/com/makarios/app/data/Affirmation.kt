package com.makarios.app.data

enum class AffirmationTone {
    STILL,
    RESOLUTE,
    GENTLE
}

data class Affirmation(
    val id: String,
    val declaration: String,
    val shortText: String = declaration.toWidgetShortText(),
    val scriptureText: String,
    val reference: String,
    val context: String,
    val category: String,
    val tone: AffirmationTone,
    val imageUrl: String,
    val isFavorite: Boolean = false,
    val personalDeclaration: String? = null
)

fun String.toWidgetShortText(): String {
    if (length <= 40) return this
    val sentence = split(Regex("(?<=[.!?])\\s+"), limit = 2).firstOrNull()?.trim().orEmpty()
    if (sentence.isNotEmpty() && sentence.length <= 40) return sentence
    return "Short version for widgets"
}

