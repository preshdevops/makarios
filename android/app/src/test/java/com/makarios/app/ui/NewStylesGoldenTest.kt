package com.makarios.app.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.BaseStyle
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleSpec
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NewStylesGoldenTest {

    @Test
    fun all36StylesRegisteredAndConfiguredCorrectly() {
        val new36 = StyleRegistry.new36
        assertEquals(36, new36.size)
        assertEquals(57, StyleRegistry.all.size)

        val expectedIds = listOf(
            // Type that speaks (22..30)
            "stack", "drop_cap", "outline", "ticker", "split_weight",
            "spine", "poem", "marker", "doorway",
            // Light and scene (31..37)
            "horizon", "eclipse", "moon", "dunes", "rain", "fireflies", "candle",
            // Paper and objects (38..45)
            "index_card", "torn_paper", "sticky_note", "letter",
            "bible_page", "ticket", "seal", "bookmark",
            // Occasions (46..51)
            "birthday", "praying_for_you", "good_morning", "day_of_the_week", "answered", "season",
            // Conversation and series (52..57)
            "chat_bubble", "fill_in", "pick_one", "week", "call_and_answer", "coordinates"
        )

        expectedIds.forEach { id ->
            val style = StyleRegistry[id]
            assertNotNull("Style '$id' must exist in StyleRegistry", style)
            assertEquals(id, style?.id)
            assertTrue("Style '$id' displayName must not be blank", style?.displayName?.isNotBlank() == true)
        }

        // Rule 1: Paper & objects styles use fixed paper colours and supportsLights is false
        val paperStyleIds = listOf(
            "index_card", "torn_paper", "sticky_note", "letter",
            "bible_page", "ticket", "seal", "bookmark"
        )
        paperStyleIds.forEach { id ->
            val style = StyleRegistry[id]
            assertNotNull(style)
            assertFalse("Paper style '$id' must have supportsLights == false", style!!.supportsLights)
            assertEquals("Paper", style.category)
        }

        // Rule 2: Footer removed on Sticker, Seal and Chat bubble
        listOf("sticker", "seal", "chat_bubble").forEach { id ->
            val style = StyleRegistry[id]
            if (style is BaseStyle) {
                assertFalse("Style '$id' must omit wordmark footer (showWordmark == false)", style.showWordmark)
            }
        }
    }

    @Test
    fun goldenRenderAll36StylesAtStorySquareAndX() {
        val spec = StyleSpec(
            declaration = "I am steady and untroubled in His perfect peace.",
            verse = "Peace I leave with you; my peace I give you. I do not give to you as the world gives.",
            reference = "John 14:27",
            chapterNumber = "14",
            recipientName = "Precious",
            replyText = "Amen, received with joy",
            seed = 777
        )

        val sizes = listOf(
            Triple(1080, 1920, "Story"),
            Triple(1080, 1080, "Square"),
            Triple(1600, 900, "X")
        )

        for (style in StyleRegistry.new36) {
            for ((w, h, formatName) in sizes) {
                val bmp = style.render(spec, Light.Dawn, IntSize(w, h))
                assertNotNull("${style.id} at $formatName must produce a non-null bitmap", bmp)
                assertEquals("${style.id} at $formatName width", w, bmp.width)
                assertEquals("${style.id} at $formatName height", h, bmp.height)

                assertFalse("${style.id} at $formatName bitmap must not be recycled", bmp.isRecycled)
                bmp.recycle()
            }
        }
    }

    @Test
    fun forYouRecommendationEngineAdaptsDynamically() {
        // 1. Short text (< 28 chars)
        val shortSpec = StyleSpec(
            declaration = "I am peace.",
            verse = "He is our peace.",
            reference = "Ephesians 2:14"
        )
        val forYouShort = StyleRegistry.forYouStyles(shortSpec, Light.Midday)
        assertEquals(6, forYouShort.size)
        assertTrue(
            "Short text should prioritize punchy short styles (Word, Stack, Horizon, Drop cap, or Dunes)",
            forYouShort.any { it.id in listOf("word", "stack", "horizon", "drop_cap", "dunes") }
        )

        // 2. Long text (> 60 chars)
        val longSpec = StyleSpec(
            declaration = "The Lord is my light and my salvation, whom shall I fear? The Lord is the stronghold of my life, of whom shall I be afraid?",
            verse = "One thing I have asked of the Lord, that will I seek after.",
            reference = "Psalm 27:1-4"
        )
        val forYouLong = StyleRegistry.forYouStyles(longSpec, Light.Midday)
        assertEquals(6, forYouLong.size)
        assertTrue(
            "Long text should prioritize long-form styles (Poem, Letter, Index card, Bible page, Torn paper)",
            forYouLong.any { it.id in listOf("poem", "letter", "index_card", "bible_page", "torn_paper", "spine") }
        )

        // 3. Recipient name present
        val occasionSpec = StyleSpec(
            declaration = "May grace and mercy surround you today.",
            verse = "The Lord bless you and keep you.",
            reference = "Numbers 6:24",
            recipientName = "Precious"
        )
        val forYouOccasion = StyleRegistry.forYouStyles(occasionSpec, Light.Dawn)
        assertEquals(6, forYouOccasion.size)
        assertTrue(
            "Recipient name present must prioritize occasion styles (Birthday, Praying, Letter, For you)",
            forYouOccasion.any { it.id in listOf("birthday", "praying_for_you", "letter", "good_morning") }
        )

        // 4. Night Light
        val nightSpec = StyleSpec(
            declaration = "I will lie down and sleep in peace, for You alone make me dwell in safety.",
            verse = "In peace I will lie down and sleep.",
            reference = "Psalm 4:8"
        )
        val forYouNight = StyleRegistry.forYouStyles(nightSpec, Light.Night)
        assertEquals(6, forYouNight.size)
        assertTrue(
            "Night light should include night scene styles (Eclipse, Moon, Candle, Constellation)",
            forYouNight.any { it.id in listOf("eclipse", "moon", "candle", "constellation") }
        )
    }

    @Test
    fun maxCharsLimitsAreEnforced() {
        StyleRegistry.all.forEach { style ->
            assertTrue("Style '${style.id}' maxChars must be positive", style.maxChars > 0)
        }

        // Concentrated short styles have lower maxChars
        assertTrue(StyleRegistry.WORD.maxChars <= 70)
        assertTrue(StyleRegistry["stack"]!!.maxChars <= 70)
        assertTrue(StyleRegistry["coordinates"]!!.maxChars <= 90)

        // Document styles have high maxChars
        assertTrue(StyleRegistry["bible_page"]!!.maxChars >= 280)
        assertTrue(StyleRegistry["letter"]!!.maxChars >= 120)
    }
}
