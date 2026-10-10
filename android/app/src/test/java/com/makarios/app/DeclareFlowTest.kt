package com.makarios.app

import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.AffirmationTone
import com.makarios.app.data.ScriptureDatabase
import com.makarios.app.data.ScriptureMatcher
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.AutoStyleSelector
import com.makarios.app.ui.wallpaper.StyleRegistry
import com.makarios.app.ui.wallpaper.StyleSpec
import com.makarios.app.util.ExportFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclareFlowTest {

    @Test
    fun testDeclareVerseMatchingAndSelection() {
        val userDeclaration = "I walk in perfect peace because God guards my heart"
        val matched = ScriptureMatcher.match(userDeclaration, AffirmationTone.STILL, limit = 15)
        assertTrue("Expected non-empty matched verses for peaceful declaration", matched.isNotEmpty())

        val bestMatch = matched[0].verse
        assertNotNull(bestMatch.reference)
        assertNotNull(bestMatch.text)

        // Ensure alternative verses (index 1 and 2) exist and differ from best match
        if (matched.size > 1) {
            val alt1 = matched[1].verse
            assertTrue("Alternative reference should not equal best match", alt1.reference != bestMatch.reference)
        }
    }

    @Test
    fun testAffirmationSavingIdempotency() {
        val testId = "test-declare-${System.currentTimeMillis()}"
        val affirmation1 = Affirmation(
            id = testId,
            declaration = "My future is secure in God's hands",
            scriptureText = "For I know the plans I have for you...",
            reference = "Jeremiah 29:11",
            context = "Authored in Makarios Declare Studio.",
            category = "Personal",
            tone = AffirmationTone.STILL,
            imageUrl = "",
            isFavorite = true
        )

        AffirmationRepository.addPersonalAffirmation(affirmation1)
        val initialCount = AffirmationRepository.getAll().count { it.id == testId }
        assertEquals(1, initialCount)

        // Re-saving same affirmation should maintain single instance
        AffirmationRepository.addPersonalAffirmation(affirmation1)
        val secondCount = AffirmationRepository.getAll().count { it.id == testId }
        assertEquals(1, secondCount)
        assertTrue(AffirmationRepository.isSaved(testId))
    }

    @Test
    fun testStyleSelectionAcrossDeclareFlow() {
        val declaration = "I am strong in the grace that is in Christ Jesus"
        val verseText = "Be strong in the Lord and in his mighty power."
        val reference = "Ephesians 6:10"

        val style = AutoStyleSelector.pickStyle(
            declaration = declaration,
            verse = verseText,
            reference = reference,
            light = Light.Midday
        )
        assertNotNull("Picked style must not be null", style)
        assertTrue("Picked style must support Story format", style.supports(ExportFormat.Story))
    }

    @Test
    fun testExportFormatDimensionsAndMultipliers() {
        assertEquals(1080, ExportFormat.Story.width)
        assertEquals(1920, ExportFormat.Story.height)
        assertEquals(1080, ExportFormat.Square.width)
        assertEquals(1080, ExportFormat.Square.height)
        assertEquals(1080, ExportFormat.Portrait.width)
        assertEquals(1350, ExportFormat.Portrait.height)
        assertEquals(1600, ExportFormat.X.width)
        assertEquals(900, ExportFormat.X.height)

        // 2x Save to photos resolution calculation test
        val scale = 2f
        assertEquals(2160, (ExportFormat.Story.width * scale).toInt())
        assertEquals(3840, (ExportFormat.Story.height * scale).toInt())
    }
}
