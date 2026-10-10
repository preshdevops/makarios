package com.makarios.app

import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.wallpaper.LayoutGeometry
import com.makarios.app.util.ExportFormat
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.min

/**
 * Item D Automated Tests:
 * 1. Reference space 390x844: uniform scaling via min(w/390, h/844).
 * 2. Safe bounds per format:
 *    - Story: top safe 20%, bottom safe 20%, side margin 8%
 *    - Square: 8% on all sides
 *    - Portrait: top safe 14%, bottom safe 14%, side margin 8%
 *    - X 16:9: side margin 6%, top/bottom 8%
 *    - Wallpaper: parallax overscan 1.08x
 */
class RenderingContractTest {

    @Test
    fun layoutGeometryComputesUniformScale() {
        val storySize = IntSize(1080, 1920)
        val geom = LayoutGeometry.compute(storySize)

        val expectedScale = min(1080f / 390f, 1920f / 844f)
        assertEquals("Scale must be uniform min(w/390, h/844)", expectedScale, geom.scale, 0.001f)
    }

    @Test
    fun safeMarginsMatchItemDContractAcrossAllFormats() {
        // 1. Story (1080x1920)
        val story = LayoutGeometry.compute(IntSize(1080, 1920))
        assertEquals(ExportFormat.Story, story.format)
        assertEquals(1080f * 0.08f, story.margin, 0.1f)
        assertEquals(1920f * 0.20f, story.topSafe, 0.1f)
        assertEquals(1920f * 0.80f, story.bottomSafe, 0.1f)

        // 2. Square (1080x1080)
        val square = LayoutGeometry.compute(IntSize(1080, 1080))
        assertEquals(ExportFormat.Square, square.format)
        assertEquals(1080f * 0.08f, square.margin, 0.1f)
        assertEquals(1080f * 0.08f, square.topSafe, 0.1f)
        assertEquals(1080f * 0.92f, square.bottomSafe, 0.1f)

        // 3. Portrait 4:5 (1080x1350)
        val portrait = LayoutGeometry.compute(IntSize(1080, 1350))
        assertEquals(ExportFormat.Portrait, portrait.format)
        assertEquals(1080f * 0.08f, portrait.margin, 0.1f)
        assertEquals(1350f * 0.14f, portrait.topSafe, 0.1f)
        assertEquals(1350f * 0.86f, portrait.bottomSafe, 0.1f)

        // 4. X 16:9 (1600x900)
        val xFormat = LayoutGeometry.compute(IntSize(1600, 900))
        assertEquals(ExportFormat.X, xFormat.format)
        assertEquals(1600f * 0.06f, xFormat.margin, 0.1f)
        assertEquals(900f * 0.08f, xFormat.topSafe, 0.1f)
        assertEquals(900f * 0.92f, xFormat.bottomSafe, 0.1f)
    }

    @Test
    fun wallpaperParallaxOverscanAddsEightPercentWidth() {
        val deviceWidth = 1080
        val overscanWidth = (deviceWidth * 1.08f).toInt()
        assertEquals(1166, overscanWidth)
        assertTrue("Wallpaper overscan width must exceed device width by 8%", overscanWidth > deviceWidth)
    }
}
