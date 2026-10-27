package com.makarios.app

import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.theme
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class LightContrastTest {
    private fun lum(r: Float, g: Float, b: Float): Double {
        fun f(c: Float) = if (c <= 0.03928f) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        return 0.2126 * f(r) + 0.7152 * f(g) + 0.0722 * f(b)
    }
    private fun ratio(fg: androidx.compose.ui.graphics.Color, bg: androidx.compose.ui.graphics.Color): Double {
        // composite fg over bg using fg alpha
        val r = fg.red * fg.alpha + bg.red * (1 - fg.alpha)
        val g = fg.green * fg.alpha + bg.green * (1 - fg.alpha)
        val b = fg.blue * fg.alpha + bg.blue * (1 - fg.alpha)
        val l1 = lum(r, g, b); val l2 = lum(bg.red, bg.green, bg.blue)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }
    @Test fun allLightsPassContrastGate() {
        Light.values().forEach { light ->
            val t = light.theme()
            assertTrue("${light.name} text/top ${ratio(t.text, t.top)}", ratio(t.text, t.top) >= 4.5)
            assertTrue("${light.name} text/bottom ${ratio(t.text, t.bottom)}", ratio(t.text, t.bottom) >= 4.5)
            assertTrue("${light.name} muted/bottom ${ratio(t.textMuted, t.bottom)}", ratio(t.textMuted, t.bottom) >= 4.5)
            assertTrue("${light.name} accent/bottom ${ratio(t.accent, t.bottom)}", ratio(t.accent, t.bottom) >= 4.5)
            assertTrue("${light.name} onAccent/accent ${ratio(t.onAccent, t.accent)}", ratio(t.onAccent, t.accent) >= 4.5)
        }
    }
}
