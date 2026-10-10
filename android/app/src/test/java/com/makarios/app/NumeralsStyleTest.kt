package com.makarios.app

import com.makarios.app.ui.wallpaper.AutoStyleSelector
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleSpec
import org.junit.Assert.*
import org.junit.Test

/**
 * Item E Verification Tests:
 * 1. Rule: use Numerals only when the chapter has 2 or more digits.
 * 2. For a 1-digit chapter, picker greys it out, and Auto picks another style (Page).
 * 3. Resolved chapter handling for chapters 1, 5, 9, 10, 23, 119, 150.
 */
class NumeralsStyleTest {

    @Test
    fun singleDigitChapterIsMarkedIneligible() {
        listOf("1", "5", "9").forEach { ch ->
            val spec = StyleSpec(declaration = "I am peace", chapterNumber = ch)
            assertFalse("Single-digit chapter $ch must NOT be eligible for Numerals", spec.isNumeralsEligible)
        }

        val refSpec = StyleSpec(declaration = "I am peace", reference = "Psalm 5:3")
        assertFalse("Single-digit chapter with 1-digit verse must NOT be eligible for Numerals", refSpec.isNumeralsEligible)
    }

    @Test
    fun twoOrMoreDigitChaptersAreEligible() {
        listOf("10", "23", "119", "150").forEach { ch ->
            val spec = StyleSpec(declaration = "The Lord is my shepherd", chapterNumber = ch)
            assertTrue("2+ digit chapter $ch must be eligible for Numerals", spec.isNumeralsEligible)
        }

        val refWithTwoDigitVerse = StyleSpec(declaration = "Light shines", reference = "Ephesians 5:13")
        assertTrue("Reference with 2-digit verse (5:13) should be eligible for Numerals", refWithTwoDigitVerse.isNumeralsEligible)
    }

    @Test
    fun autoSelectorPicksPageOverNumeralsForSingleDigitChapter() {
        val singleDigitChoice = AutoStyleSelector.pickStyle(
            declaration = "Short declaration",
            verse = "A much longer scripture verse text to prioritize scripture-forward styles",
            chapterNumber = "5"
        )
        assertNotEquals("Auto selector must not pick Numerals for 1-digit chapter", StyleRegistry.NUMERALS.id, singleDigitChoice.id)
        assertEquals("Auto selector should fallback to Page for scripture-forward 1-digit chapter", StyleRegistry.PAGE.id, singleDigitChoice.id)

        val twoDigitChoice = AutoStyleSelector.pickStyle(
            declaration = "Short declaration",
            verse = "A much longer scripture verse text to prioritize scripture-forward styles",
            chapterNumber = "23"
        )
        assertEquals("Auto selector must pick Numerals for 2-digit chapter", StyleRegistry.NUMERALS.id, twoDigitChoice.id)
    }

    @Test
    fun resolvedChapterMatchesExpectedInputs() {
        listOf("1", "5", "9", "10", "23", "119", "150").forEach { ch ->
            val spec = StyleSpec(declaration = "Declaration", chapterNumber = ch)
            assertEquals(ch, spec.resolvedChapter)
        }
    }
}
