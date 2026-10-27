package com.makarios.app.ui

import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.*
import com.makarios.app.util.ExportFormat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.min

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StyleAuditTest {

    private val testLights = listOf(Light.Dawn, Light.Ember, Light.Night)

    private val testSizes = listOf(
        Pair(ExportFormat.Story, IntSize(1080, 1920)),
        Pair(ExportFormat.Square, IntSize(1080, 1080)),
        Pair(ExportFormat.Portrait, IntSize(1080, 1350)),
        Pair(ExportFormat.X, IntSize(1600, 900)),
        Pair(ExportFormat.Wallpaper, IntSize(1080, 2400))
    )

    private val text1Short = StyleSpec(
        declaration = "I am victorious",
        verse = null,
        reference = null
    )

    private val text2Medium = StyleSpec(
        declaration = "The Lord is my light and my salvation; whom shall I fear? The Lord is the strength of my life.",
        verse = "The LORD is my light and my salvation; whom shall I fear?",
        reference = "Psalm 27:1"
    )

    private val text3Long = StyleSpec(
        declaration = "I declare that no weapon formed against me shall prosper, and every tongue that rises against me in judgment is condemned. The peace of God, which surpasses all understanding, guards my heart and mind through Christ Jesus. I am strong in the Lord and in the power of His might every single day.",
        verse = null,
        reference = null
    )

    private val text4LongPlusLongVerse = StyleSpec(
        declaration = "I declare that no weapon formed against me shall prosper, and every tongue that rises against me in judgment is condemned. The peace of God, which surpasses all understanding, guards my heart and mind through Christ Jesus. I am strong in the Lord and in the power of His might every single day.",
        verse = "For God so loved the world that He gave His only begotten Son, that whoever believes in Him should not perish but have everlasting life. For God did not send His Son into the world to condemn the world, but that the world through Him might be saved. He who believes in Him is not condemned; but he who does not believe is condemned already, because he has not believed in the name of the Son of God.",
        reference = "JOHN 3:16-18"
    )

    private val testTexts = listOf(text1Short, text2Medium, text3Long, text4LongPlusLongVerse)

    @Test
    fun auditNew20StylesLayoutCompliance() {
        val failures = mutableListOf<String>()
        var totalAudited = 0

        val stylesToAudit = StyleRegistry.new20

        for (style in stylesToAudit) {
            for (light in testLights) {
                for ((format, size) in testSizes) {
                    for ((textIndex, spec) in testTexts.withIndex()) {
                        totalAudited++
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val s = min(w / 390f, h / 844f)

                        val safe = StyleLayoutEngine.computeSafeBounds(w, h, format, isLockScreen = false)

                        val stack = StyleLayoutEngine.layoutVerticalStack(
                            width = w,
                            height = h,
                            safeBounds = safe,
                            spec = spec,
                            light = light,
                            canvasScale = s,
                            declFont = Typeface.DEFAULT_BOLD,
                            verseFont = Typeface.DEFAULT,
                            refFont = Typeface.DEFAULT,
                            centerAlign = true,
                            includeRule = true,
                            includeVerse = spec.verse != null,
                            includeRef = spec.reference != null
                        )

                        // 1. Text fits inside safe area
                        if (stack.declBounds.left < safe.left - 1f || stack.declBounds.right > safe.right + 1f ||
                            stack.declBounds.top < safe.top - 1f || stack.declBounds.bottom > safe.bottom + 1f) {
                            failures.add("Style ${style.id}: Declaration out of bounds for format $format text #$textIndex. Bounds: ${stack.declBounds}, Safe: $safe")
                        }

                        // 2. Minimum text size on 1080 canvas is at least 28px
                        val declPx = stack.declResult.fontSizeSp * s
                        if (w >= 1080f && declPx < 28f) {
                            failures.add("Style ${style.id}: Declaration text size too small ($declPx px < 28px) for format $format text #$textIndex")
                        }

                        // 3. No two text boxes intersect
                        if (stack.verseBounds != null) {
                            if (RectF.intersects(stack.declBounds, stack.verseBounds)) {
                                failures.add("Style ${style.id}: Declaration and Verse intersect for format $format text #$textIndex")
                            }
                            if (stack.verseBounds.bottom > safe.bottom + 1f) {
                                failures.add("Style ${style.id}: Verse exceeds safe bottom for format $format text #$textIndex")
                            }
                        }

                        if (stack.refBounds != null) {
                            if (RectF.intersects(stack.declBounds, stack.refBounds)) {
                                failures.add("Style ${style.id}: Declaration and Reference intersect for format $format text #$textIndex")
                            }
                            if (stack.verseBounds != null && RectF.intersects(stack.verseBounds, stack.refBounds)) {
                                failures.add("Style ${style.id}: Verse and Reference intersect for format $format text #$textIndex")
                            }
                            if (stack.refBounds.bottom > safe.bottom + 1f) {
                                failures.add("Style ${style.id}: Reference exceeds safe bottom for format $format text #$textIndex")
                            }
                        }
                    }
                }
            }
        }

        println("Style Audit complete. Audited $totalAudited configurations across ${stylesToAudit.size} styles.")
        if (failures.isNotEmpty()) {
            println("Failures:\n" + failures.joinToString("\n"))
        }
        assertTrue("Style audit failed with ${failures.size} issues:\n${failures.take(10).joinToString("\n")}", failures.isEmpty())
    }
}
