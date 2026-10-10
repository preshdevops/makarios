package com.makarios.app

import android.graphics.RectF
import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.LayoutGeometry
import com.makarios.app.ui.wallpaper.StyleSpec
import com.makarios.app.ui.wallpaper.computeSafeDiscPosition
import com.makarios.app.ui.wallpaper.measurePairingBounds
import org.junit.Assert.*
import org.junit.Test

/**
 * Item F Verification Tests:
 * 1. Disc and glow must never sit behind text (clear text bounding box by at least 6% of width).
 * 2. On Story/Wallpaper, disc must never overlap the top 25% (status bar / clock).
 * 3. On Story/Wallpaper, disc must never overlap the right 14% (reaction rail).
 * 4. Verified across all 8 Lights and diverse declaration lengths.
 */
class SafeDiscPositioningTest {

    private val declarations = listOf(
        "I am held and known in every season.",
        "The Lord is my light and my salvation, whom shall I fear? The Lord is the stronghold of my life, of whom shall I be afraid?",
        "Peace be still.",
        "I will not be shaken, for my hope and my strength are in God forevermore."
    )

    @Test
    fun discClearsTextBlockByAtLeastSixPercentWidth() {
        val geom = LayoutGeometry.compute(IntSize(1080, 1920))
        val marginClear = geom.width * 0.06f

        Light.values().forEach { light ->
            declarations.forEach { decl ->
                val spec = StyleSpec(declaration = decl, verse = "A verse anchor", reference = "Psalm 23:1")
                val textBounds = measurePairingBounds(geom, light, spec)
                val discPos = computeSafeDiscPosition(geom, light, textBounds)
                val discR = light.discR * geom.scale

                val discBox = RectF(discPos.x - discR, discPos.y - discR, discPos.x + discR, discPos.y + discR)
                val blockedTextRect = RectF(
                    textBounds.left - marginClear,
                    textBounds.top - marginClear,
                    textBounds.right + marginClear,
                    textBounds.bottom + marginClear
                )

                val intersects = !(discPos.x + discR < blockedTextRect.left || discPos.x - discR > blockedTextRect.right || discPos.y + discR < blockedTextRect.top || discPos.y - discR > blockedTextRect.bottom)
                assertFalse(
                    "Disc at (${discPos.x}, ${discPos.y}) with radius $discR must not intersect blocked text rect in $light for '$decl'",
                    intersects
                )
            }
        }
    }

    @Test
    fun discOnStoryNeverOverlapsTopTwentyFivePercentOrRightFourteenPercent() {
        val geom = LayoutGeometry.compute(IntSize(1080, 1920))
        val minTopY = 1920f * 0.25f
        val maxRightX = 1080f * (1f - 0.14f)

        Light.values().forEach { light ->
            declarations.forEach { decl ->
                val spec = StyleSpec(declaration = decl, verse = "A verse anchor", reference = "Psalm 23:1")
                val textBounds = measurePairingBounds(geom, light, spec)
                val discPos = computeSafeDiscPosition(geom, light, textBounds)
                val discR = light.discR * geom.scale

                assertTrue(
                    "Disc top edge (${discPos.y - discR}) must sit below top 25% ($minTopY) in $light",
                    discPos.y - discR >= minTopY - 1f
                )
                assertTrue(
                    "Disc right edge (${discPos.x + discR}) must stay left of right 14% rail ($maxRightX) in $light",
                    discPos.x + discR <= maxRightX + 1f
                )
            }
        }
    }
}
