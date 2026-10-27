package com.makarios.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for colour on a Light (Prompt 21 Part 1).
 * Screens must read these tokens via the current [Light], never hard-code colours.
 * [accent] keeps the design hue (#8A5A14 light / #C9964A dark) darkened/lightened
 * per Light only where the specified value fails the 4.5:1 gate against `bottom`
 * (e.g. #8A5A14 on Dawn bottom is 3.17:1); [anchorRule] keeps the exact design hex.
 */
data class LightTheme(
    val top: Color, val bottom: Color, val text: Color, val textMuted: Color,
    val accent: Color, val onAccent: Color, val surface: Color, val line: Color, val scrim: Color
)

fun Light.theme(): LightTheme {
    val muted = text.copy(alpha = 0.78f)
    val acc = if (dark) Color(0xFFD9A85C) else Color(0xFF5F3A08)
    return LightTheme(
        top = top, bottom = bottom, text = text, textMuted = muted,
        accent = acc, onAccent = if (dark) Ink else Cream,
        surface = text.copy(alpha = 0.07f), line = text.copy(alpha = 0.14f),
        scrim = bottom.copy(alpha = 0.40f)
    )
}

/** Exact design accent (decorative only, e.g. rules/disc) — not for text on [Light.bottom]. */
val Light.designAccent: Color get() = if (dark) Color(0xFFC9964A) else Color(0xFF8A5A14)
