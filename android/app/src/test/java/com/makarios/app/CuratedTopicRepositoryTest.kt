package com.makarios.app

import com.makarios.app.data.CuratedTopicRepository
import com.makarios.app.data.bible.BibleReferenceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedTopicRepositoryTest {

    private val requiredTopics = listOf(
        "Identity", "Peace", "Strength", "Purpose", "Courage", "Joy", "Rest"
    )

    @Test
    fun testEveryTopicHasAtLeast25CuratedPairs() {
        for (topic in requiredTopics) {
            val count = CuratedTopicRepository.getCountForTopic(topic)
            assertTrue("Topic $topic must have >= 25 pairs, found $count", count >= 25)
        }
    }

    @Test
    fun testEveryReferenceParsesCleanly() {
        for (pair in CuratedTopicRepository.pairs) {
            assertTrue("Declaration cannot be blank", pair.declaration.isNotBlank())
            val parsed = BibleReferenceParser.parse(pair.reference)
            assertNotNull("Reference '${pair.reference}' for pair '${pair.id}' must parse into a valid Bible reference", parsed)
        }
    }

    @Test
    fun testSearchDeclarations() {
        val peaceMatches = CuratedTopicRepository.searchDeclarations("peace")
        assertTrue("Expected search for 'peace' to return matches", peaceMatches.isNotEmpty())

        val courageMatches = CuratedTopicRepository.searchDeclarations("courage")
        assertTrue("Expected search for 'courage' to return matches", courageMatches.isNotEmpty())
    }
}
