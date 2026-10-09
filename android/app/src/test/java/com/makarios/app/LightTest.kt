package com.makarios.app

import com.makarios.app.ui.theme.Light
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LightTest {
    @Test
    fun testLightIsLightProperty() {
        assertTrue(Light.Dawn.isLight)
        assertTrue(Light.Midday.isLight)
        assertTrue(Light.Mist.isLight)
        assertTrue(Light.Rain.isLight)
        assertTrue(!Light.Ember.isLight)
        assertTrue(!Light.Dusk.isLight)
        assertTrue(!Light.Grove.isLight)
        assertTrue(!Light.Night.isLight)
    }

    @Test
    fun testLightAnchorColors() {
        // Light anchor rule #8A5A14
        assertEquals(0xFF8A5A14.toInt(), androidx.compose.ui.graphics.toArgb(Light.Dawn.anchorRule))
        // Dark anchor rule #C9964A
        assertEquals(0xFFC9964A.toInt(), androidx.compose.ui.graphics.toArgb(Light.Night.anchorRule))
    }

    @Test
    fun testLightButtonColors() {
        // Light themes get Ink fill
        assertEquals(0xFF2A1B14.toInt(), androidx.compose.ui.graphics.toArgb(Light.Dawn.buttonFill))
        // Dark themes get Cream fill
        assertEquals(0xFFFFF4E4.toInt(), androidx.compose.ui.graphics.toArgb(Light.Night.buttonFill))
    }
}
