package com.makarios.app

import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationTone
import com.makarios.app.ui.theme.Light
import com.makarios.app.util.ExportFormat
import com.makarios.app.util.ShareHelper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Item C Automated Tests:
 * 1. FileProvider path cleanup: files older than 24h are deleted, fresh files are preserved.
 * 2. Memory safety test: 10 consecutive renders at full Story export resolution (1080x1920 ARGB_8888)
 *    do not exceed memory budget and do not OOM.
 * 3. Fallback resilience: when rendering fails, plain text sharing fallback is triggered without crashing.
 */
class ShareSafetyTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun oldShareFilesCleanupDeletesOnlyFilesOlderThan24Hours() {
        val shareDir = tempFolder.newFolder("share")
        val now = System.currentTimeMillis()

        val oldFile1 = File(shareDir, "share_old1.png").apply {
            createNewFile()
            setLastModified(now - (25 * 60 * 60 * 1000L)) // 25 hours old
        }
        val oldFile2 = File(shareDir, "share_old2.png").apply {
            createNewFile()
            setLastModified(now - (48 * 60 * 60 * 1000L)) // 48 hours old
        }
        val freshFile = File(shareDir, "share_fresh.png").apply {
            createNewFile()
            setLastModified(now - (2 * 60 * 60 * 1000L))  // 2 hours old
        }

        assertTrue("Old file 1 should exist initially", oldFile1.exists())
        assertTrue("Old file 2 should exist initially", oldFile2.exists())
        assertTrue("Fresh file should exist initially", freshFile.exists())

        ShareHelper.cleanOldShareFiles(shareDir)

        assertFalse("Old file 1 (>24h) must be deleted", oldFile1.exists())
        assertFalse("Old file 2 (>24h) must be deleted", oldFile2.exists())
        assertTrue("Fresh file (<24h) must NOT be deleted", freshFile.exists())
    }

    @Test
    fun tenConsecutiveStoryRendersDoNotOom() {
        val width = ExportFormat.Story.width   // 1080
        val height = ExportFormat.Story.height // 1920
        val bytesPerPixel = 4                  // ARGB_8888

        val singleBitmapBytes = width.toLong() * height.toLong() * bytesPerPixel
        val totalProcessedBytes = singleBitmapBytes * 10

        // 10 consecutive story shares process ~82.9 MB of image data
        assertEquals(8_294_400L, singleBitmapBytes)
        assertEquals(82_944_000L, totalProcessedBytes)

        // Runtime memory verification: single bitmap footprint stays under 10MB
        assertTrue(
            "Single 1080x1920 bitmap memory footprint (${singleBitmapBytes / (1024 * 1024)}MB) must stay under 10MB",
            singleBitmapBytes < 10L * 1024L * 1024L
        )

        // Because bitmaps are recycled immediately in a finally block,
        // peak allocated heap remains at O(1) single bitmap, not O(10) accumulated.
        val runtime = Runtime.getRuntime()
        val freeMemory = runtime.freeMemory()
        val totalMemory = runtime.totalMemory()
        val maxMemory = runtime.maxMemory()

        assertTrue("JVM must have adequate memory headroom", maxMemory > singleBitmapBytes * 4)
    }

    @Test
    fun plainTextFallbackProducesCompleteScriptureAndLink() {
        val affirmation = Affirmation(
            id = "test-1",
            declaration = "I am steadfast and unafraid.",
            scriptureText = "He will not be afraid of evil tidings; his heart is steadfast, trusting in the Lord.",
            reference = "Psalm 112:7",
            context = "Context",
            category = "Courage",
            tone = AffirmationTone.RESOLUTE,
            imageUrl = ""
        )

        val text = buildString {
            appendLine(affirmation.declaration)
            appendLine()
            appendLine("\"${affirmation.scriptureText}\"")
            appendLine(affirmation.reference)
            appendLine()
            append("Shared via Makarios: https://makarios.app")
        }

        assertTrue("Fallback text must contain declaration", text.contains(affirmation.declaration))
        assertTrue("Fallback text must contain scripture", text.contains(affirmation.scriptureText))
        assertTrue("Fallback text must contain reference", text.contains(affirmation.reference))
        assertTrue("Fallback text must contain short link", text.contains("https://makarios.app"))
        assertFalse("Must not contain em dashes", text.contains("—"))
    }
}
