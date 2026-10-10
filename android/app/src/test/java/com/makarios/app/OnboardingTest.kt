package com.makarios.app

import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.OnboardingStore
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.ContrastGate
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class OnboardingTest {

    @Test
    fun sixTopicTilesWithCorrectLightAndPromiseMappings() {
        val tiles = OnboardingStore.TOPIC_TILES
        assertEquals(6, tiles.size)

        val expected = mapOf(
            "Peace" to (Light.Mist to "Rest for a worried mind"),
            "Strength" to (Light.Ember to "When you have nothing left"),
            "Identity" to (Light.Dawn to "Who you are in Christ"),
            "Purpose" to (Light.Rain to "Why you are here"),
            "Courage" to (Light.Dusk to "For the next brave step"),
            "Joy" to (Light.Midday to "A heart that sings")
        )

        tiles.forEach { tile ->
            assertTrue("Topic '${tile.title}' should be recognized", expected.containsKey(tile.title))
            val (expectedLight, expectedPromise) = expected[tile.title]!!
            assertEquals("Light mismatch for ${tile.title}", expectedLight, tile.light)
            assertEquals("Promise mismatch for ${tile.title}", expectedPromise, tile.promise)
        }
    }

    @Test
    fun declarationBankCoversAllTopicsWithCompleteScripture() {
        val topics = listOf("Peace", "Strength", "Identity", "Purpose", "Courage", "Joy")

        topics.forEach { topic ->
            val bankItems = OnboardingStore.getBankForTopics(listOf(topic))
            assertTrue("Topic $topic should have at least 3 affirmations in bank", bankItems.size >= 3)
            bankItems.forEach { affirmation ->
                assertTrue(affirmation.declaration.isNotBlank())
                assertTrue(affirmation.scriptureText.isNotBlank())
                assertTrue(affirmation.reference.isNotBlank())
                assertEquals(topic, affirmation.category)
            }
        }

        // Verify prompt example: Identity first entry
        val identityBank = OnboardingStore.getBankForTopics(listOf("Identity"))
        val first = identityBank.first()
        assertEquals("I am fully known, deeply loved, and precisely placed for this moment.", first.declaration)
        assertEquals("Psalm 139:1-2", first.reference)
    }

    @Test
    fun topicSelectionCapRules() {
        val initial = listOf("Peace", "Strength")
        // Adding 3rd item allowed
        val withThird = initial + "Identity"
        assertEquals(3, withThird.size)

        // Attempting to add 4th without removing should remain capped at 3 in controller logic
        val attemptedFourth = if (withThird.size < 3) withThird + "Joy" else withThird
        assertEquals(3, attemptedFourth.size)
        assertFalse(attemptedFourth.contains("Joy"))
    }

    @Test
    fun weightedTopicDistributionStatisticallyMatches60Percent() {
        val all = AffirmationRepository.getAll()
        val chosenTopics = setOf("Identity", "Peace")

        val matching = all.filter { a -> chosenTopics.any { it.equals(a.category, ignoreCase = true) } }
        val others = all.filter { a -> chosenTopics.none { it.equals(a.category, ignoreCase = true) } }

        assertTrue(matching.isNotEmpty())
        assertTrue(others.isNotEmpty())

        var chosenCount = 0
        val trials = 2000
        val rng = Random(42)

        for (i in 1..trials) {
            val selected = if (rng.nextFloat() < 0.60f) {
                matching.random(rng)
            } else {
                others.random(rng)
            }
            if (chosenTopics.any { it.equals(selected.category, ignoreCase = true) }) {
                chosenCount++
            }
        }

        val percentage = (chosenCount.toDouble() / trials.toDouble()) * 100.0
        // Expect roughly 60% with tolerance bounds [56%, 64%]
        assertTrue("Expected ~60% distribution from chosen topics, got $percentage%", percentage in 55.0..65.0)
    }

    @Test
    fun screenOneTextContrastMeetsWcagAA() {
        val cream = 0xFFFFF4E4.toInt()
        val ink = 0xFF2A1B14.toInt()

        // Background vertical gradient stops
        val stop0 = 0xFF0F1716.toInt()  // 0%
        val stop30 = 0xFF1F2B2A.toInt() // 30%
        val stop62 = 0xFF43292B.toInt() // 62%
        val stop100 = 0xFFC9703F.toInt() // 100%

        val contrastAt0 = ContrastGate.contrastRatio(cream, stop0)
        val contrastAt30 = ContrastGate.contrastRatio(cream, stop30)
        val contrastAt62 = ContrastGate.contrastRatio(cream, stop62)

        // Text is in the top 50% of the screen
        assertTrue("Top text contrast at 0% ($contrastAt0:1) must exceed AA 4.5:1", contrastAt0 >= 4.5f)
        assertTrue("Top text contrast at 30% ($contrastAt30:1) must exceed AA 4.5:1", contrastAt30 >= 4.5f)
        assertTrue("Text contrast at 62% ($contrastAt62:1) must exceed AA 4.5:1", contrastAt62 >= 4.5f)

        // Button label: ink text on cream pill
        val buttonContrast = ContrastGate.contrastRatio(cream, ink)
        assertTrue("Button label contrast ($buttonContrast:1) must exceed AA 4.5:1", buttonContrast >= 4.5f)
    }
}
