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
    primary = BrandPlum,
    onPrimary = SurfaceWhite,
    primaryContainer = BrandPlumLight,
    onPrimaryContainer = SurfaceWhite,
    secondary = SalmonTerracotta,
    onSecondary = SurfaceWhite,
    secondaryContainer = SalmonLight,
    onSecondaryContainer = SalmonTerracotta,
    tertiary = LiveFeedGreen,
    onTertiary = SurfaceWhite,
    tertiaryContainer = LiveFeedGreenBg,
    onTertiaryContainer = LiveFeedGreen,
    background = PorcelainBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = ToneResoluteSand,
    onSurfaceVariant = TextPrimary,
    outline = BorderSubtle
)

@Composable
fun MakariosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Intentional warm porcelain identity per PRODUCT.md

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PorcelainBackground.toArgb()
            window.navigationBarColor = SurfaceWhite.toArgb()
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
