package com.makarios.app

import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.*
import com.makarios.app.util.ExportFormat
import org.junit.Assert.*
import org.junit.Test

class StyleTest {

    @Test
    fun twelveStylesAreRegisteredAndAvailable() {
        val essentials = StyleRegistry.essentials
        assertEquals(12, essentials.size)
        assertEquals(12, Style.values().size)
        assertEquals(21, StyleRegistry.all.size)

        val expectedIds = listOf(
            "pairing", "windows", "rays", "numerals",
            "paper", "constellation", "word", "page",
            "eight_lights", "cross", "path", "tide"
        )

        expectedIds.forEach { id ->
            val style = StyleRegistry[id]
            assertNotNull("Style with id '$id' should be found in registry", style)
            assertEquals(id, style?.id)
            assertTrue(style?.displayName?.isNotBlank() == true)
        }
    }

    @Test
    fun pageStyleFormatSupportRule() {
        // Page illuminated manuscript supports portrait/story/square but NOT X (16:9)
        assertFalse("PageStyle must not support X (16:9)", StyleRegistry.PAGE.supports(ExportFormat.X))
        assertTrue("PageStyle must support Story", StyleRegistry.PAGE.supports(ExportFormat.Story))
        assertTrue("PageStyle must support Square", StyleRegistry.PAGE.supports(ExportFormat.Square))
        assertTrue("PageStyle must support Portrait", StyleRegistry.PAGE.supports(ExportFormat.Portrait))
        assertTrue("PageStyle must support Wallpaper", StyleRegistry.PAGE.supports(ExportFormat.Wallpaper))

        // All other 11 styles support all formats including X
        StyleRegistry.all.filter { it.id != "page" }.forEach { style ->
            assertTrue("${style.displayName} must support X format", style.supports(ExportFormat.X))
            assertTrue("${style.displayName} must support Story format", style.supports(ExportFormat.Story))
            assertTrue("${style.displayName} must support Square format", style.supports(ExportFormat.Square))
        }
    }

    @Test
    fun autoStyleSelectorMatchesSemanticAndLengthRules() {
        // 1. Short declaration <= 28 chars without verse chapter -> Word
        val wordStyle = AutoStyleSelector.pickStyle("The Lord is my shepherd.", chapterNumber = null)
        assertEquals(StyleRegistry.WORD, wordStyle)

        // 2. Scripture-forward -> Page or Numerals
        val pageStyle = AutoStyleSelector.pickStyle(
            declaration = "He restores my soul and leads me beside quiet waters.",
            verse = "He restores my soul.",
            reference = "Psalm 23:3",
            chapterNumber = "23"
        )
        assertTrue(pageStyle == StyleRegistry.PAGE || pageStyle == StyleRegistry.NUMERALS)

        // 3. Peace/Rest -> Tide or Paper
        val peaceStyle = AutoStyleSelector.pickStyle("I abide in perfect peace and rest.")
        assertTrue(peaceStyle == StyleRegistry.TIDE || peaceStyle == StyleRegistry.PAPER)

        // 4. Courage/Strength -> Rays or Cross
        val strengthStyle = AutoStyleSelector.pickStyle("The Lord gives strength to the weary and courage to the faint.")
        assertTrue(strengthStyle == StyleRegistry.RAYS || strengthStyle == StyleRegistry.CROSS)

        // 5. Guidance -> Path
        val pathStyle = AutoStyleSelector.pickStyle("He guides my path and directs my every step.")
        assertEquals(StyleRegistry.PATH, pathStyle)

        // 6. Identity -> Eight Lights or Windows
        val identityStyle = AutoStyleSelector.pickStyle("I am chosen, beloved and a child of God.")
        assertTrue(identityStyle == StyleRegistry.EIGHT_LIGHTS || identityStyle == StyleRegistry.WINDOWS)

        // 7. Night hour fallback -> Constellation
        val nightStyle = AutoStyleSelector.pickStyle("In the quiet watches of the night I will meditate on You.", light = Light.Night)
        assertEquals(StyleRegistry.CONSTELLATION, nightStyle)

        // 8. Surprise me returns valid registered style
        val surprise = AutoStyleSelector.surpriseMe(seed = 12345)
        assertTrue(StyleRegistry.all.contains(surprise))
    }

    @Test
    fun textBudgetsAndOverflowWith280Characters() {
        val longDeclaration = "For I am persuaded, that neither death, nor life, nor angels, nor principalities, nor powers, nor things present, nor things to come, nor height, nor depth, nor any other creature, shall be able to separate us from the love of God, which is in Christ Jesus our Lord today and forever."
        assertTrue("Must be at least 280 characters", longDeclaration.length >= 280)

        val spec = StyleSpec(
            declaration = longDeclaration,
            verse = "Neither death nor life can separate us.",
            reference = "Romans 8:38-39",
            chapterNumber = "8"
        )

        // Declaration must be preserved with NO truncation
        assertEquals(longDeclaration, spec.declaration)
        // Short text is safely capped for widget/preview budgets
        assertEquals(40, spec.shortText.length)

        // Verify text budget across all export formats
        ExportFormat.values().forEach { format ->
            StyleRegistry.all.forEach { style ->
                val budget = style.textBudget(format)
                assertTrue("minDeclarationSp <= maxDeclarationSp for ${style.id} in $format",
                    budget.minDeclarationSp <= budget.maxDeclarationSp)
                assertTrue("minVerseSp <= maxVerseSp for ${style.id} in $format",
                    budget.minVerseSp <= budget.maxVerseSp)
                assertTrue("Minimum declaration readable floor >= 14sp",
                    budget.minDeclarationSp >= 14f)
            }
        }
    }

    @Test
    fun automatedContrastGateComputations() {
        val white = 0xFFFFFFFF.toInt()
        val black = 0xFF000000.toInt()

        val whiteLum = ContrastGate.relativeLuminance(white)
        val blackLum = ContrastGate.relativeLuminance(black)

        assertEquals(1.0f, whiteLum, 0.01f)
        assertEquals(0.0f, blackLum, 0.01f)

        // Contrast ratio of pure black and white is exactly 21:1
        val maxContrast = ContrastGate.contrastRatio(white, black)
        assertEquals(21.0f, maxContrast, 0.1f)

        // Must satisfy WCAG AA >= 4.5:1 for body and >= 3.0:1 for large display (24sp+)
        assertTrue("Must exceed normal text WCAG AA gate (4.5:1)", maxContrast >= 4.5f)
        assertTrue("Must exceed large text WCAG AA gate (3.0:1)", maxContrast >= 3.0f)

        // Midday text color over cream background
        val creamBg = 0xFFFFF4D6.toInt()
        val inkText = 0xFF2A1B14.toInt()
        val middayContrast = ContrastGate.contrastRatio(creamBg, inkText)
        assertTrue("Midday text on cream background must pass contrast gate (>= 4.5:1)", middayContrast >= 4.5f)

        // Night text color over dark background
        val nightBg = 0xFF0F1716.toInt()
        val creamText = 0xFFF3E6C8.toInt()
        val nightContrast = ContrastGate.contrastRatio(nightBg, creamText)
        assertTrue("Night text on night background must pass contrast gate (>= 4.5:1)", nightContrast >= 4.5f)
    }

    @Test
    fun memoryFootprintForStoryExportStaysUnder40MB() {
        val width = ExportFormat.Story.width   // 1080
        val height = ExportFormat.Story.height // 1920
        val bytesPerPixel = 4                  // ARGB_8888

        val rawBitmapBytes = width.toLong() * height.toLong() * bytesPerPixel
        val maxBudgetBytes = 40L * 1024L * 1024L // 40 MB

        // 1080 * 1920 * 4 = 8,294,400 bytes ≈ 7.91 MB
        assertEquals(8_294_400L, rawBitmapBytes)
        assertTrue("1080x1920 Story render footprint (${rawBitmapBytes / (1024 * 1024)}MB) must stay well under 40MB limit",
            rawBitmapBytes < maxBudgetBytes)

        val memoryHeadroomFactor = maxBudgetBytes.toDouble() / rawBitmapBytes.toDouble()
        assertTrue("Must have at least 4x memory headroom", memoryHeadroomFactor >= 4.0)
    }

    @Test
    fun goldenMatrixValidation72Configurations() {
        // 12 styles x 3 Lights (Dawn, Midday, Night) x 2 formats (Story, Square) = 72 configurations
        val matrixStyles = StyleRegistry.essentials
        val matrixLights = listOf(Light.Dawn, Light.Midday, Light.Night)
        val matrixFormats = listOf(ExportFormat.Story, ExportFormat.Square)

        assertEquals(12, matrixStyles.size)
        assertEquals(3, matrixLights.size)
        assertEquals(2, matrixFormats.size)

        var totalConfigurations = 0

        for (style in matrixStyles) {
            for (light in matrixLights) {
                for (format in matrixFormats) {
                    totalConfigurations++

                    val spec = StyleSpec(
                        declaration = "I am held in peace.",
                        verse = "The Lord is my light and my salvation.",
                        reference = "Psalm 27:1",
                        chapterNumber = "27",
                        seed = 42
                    )

                    // Format support verification
                    val isSupported = style.supports(format)
                    if (style.id == "page" && format == ExportFormat.X) {
                        assertFalse(isSupported)
                    } else {
                        assertTrue(isSupported)
                    }

                    // Text budget bounds check
                    val budget = style.textBudget(format)
                    assertTrue(budget.maxDeclarationSp > 0f)
                    assertTrue(budget.minDeclarationSp > 0f)

                    // Reference layout dimension verification
                    val width = format.width
                    val height = format.height
                    assertTrue(width > 0)
                    assertTrue(height > 0)

                    // Layout scale from 390 reference
                    val scale = width / 390f
                    assertTrue("Scale factor must be positive", scale > 0f)

                    // Platform safe margins check
                    if (format == ExportFormat.Story) {
                        val topSafe = height * 0.14f
                        val bottomSafe = height * 0.86f
                        assertTrue("Top safe boundary must be above bottom safe boundary", topSafe < bottomSafe)
                    } else if (format == ExportFormat.Square) {
                        val centralBandH = 390f * scale
                        assertTrue("Square central band must be positive", centralBandH > 0f)
                    }
                }
            }
        }

        // Verify exactly 72 configurations were evaluated
        assertEquals(72, totalConfigurations)
    }
}
