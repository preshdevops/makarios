package com.makarios.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLight = staticCompositionLocalOf { Light.forNow() }
val LocalSetLight = staticCompositionLocalOf<(Light?) -> Unit> { {} }

/**
 * CompositionLocal exposing the CURRENT screen's active Light.
 * Nav icon and label colours read from this local.
 */
val LocalActiveLight = compositionLocalOf { Light.forNow() }

/**
 * Internal listener so screens can report their active Light up to the nav bar.
 */
val LocalActiveLightUpdater = staticCompositionLocalOf<((Light, String) -> Unit)?> { null }

@Composable
fun ProvideUserLight(light: Light, setLight: (Light?) -> Unit, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLight provides light, LocalSetLight provides setLight, content = content)
}

/**
 * Sets the active light for this screen, notifying the outer nav bar and exposing LocalActiveLight.
 */
@Composable
fun ProvideActiveLight(light: Light, routeName: String = "", content: @Composable () -> Unit = {}) {
    val updater = LocalActiveLightUpdater.current
    LaunchedEffect(light, routeName) {
        updater?.invoke(light, routeName)
    }
    CompositionLocalProvider(LocalActiveLight provides light, content = content)
}
