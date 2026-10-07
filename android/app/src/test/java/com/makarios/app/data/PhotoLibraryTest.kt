package com.makarios.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoLibraryTest {

    @Test
    fun allPhotoIdsAndUnsplashIdsAreGloballyUnique() {
        val allEntries = PhotoLibrary.entries
        val uniqueIds = allEntries.map { it.id }.toSet()
        val uniqueUnsplashIds = allEntries.map { it.unsplashId }.toSet()

        assertEquals("All photo IDs must be globally unique", allEntries.size, uniqueIds.size)
        assertEquals("All Unsplash photo IDs must be globally unique", allEntries.size, uniqueUnsplashIds.size)
    }

    @Test
    fun createBackgroundsDoNotOverlapOtherRoles() {
        val createPhotos = PhotoLibrary.getCreateBackgrounds()
        val otherPhotos = PhotoLibrary.entries.filter { it.role != PhotoRole.CREATE_BACKGROUND }

        val createUnsplashIds = createPhotos.map { it.unsplashId }.toSet()
        val otherUnsplashIds = otherPhotos.map { it.unsplashId }.toSet()

        val overlap = createUnsplashIds.intersect(otherUnsplashIds)
        assertTrue("Create backgrounds must be disjoint from all other roles, but found overlap: $overlap", overlap.isEmpty())
    }

    @Test
    fun allThemesHaveDedicatedPhotos() {
        val themes = listOf(
            "Identity", "Peace", "Strength", "Purpose",
            "Courage", "Joy", "Provision", "Confidence",
            "Relationships", "Discipline", "Personal"
        )

        val themePhotos = themes.map { PhotoLibrary.getThemePhoto(it) }
        val uniqueThemeUnsplashIds = themePhotos.map { it.unsplashId }.toSet()

        assertEquals("Every theme must have a distinct photo", themes.size, uniqueThemeUnsplashIds.size)
    }

    @Test
    fun descriptionsAreLiteralAndNonEmpty() {
        for (entry in PhotoLibrary.entries) {
            assertTrue("Description for ${entry.id} must not be blank", entry.description.isNotBlank())
            assertTrue("Unsplash ID for ${entry.id} must not be blank", entry.unsplashId.isNotBlank())
            assertTrue("URL for ${entry.id} must be valid HTTPS", entry.url().startsWith("https://images.unsplash.com/photo-"))
        }
    }
}
