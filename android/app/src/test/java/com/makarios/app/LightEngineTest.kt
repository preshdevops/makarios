package com.makarios.app

import com.makarios.app.ui.theme.Light
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class LightEngineTest {
    @Test fun allEightLightsHaveStableTokens() { assertEquals(8, Light.values().size); Light.values().forEach { assertTrue(it.discR > 0f); assertTrue(it.discAlpha in 0f..1f) } }
    @Test fun selectionUsesInclusiveBoundaries() {
        val zone=ZoneId.of("UTC")
        fun at(h:Int,m:Int)=Light.forNow(Clock.fixed(Instant.parse("2026-01-01T%02d:%02d:00Z".format(h,m)),zone))
        assertEquals(Light.Dawn,at(5,30)); assertEquals(Light.Midday,at(9,0)); assertEquals(Light.Dusk,at(16,30)); assertEquals(Light.Night,at(19,30)); assertEquals(Light.Night,at(5,29))
    }
    @Test fun topicMappingIsCanonical() { assertEquals(Light.Dawn,Light.forTopic("Identity"));assertEquals(Light.Mist,Light.forTopic("peace"));assertEquals(Light.Ember,Light.forTopic("Strength"));assertEquals(Light.Rain,Light.forTopic("purpose"));assertEquals(Light.Dusk,Light.forTopic("courage"));assertEquals(Light.Midday,Light.forTopic("joy"));assertEquals(Light.Grove,Light.forTopic("rest")) }
}
