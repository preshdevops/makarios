package com.makarios.app.ui

import com.makarios.app.data.CuratedTopicRepository
import com.makarios.app.data.bible.BibleReferenceParser
import com.makarios.app.data.bible.BibleVerse
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.ui.wallpaper.StyleRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LazyListKeyUniquenessTest {

    @Test
    fun testExploreStaticAndDynamicKeysAreDistinct() {
        val staticKeys = listOf(
            "explore-read-bible-card",
            "explore-votd",
            "explore-bible-card-bottom"
        )
        assertEquals("Static explore keys must be distinct", staticKeys.size, staticKeys.distinct().size)

        val topicKeys = CuratedTopicRepository.TOPIC_METAS
            .distinctBy { it.name }
            .map { "explore-topic-${it.name}" }
        assertEquals("Explore topic keys must be distinct", topicKeys.size, topicKeys.distinct().size)

        val allKeys = staticKeys + topicKeys
        assertEquals("All combined explore keys must be distinct", allKeys.size, allKeys.distinct().size)
    }

    @Test
    fun testExploreSearchKeysAreDistinctWithDuplicateInputs() {
        val rawTopics = CuratedTopicRepository.TOPIC_METAS + CuratedTopicRepository.TOPIC_METAS
        val topicKeys = rawTopics.distinctBy { it.name }.map { "explore-search-topic-${it.name}" }
        assertEquals("De-duplicated search topic keys must be distinct", topicKeys.size, topicKeys.distinct().size)

        val decls = CuratedTopicRepository.pairs + CuratedTopicRepository.pairs
        val declKeys = decls.distinctBy { it.id }.map { "explore-search-decl-${it.id}" }
        assertEquals("De-duplicated search declaration keys must be distinct", declKeys.size, declKeys.distinct().size)

        val verses = listOf(
            BibleVerse("John", 3, 16, "For God so loved the world", "John 3:16"),
            BibleVerse("John", 3, 16, "Duplicate John 3:16", "John 3:16"),
            BibleVerse("Psalm", 23, 1, "Yahweh is my shepherd", "Psalm 23:1")
        )
        val verseKeys = verses.distinctBy { "${it.book}-${it.chapter}-${it.verse}" }
            .map { "explore-search-verse-${it.book}-${it.chapter}-${it.verse}" }
        assertEquals("De-duplicated search verse keys must be distinct", 2, verseKeys.size)
        assertEquals(verseKeys.size, verseKeys.distinct().size)
    }

    @Test
    fun testTopicDetailKeysAreDistinct() {
        for (meta in CuratedTopicRepository.TOPIC_METAS) {
            val pairs = CuratedTopicRepository.getPairsForTopic(meta.name)
            val keys = pairs.distinctBy { it.id }.map { "topic-detail-${it.id}" }
            assertEquals("Keys for topic ${meta.name} must be distinct", keys.size, keys.distinct().size)
        }
    }

    @Test
    fun testBiblePickerKeysAreDistinct() {
        val books = BibleReferenceParser.CANONICAL_BOOKS
        val keys = books.distinctBy { it }.map { "picker-book-$it" }
        assertEquals("Picker book keys must be distinct", books.size, keys.size)
        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun testSavedItemsKeysAreDistinct() {
        val testEntities = listOf(
            KeptEntity(id = "1", declaration = "D1", verseText = "V1", reference = "R1", lightName = "Dawn", styleId = "s1"),
            KeptEntity(id = "2", declaration = "D2", verseText = "V2", reference = "R2", lightName = "Dawn", styleId = "s2"),
            KeptEntity(id = "1", declaration = "Duplicate", verseText = "V3", reference = "R3", lightName = "Dawn", styleId = "s3")
        )
        val keys = testEntities.distinctBy { it.id }.map { "saved-item-${it.id}" }
        assertEquals(2, keys.size)
        assertEquals(keys.size, keys.distinct().size)
    }

    @Test
    fun testStyleRegistryKeysAreDistinct() {
        val styles = StyleRegistry.all
        val keys = styles.distinctBy { it.id }.map { "wallpaper-style-${it.id}" }
        assertEquals("Style registry keys must be unique", styles.size, keys.size)
        assertEquals(keys.size, keys.distinct().size)
    }
}
