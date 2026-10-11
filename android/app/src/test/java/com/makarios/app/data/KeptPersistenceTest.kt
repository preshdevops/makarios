package com.makarios.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.makarios.app.data.local.AppDatabase
import com.makarios.app.data.local.KeptDao
import com.makarios.app.data.local.KeptEntity
import kotlinx.coroutines.flow.first
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
class KeptPersistenceTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: KeptDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.keptDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadKeptEntitiesInOrder() = runBlocking {
        val item1 = KeptEntity(
            id = "item-1",
            declaration = "I am never stranded.",
            verseText = "He will never leave you nor forsake you.",
            reference = "Deuteronomy 31:6",
            lightName = "Dawn",
            styleId = "pairing",
            createdAt = 1000L,
            sourceType = "authored"
        )
        val item2 = KeptEntity(
            id = "item-2",
            declaration = "God is on my inside.",
            verseText = "Christ in you, the hope of glory.",
            reference = "Colossians 1:27",
            lightName = "Midday",
            styleId = "windows",
            createdAt = 2000L,
            sourceType = "authored"
        )
        val item3 = KeptEntity(
            id = "item-3",
            declaration = "I can literally do anything through Christ.",
            verseText = "I can do all things through him who strengthens me.",
            reference = "Philippians 4:13",
            lightName = "Dusk",
            styleId = "numerals",
            createdAt = 3000L,
            sourceType = "bank"
        )

        dao.insert(item1)
        dao.insert(item2)
        dao.insert(item3)

        val all = dao.getAllFlow().first()
        assertEquals(3, all.size)
        // Descending createdAt order: item3 (3000), item2 (2000), item1 (1000)
        assertEquals("item-3", all[0].id)
        assertEquals("item-2", all[1].id)
        assertEquals("item-1", all[2].id)

        // Mine tab filter
        val mine = dao.getMineFlow().first()
        assertEquals(2, mine.size)
        assertEquals("item-2", mine[0].id)
        assertEquals("item-1", mine[1].id)

        // Declarations tab filter
        val decls = dao.getDeclarationsFlow().first()
        assertEquals(1, decls.size)
        assertEquals("item-3", decls[0].id)
    }

    @Test
    fun deleteAndUndoRestoresEntity() = runBlocking {
        val item = KeptEntity(
            id = "item-undo",
            declaration = "He restores my soul.",
            verseText = "He restores my soul.",
            reference = "Psalm 23:3",
            lightName = "Grove",
            styleId = "pairing",
            createdAt = 5000L,
            sourceType = "authored"
        )
        dao.insert(item)
        assertEquals(1, dao.getAllFlow().first().size)

        // Delete
        dao.delete(item)
        assertEquals(0, dao.getAllFlow().first().size)

        // Undo (re-insert)
        dao.insert(item)
        val restored = dao.getAllFlow().first()
        assertEquals(1, restored.size)
        assertEquals("item-undo", restored[0].id)
        assertEquals("He restores my soul.", restored[0].declaration)
    }
}
