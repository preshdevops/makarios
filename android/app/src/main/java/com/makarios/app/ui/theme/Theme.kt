package com.makarios.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Olive,
    onPrimary = Surface,
    primaryContainer = OliveLight,
    onPrimaryContainer = Olive,
    secondary = Olive,
    onSecondary = Surface,
    secondaryContainer = OliveLight,
    onSecondaryContainer = Olive,
    tertiary = Sage,
    onTertiary = Surface,
    tertiaryContainer = LiveFeedGreenBg,
    onTertiaryContainer = Sage,
    background = Porcelain,
    onBackground = Espresso,
    surface = Surface,
    onSurface = Espresso,
    surfaceVariant = PorcelainWarm,
    onSurfaceVariant = Espresso,
    outline = BorderSubtle
)

@Composable
fun MakariosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Porcelain.toArgb()
            window.navigationBarColor = Surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MakariosTypography,
        content = content
    )
}
