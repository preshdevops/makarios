package com.makarios.app

import com.makarios.app.ui.theme.Light
import org.junit.Assert.*
import org.junit.Test

/**
 * Validates Item A edge-to-edge requirements:
 * 1. Dark vs Light classification for status/nav bar icon appearance across all 8 Lights.
 * 2. Icon contrast >= 4.5:1 against the Light's top (status bar) and bottom (navigation bar).
 * 3. Light.isDark contract: Dawn, Midday, Mist, Rain have !isDark (dark icons); Ember, Dusk, Grove, Night have isDark (light icons).
 */
class EdgeToEdgeSystemBarsTest {

    private fun luminance(color: androidx.compose.ui.graphics.Color): Float {
        fun channel(c: Float): Float {
            return if (c <= 0.03928f) c / 12.92f else Math.pow(((c + 0.055) / 1.055).toDouble(), 2.4).toFloat()
        }
        return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
    }

    private fun contrastRatio(c1: androidx.compose.ui.graphics.Color, c2: androidx.compose.ui.graphics.Color): Float {
        val l1 = luminance(c1)
        val l2 = luminance(c2)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    @Test
    fun lightClassificationMatchesIconAppearanceRules() {
        // Light Lights get dark icons (!isDark == true)
        val lightLights = listOf(Light.Dawn, Light.Midday, Light.Mist, Light.Rain)
        lightLights.forEach { light ->
            assertFalse("${light.name} must NOT be dark", light.isDark)
            assertTrue("${light.name} must require dark status/nav icons (!isDark)", !light.isDark)
        }

        // Dark Lights get light icons (isDark == true, !isDark == false)
        val darkLights = listOf(Light.Ember, Light.Dusk, Light.Grove, Light.Night)
        darkLights.forEach { light ->
            assertTrue("${light.name} must be dark", light.isDark)
            assertFalse("${light.name} must require light status/nav icons", !light.isDark)
        }
    }

    @Test
    fun systemBarIconsPassContrastGateOnEveryLight() {
        val darkIcon = com.makarios.app.ui.theme.Ink
        val lightIcon = com.makarios.app.ui.theme.Cream

        Light.values().forEach { light ->
            val iconColor = if (light.isDark) lightIcon else darkIcon

            val topContrast = contrastRatio(iconColor, light.top)
            val botContrast = contrastRatio(iconColor, light.bottom)

            assertTrue(
                "Status bar icon contrast on ${light.name} top (${topContrast}:1) must be >= 4.5:1",
                topContrast >= 4.5f
            )
            assertTrue(
                "Nav bar icon contrast on ${light.name} bottom (${botContrast}:1) must be >= 4.5:1",
                botContrast >= 4.5f
            )
        }
    }
}
