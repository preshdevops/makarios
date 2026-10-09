package com.makarios.app

import com.makarios.app.ui.theme.AnchorDark
import com.makarios.app.ui.theme.AnchorLight
import com.makarios.app.ui.theme.Cream
import com.makarios.app.ui.theme.Ink
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
        assertEquals(AnchorLight, Light.Dawn.anchorRule)
        // Dark anchor rule #C9964A
        assertEquals(AnchorDark, Light.Night.anchorRule)
    }

    @Test
    fun testLightButtonColors() {
        // Light themes get Ink fill
        assertEquals(Ink, Light.Dawn.buttonFill)
        // Dark themes get Cream fill
        assertEquals(Cream, Light.Night.buttonFill)
    }
}
