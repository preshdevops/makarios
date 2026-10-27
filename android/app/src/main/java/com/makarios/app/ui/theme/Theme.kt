package com.makarios.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Maps EVERY Material role from the Light — nothing falls back to Material purple (Prompt 21 Part 1.3). */
fun lightColorSchemeFrom(theme: LightTheme, dark: Boolean) = if (dark) darkColorScheme(
    primary = theme.text, onPrimary = theme.bottom, secondary = theme.accent, onSecondary = theme.onAccent,
    secondaryContainer = theme.surface, onSecondaryContainer = theme.text,
    tertiary = theme.accent, onTertiary = theme.onAccent, background = theme.bottom, onBackground = theme.text,
    surface = theme.top, onSurface = theme.text, surfaceVariant = theme.surface, onSurfaceVariant = theme.textMuted,
    outline = theme.line, outlineVariant = theme.line, error = Color(0xFFBA1A1A), onError = theme.onAccent,
    surfaceTint = theme.surface, inverseSurface = theme.text, inverseOnSurface = theme.bottom, inversePrimary = theme.accent
) else lightColorScheme(
    primary = theme.text, onPrimary = theme.bottom, secondary = theme.accent, onSecondary = theme.onAccent,
    secondaryContainer = theme.surface, onSecondaryContainer = theme.text,
    tertiary = theme.accent, onTertiary = theme.onAccent, background = theme.bottom, onBackground = theme.text,
    surface = theme.top, onSurface = theme.text, surfaceVariant = theme.surface, onSurfaceVariant = theme.textMuted,
    outline = theme.line, outlineVariant = theme.line, error = Color(0xFFBA1A1A), onError = theme.onAccent,
    surfaceTint = theme.surface, inverseSurface = theme.text, inverseOnSurface = theme.bottom, inversePrimary = theme.accent
)

@Composable
fun MakariosTheme(light: Light = Light.forNow(), content: @Composable () -> Unit) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = lightColorSchemeFrom(light.theme(), light.dark),
        typography = MakariosTypography, content = content
    )
}
