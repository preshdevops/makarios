package com.makarios.app.data

enum class AffirmationTone { STILL, RESOLUTE, GENTLE }

data class Affirmation(
    val id: String, val declaration: String,
    val shortText: String = declaration.toWidgetShortText(),
    val scriptureText: String, val reference: String, val context: String = "", val category: String = "",
    val tone: AffirmationTone = AffirmationTone.RESOLUTE, val imageUrl: String = "", val isFavorite: Boolean = false,
    val personalDeclaration: String? = null
)

val WIDGET_UI_LABELS = setOf("Short version for widgets", "Declaration", "Verse")

/** Real content only: shortText, else first sentence, else first 60 chars. Never a UI label. */
fun String.toWidgetShortText(): String {
    val clean = trim()
    if (clean.isEmpty() || clean in WIDGET_UI_LABELS) return ""
    if (clean.length <= 60) return clean
    val sentence = split(Regex("(?<=[.!?])\\s+"), limit = 2).firstOrNull()?.trim().orEmpty()
    if (sentence.isNotEmpty() && sentence.length <= 60 && sentence !in WIDGET_UI_LABELS) return sentence
    return clean.take(60).trimEnd()
}

fun widgetTextFor(declaration: String, shortText: String): String {
    val candidate = shortText.trim().takeIf { it.isNotEmpty() && it !in WIDGET_UI_LABELS } ?: declaration.toWidgetShortText()
    return candidate.ifEmpty { declaration.trim().take(60) }
}
