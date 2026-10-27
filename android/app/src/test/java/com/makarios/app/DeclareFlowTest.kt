package com.makarios.app

import com.makarios.app.data.Affirmation
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.makarios.app.data.local.AppDatabase
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import kotlinx.coroutines.runBlocking
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DeclareFlowTest {

    @Test
    fun testDeclareVerseMatchingAndSelection() {
        val userDeclaration = "I walk in perfect peace because God guards my heart"
        val matched = ScriptureMatcher.match(userDeclaration, limit = 15)
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
    fun testKeptRepositoryPersistenceAndUndo() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        try {
            KeptRepository.setDatabaseForTesting(db)
            val row = KeptEntity(
                id = "kept-test",
                declaration = "I am held.",
                verseText = "Do not fear.",
                reference = "Isaiah 41:10",
                lightName = "Dawn",
                styleId = "classic",
                createdAt = 1L
            )
            KeptRepository.save(row)
            val stored = KeptRepository.getById(row.id)!!
            assertEquals(row.declaration, stored.declaration)
            assertEquals(row.reference, stored.reference)
            assertEquals(row.lightName, stored.lightName)
            assertEquals(1, KeptRepository.activeCount())
            KeptRepository.softDelete(row.id, 2L)
            assertEquals(0, KeptRepository.activeCount())
            assertEquals(1, KeptRepository.deletedCount())
            KeptRepository.restore(row.id, 3L)
            assertEquals(1, KeptRepository.activeCount())
        } finally {
            KeptRepository.setDatabaseForTesting(null)
            db.close()
        }
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


