package com.makarios.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.makarios.app.data.local.AppDatabase
import com.makarios.app.data.local.KeptDao
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DeclarationPickerTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: KeptDao
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.keptDao()
        KeptRepository.setDatabaseForTesting(db)
    }

    @After
    fun tearDown() {
        KeptRepository.setDatabaseForTesting(null)
        db.close()
    }

    @Test
    fun emptyPoolFallback_widgetFallsBackToMakarios() = runBlocking {
        // Given: local database has no items, settings ask for MINE only
        val settings = WidgetSettings(
            sources = setOf(DeclarationSource.MINE),
            order = DeclarationOrder.NEWEST_FIRST
        )

        // When: picking for widget
        val picked = DeclarationPicker.pickForWidget(context, settings)

        // Then: does not crash, falls back to Makarios pool
        assertNotNull(picked)
        assertTrue(picked.declaration.isNotBlank())
        assertTrue(picked.reference.isNotBlank())
    }

    @Test
    fun emptyPoolFallback_reminderFallsBackToMakarios() = runBlocking {
        // Given: local database has no items, settings ask for KEPT only
        val settings = ReminderSettings(
            sources = setOf(DeclarationSource.KEPT),
            order = DeclarationOrder.NEWEST_FIRST
        )

        // When: picking for reminder
        val picked = DeclarationPicker.pickForReminder(context, settings)

        // Then: does not crash, falls back to Makarios pool
        assertNotNull(picked)
        assertTrue(picked.declaration.isNotBlank())
        assertTrue(picked.reference.isNotBlank())
    }

    @Test
    fun flagsFirst_widgetPicksFlaggedItemFirst() = runBlocking {
        // Given: unflagged item created earlier, flagged item created later or vice versa
        val unflagged = KeptEntity(
            id = "unflagged-1",
            declaration = "Unflagged item",
            verseText = "Verse text",
            reference = "John 1:1",
            lightName = "Dawn",
            styleId = "pairing",
            createdAt = 2000L,
            sourceType = "authored",
            inWidget = false
        )
        val flagged = KeptEntity(
            id = "flagged-1",
            declaration = "Flagged item",
            verseText = "Verse text",
            reference = "John 1:2",
            lightName = "Dawn",
            styleId = "pairing",
            createdAt = 1000L,
            sourceType = "authored",
            inWidget = true
        )
        dao.insert(unflagged)
        dao.insert(flagged)

        val settings = WidgetSettings(
            sources = setOf(DeclarationSource.MINE),
            order = DeclarationOrder.NEWEST_FIRST
        )

        // When: picking for widget
        val picked = DeclarationPicker.pickForWidget(context, settings)

        // Then: flagged item is picked first even though unflagged was created more recently
        assertEquals("flagged-1", picked.id)
    }

    @Test
    fun flagsFirst_reminderPicksFlaggedItemFirst() = runBlocking {
        val unflagged = KeptEntity(
            id = "unflagged-rem",
            declaration = "Unflagged reminder",
            verseText = "Verse text",
            reference = "Romans 8:1",
            lightName = "Midday",
            styleId = "pairing",
            createdAt = 3000L,
            sourceType = "authored",
            inReminders = false
        )
        val flagged = KeptEntity(
            id = "flagged-rem",
            declaration = "Flagged reminder",
            verseText = "Verse text",
            reference = "Romans 8:2",
            lightName = "Midday",
            styleId = "pairing",
            createdAt = 1000L,
            sourceType = "authored",
            inReminders = true
        )
        dao.insert(unflagged)
        dao.insert(flagged)

        val settings = ReminderSettings(
            sources = setOf(DeclarationSource.MINE),
            order = DeclarationOrder.NEWEST_FIRST
        )

        // When: picking for reminder
        val picked = DeclarationPicker.pickForReminder(context, settings)

        // Then: flagged item is picked first
        assertEquals("flagged-rem", picked.id)
    }

    @Test
    fun shuffle_doesNotRepeatLastPickedWhenPoolGreaterThanOne() = runBlocking {
        // Given: 2 items in pool
        val item1 = KeptEntity(
            id = "item-1",
            declaration = "Item 1",
            verseText = "Verse 1",
            reference = "Psalm 23:1",
            lightName = "Dawn",
            styleId = "pairing",
            createdAt = 1000L,
            sourceType = "authored"
        )
        val item2 = KeptEntity(
            id = "item-2",
            declaration = "Item 2",
            verseText = "Verse 2",
            reference = "Psalm 23:2",
            lightName = "Dawn",
            styleId = "pairing",
            createdAt = 2000L,
            sourceType = "authored"
        )
        dao.insert(item1)
        dao.insert(item2)

        val settings = WidgetSettings(
            sources = setOf(DeclarationSource.MINE),
            order = DeclarationOrder.SHUFFLE
        )

        // When: excludeId is "item-1"
        val picked = DeclarationPicker.pickForWidget(context, settings, excludeId = "item-1")

        // Then: picked item cannot be "item-1"
        assertEquals("item-2", picked.id)
    }
}
