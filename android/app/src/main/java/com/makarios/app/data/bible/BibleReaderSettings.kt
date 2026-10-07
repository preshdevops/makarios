package com.makarios.app.data.bible

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.makarios.app.ui.theme.Espresso
import com.makarios.app.ui.theme.Porcelain
import com.makarios.app.ui.theme.Stone

enum class ReaderTheme(
    val title: String,
    val background: Color,
    val text: Color,
    val verseNumber: Color,
    val highlightColor: Color
) {
    LIGHT(
        "Light",
        Porcelain,
        Espresso,
        Stone,
        Color(0xFFF3E7C4)
    ),
    SEPIA(
        "Sepia",
        Color(0xFFF4ECD8),
        Color(0xFF4B3B2A),
        Color(0xFF7A664F),
        Color(0xFFE8D7B0)
    ),
    DARK(
        "Dark",
        Color(0xFF1C1B19),
        Color(0xFFE8E2D6),
        Color(0xFF9E988D),
        Color(0xFF3D3728)
    )
}

object BibleReaderPrefs {
    private const val PREFS_NAME = "makarios_bible"
    private const val KEY_FONT_SIZE = "reader_font_size"
    private const val KEY_THEME = "reader_theme"
    private const val KEY_SHOW_NUMBERS = "reader_show_numbers"
    private const val KEY_HIGHLIGHTS = "reader_highlights"

    fun getFontSize(context: Context): Float {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_FONT_SIZE, 18f)
    }

    fun setFontSize(context: Context, size: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_FONT_SIZE, size).apply()
    }

    fun getTheme(context: Context): ReaderTheme {
        val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, ReaderTheme.LIGHT.name)
        return try {
            ReaderTheme.valueOf(name ?: ReaderTheme.LIGHT.name)
        } catch (_: Exception) {
            ReaderTheme.LIGHT
        }
    }

    fun setTheme(context: Context, theme: ReaderTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_THEME, theme.name).apply()
    }

    fun getShowVerseNumbers(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW_NUMBERS, true)
    }

    fun setShowVerseNumbers(context: Context, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SHOW_NUMBERS, show).apply()
    }

    fun getHighlights(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_HIGHLIGHTS, emptySet()) ?: emptySet()
    }

    fun toggleHighlight(context: Context, reference: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_HIGHLIGHTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val isNowHighlighted = if (current.contains(reference)) {
            current.remove(reference)
            false
        } else {
            current.add(reference)
            true
        }
        prefs.edit().putStringSet(KEY_HIGHLIGHTS, current).apply()
        return isNowHighlighted
    }
}
