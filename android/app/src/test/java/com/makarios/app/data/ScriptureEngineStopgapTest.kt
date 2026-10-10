package com.makarios.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptureEngineStopgapTest {

    @Test
    fun testLiterallyDoAnythingMapsToPhilippians() {
        val matches = ScriptureMatcher.match("I can literally do anything")
        assertTrue("Expected matches for 'I can literally do anything'", matches.isNotEmpty())
        assertEquals("Philippians 4:13", matches.first().verse.reference)
    }

    @Test
    fun testNeverStrandedReturnsPromissoryAndExcludesNarrative() {
        val matches = ScriptureMatcher.match("I am never stranded")
        assertTrue("Expected matches for 'I am never stranded'", matches.isNotEmpty())
        val topRef = matches.first().verse.reference
        assertTrue(
            "Expected top match to be promissory (Deuteronomy 31:6 or Psalm), got $topRef",
            topRef == "Deuteronomy 31:6" || topRef.startsWith("Psalm")
        )
        // Must never return narrative offenders
        val references = matches.map { it.verse.reference }
        assertFalse("Must exclude narrative Mark 14:7", references.contains("Mark 14:7"))
        assertFalse("Must exclude narrative Matthew 26:11", references.contains("Matthew 26:11"))
        assertFalse("Must exclude narrative John 7:4", references.contains("John 7:4"))
    }

    @Test
    fun testLowConfidenceTriggersNoMatch() {
        val matches = ScriptureMatcher.match("qzxv toaster 9911")
        assertTrue("Gibberish query should produce no confident matches (< 0.35)", matches.isEmpty())
    }

    @Test
    fun testCuratedBankMappings() {
        assertEquals("Philippians 4:13", ScriptureMatcher.match("I can do all things").first().verse.reference)
        val lovedTop = ScriptureMatcher.match("I am loved").first().verse.reference
        assertTrue("Expected Romans 8 verse for loved, got $lovedTop", lovedTop.startsWith("Romans 8"))
        assertEquals("Isaiah 41:10", ScriptureMatcher.match("I am not afraid").first().verse.reference)
        assertEquals("Isaiah 53:5", ScriptureMatcher.match("I am healed").first().verse.reference)
        val blessedTop = ScriptureMatcher.match("I am blessed").first().verse.reference
        assertTrue("Expected Ephesians 1:3 or Deuteronomy 28:13 for blessed, got $blessedTop", blessedTop == "Ephesians 1:3" || blessedTop == "Deuteronomy 28:13")
        assertEquals("Deuteronomy 31:6", ScriptureMatcher.match("I am never alone").first().verse.reference)
        assertEquals("1 John 1:9", ScriptureMatcher.match("I am forgiven").first().verse.reference)
    }

    @Test
    fun testFillerWordsDoNotPolluteMatching() {
        val matches = ScriptureMatcher.match("I am really literally very blessed")
        assertTrue(matches.isNotEmpty())
        val topRef = matches.first().verse.reference
        assertTrue(
            "Expected blessing verse despite filler words, got $topRef",
            topRef == "Ephesians 1:3" || topRef == "Deuteronomy 28:13"
        )
    }
}
