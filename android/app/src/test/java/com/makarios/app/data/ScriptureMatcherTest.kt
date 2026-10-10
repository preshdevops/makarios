package com.makarios.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptureMatcherTest {
    @Test fun canonicalRegressionMapsAnythingToPhilippians() { assertEquals("Philippians 4:13", ScriptureMatcher.match("I can do anything").first().verse.reference) }
    @Test fun blessingRegressionRejectsCurseVerse() { assertEquals("Deuteronomy 28:13", ScriptureMatcher.match("I am the head and not the tail").first().verse.reference); assertTrue(ScriptureMatcher.match("I am the head and not the tail").none { it.verse.reference == "Deuteronomy 28:44" }) }
    @Test fun gibberishHasNoMatch() { assertTrue(ScriptureMatcher.match("qzxv toaster 9911").isEmpty()) }
    @Test fun firstPersonLoveUsesIdentityBank() { assertEquals("Psalm 139:1-3", ScriptureMatcher.match("I am fully known, deeply loved, and precisely placed").first().verse.reference) }
}



