package com.makarios.app

import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.renderLightBitmap
import org.junit.Assert.assertEquals
import org.junit.Test

/** Instrumented screenshot fixtures: each case renders at the canonical 1080x1920 pixel size. */
class LightScreenshotTest {
    @Test fun dawn()=assertSize(Light.Dawn)
    @Test fun midday()=assertSize(Light.Midday)
    @Test fun mist()=assertSize(Light.Mist)
    @Test fun rain()=assertSize(Light.Rain)
    @Test fun ember()=assertSize(Light.Ember)
    @Test fun dusk()=assertSize(Light.Dusk)
    @Test fun grove()=assertSize(Light.Grove)
    @Test fun night()=assertSize(Light.Night)
    private fun assertSize(light:Light){val bitmap=renderLightBitmap(light,1080,1920);assertEquals(1080,bitmap.width);assertEquals(1920,bitmap.height);bitmap.recycle()}
}
