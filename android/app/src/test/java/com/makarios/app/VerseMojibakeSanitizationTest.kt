package com.makarios.app

import com.makarios.app.data.ScriptureDatabase
import com.makarios.app.data.bible.ScriptureText
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.InputStreamReader

/**
 * Item G Verification Tests:
 * 1. Read verses.json and assets as UTF-8 explicitly.
 * 2. Normalise all references to use a plain hyphen between ranges ("Ephesians 1:4-6").
 * 3. Assert no string in the verse database contains â€, Ã, or the replacement character .
 */
class VerseMojibakeSanitizationTest {

    private val forbiddenSubstrings = listOf("â€", "Ã", "\uFFFD")

    @Test
    fun scriptureDatabaseHasNoMojibakeOrCorruptedChars() {
        val verses = ScriptureDatabase.verses
        assertTrue("Scripture database must contain verses", verses.isNotEmpty())

        for (verse in verses) {
            forbiddenSubstrings.forEach { bad ->
                assertFalse(
                    "Verse reference '${verse.reference}' must not contain '$bad'",
                    verse.reference.contains(bad)
                )
                assertFalse(
                    "Verse text for '${verse.reference}' must not contain '$bad'",
                    verse.text.contains(bad)
                )
            }
            // All range references must use plain hyphens, never unicode em/en dashes
            assertFalse(
                "Verse reference '${verse.reference}' must use plain hyphen '-', not en-dash or em-dash",
                verse.reference.contains("–") || verse.reference.contains("—")
            )
        }
    }

    @Test
    fun referenceNormalizerCleansRangesAndMojibake() {
        val testCases = mapOf(
            "Ephesians 1:4â€“6" to "Ephesians 1:4-6",
            "Philippians 4:6–7" to "Philippians 4:6-7",
            "Matthew 11:28—30" to "Matthew 11:28-30",
            "Psalm 23:1 - 3" to "Psalm 23:1-3",
            "Romans 8:38-39" to "Romans 8:38-39"
        )

        for ((input, expected) in testCases) {
            val normalized = ScriptureText.normalizeReference(input)
            assertEquals("Normalized reference must match plain hyphen citation", expected, normalized)
        }
    }

    @Test
    fun versesJsonAssetHasNoMojibakeTokens() {
        val assetsFile = File("src/main/assets/verses.json")
        if (assetsFile.exists()) {
            val reader = InputStreamReader(assetsFile.inputStream(), Charsets.UTF_8)
            val content = reader.use { it.readText() }
            forbiddenSubstrings.forEach { bad ->
                assertFalse("verses.json must not contain '$bad'", content.contains(bad))
            }
        }
    }
}
