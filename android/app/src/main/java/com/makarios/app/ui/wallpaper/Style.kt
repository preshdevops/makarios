package com.makarios.app.ui.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.Light
import com.makarios.app.util.ExportFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** Text budget per format for optimal readability without crowding. */
data class TextBudget(
    val maxDeclarationSp: Float,
    val minDeclarationSp: Float,
    val maxVerseSp: Float,
    val minVerseSp: Float
)

/** Specification for styling a declaration, scripture verse and optional metadata. */
data class StyleSpec(
    val declaration: String,
    val verse: String? = null,
    val reference: String? = null,
    val chapterNumber: String? = null,
    val shortText: String = declaration.take(40),
    val seed: Int = 23
) {
    /** Resolved chapter number: uses explicit value, extracts from reference, or defaults to 23. */
    val resolvedChapter: String
        get() = chapterNumber
            ?: reference?.substringAfterLast(" ")?.substringBefore(":")?.filter { it.isDigit() }?.ifEmpty { null }
            ?: "23"
}

data class CuratedVerse(val text: String, val reference: String)

val CuratedWallpaperVerses = listOf(
    CuratedVerse("The Lord is my shepherd; I shall not want.", "Psalm 23:1"),
    CuratedVerse("He restores my soul.", "Psalm 23:3"),
    CuratedVerse("I will fear no evil, for you are with me.", "Psalm 23:4"),
    CuratedVerse("Be still, and know that I am God.", "Psalm 46:10"),
    CuratedVerse("The Lord is my light and my salvation.", "Psalm 27:1"),
    CuratedVerse("He gives power to the faint.", "Isaiah 40:29"),
    CuratedVerse("My grace is sufficient for you.", "2 Corinthians 12:9"),
    CuratedVerse("The Lord will watch over your coming and going.", "Psalm 121:8"),
    CuratedVerse("I have loved you with an everlasting love.", "Jeremiah 31:3"),
    CuratedVerse("Under his wings you will find refuge.", "Psalm 91:4"),
    CuratedVerse("The peace of God will guard your hearts.", "Philippians 4:7"),
    CuratedVerse("Those who hope in the Lord will renew their strength.", "Isaiah 40:31")
)

/** Backward-compatible WallpaperSpec bridging to unified StyleSpec. */
data class WallpaperSpec(
    val style: Style,
    val light: Light,
    val verse: CuratedVerse? = null,
    val declaration: String? = null,
    val seed: Int = 23
) {
    fun asStyleSpec(): StyleSpec = StyleSpec(
        declaration = declaration.orEmpty().ifEmpty { verse?.text.orEmpty() },
        verse = verse?.text,
        reference = verse?.reference,
        chapterNumber = verse?.reference?.substringAfterLast(" ")?.substringBefore(":")?.filter { it.isDigit() },
        shortText = (declaration ?: verse?.text).orEmpty().take(40),
        seed = seed
    )
}

typealias WallpaperStyle = Style

fun defaultWallpaperSpec(style: Style, light: Light = Light.forNow()): WallpaperSpec =
    WallpaperSpec(style, light, CuratedWallpaperVerses.first())

/**
 * A Style is a pure, offscreen renderer: Style x Light x Format -> Bitmap.
 * Designed in a 390x844 reference coordinate space and scaled by (width/390).
 */
interface Style {
    val id: String
    val displayName: String

    fun render(spec: StyleSpec, light: Light, size: IntSize): Bitmap

    fun supports(format: ExportFormat): Boolean
    fun supports(format: String): Boolean = runCatching {
        val f = ExportFormat.values().firstOrNull { it.name.equals(format, ignoreCase = true) }
            ?: if (format.lowercase() == "whatsapp" || format.lowercase() == "status") ExportFormat.Story else ExportFormat.Story
        supports(f)
    }.getOrDefault(true)

    fun textBudget(format: ExportFormat): TextBudget
    fun textBudget(format: String): TextBudget = runCatching {
        val f = ExportFormat.values().firstOrNull { it.name.equals(format, ignoreCase = true) }
            ?: ExportFormat.Story
        textBudget(f)
    }.getOrDefault(TextBudget(48f, 26f, 28f, 20f))

    companion object {
        val PAIRING: Style get() = StyleRegistry.PAIRING
        val WINDOWS: Style get() = StyleRegistry.WINDOWS
        val RAYS: Style get() = StyleRegistry.RAYS
        val NUMERALS: Style get() = StyleRegistry.NUMERALS
        val PAPER: Style get() = StyleRegistry.PAPER
        val CONSTELLATION: Style get() = StyleRegistry.CONSTELLATION
        val WORD: Style get() = StyleRegistry.WORD
        val PAGE: Style get() = StyleRegistry.PAGE
        val EIGHT_LIGHTS: Style get() = StyleRegistry.EIGHT_LIGHTS
        val CROSS: Style get() = StyleRegistry.CROSS
        val PATH: Style get() = StyleRegistry.PATH
        val TIDE: Style get() = StyleRegistry.TIDE

        val TEXT: Style get() = PAIRING

        fun values(): Array<Style> = StyleRegistry.all.toTypedArray()
        fun valueOf(id: String): Style = StyleRegistry[id]
            ?: error("Style not found for id: $id. Available: ${StyleRegistry.all.map { it.id }}")
    }
}

/** Registry of the 12 canonical Makarios styles. */
object StyleRegistry {
    val PAIRING: Style = PairingStyle
    val WINDOWS: Style = WindowsStyle
    val RAYS: Style = RaysStyle
    val NUMERALS: Style = NumeralsStyle
    val PAPER: Style = PaperStyle
    val CONSTELLATION: Style = ConstellationStyle
    val WORD: Style = WordStyle
    val PAGE: Style = PageStyle
    val EIGHT_LIGHTS: Style = EightLightsStyle
    val CROSS: Style = CrossStyle
    val PATH: Style = PathStyle
    val TIDE: Style = TideStyle

    val all: List<Style> = listOf(
        PAIRING, WINDOWS, RAYS, NUMERALS,
        PAPER, CONSTELLATION, WORD, PAGE,
        EIGHT_LIGHTS, CROSS, PATH, TIDE
    )

    private val byId: Map<String, Style> = all.associateBy { it.id.lowercase() }

    operator fun get(id: String): Style? = byId[id.lowercase()]
        ?: all.firstOrNull { it.displayName.equals(id, ignoreCase = true) }
}

/**
 * Intelligent automatic style selector based on declaration semantics, length and spiritual context.
 */
object AutoStyleSelector {
    fun pickStyle(
        declaration: String,
        verse: String? = null,
        reference: String? = null,
        chapterNumber: String? = null,
        light: Light = Light.forNow()
    ): Style {
        val trimmed = declaration.trim()
        val lower = trimmed.lowercase()
        val len = trimmed.length
        val hasChapter = !chapterNumber.isNullOrBlank() || (reference != null && reference.contains(":"))

        // 1. Night hour -> Constellation
        if (light == Light.Night && (lower.contains("night") || lower.contains("star") || lower.contains("sleep") || lower.contains("watch") || lower.contains("dark"))) {
            return StyleRegistry.CONSTELLATION
        }

        // 2. <= 28 chars and no verse chapter -> WORD
        if (len <= 28 && !hasChapter) {
            return StyleRegistry.WORD
        }

        // 3. Scripture-forward -> Page or Numerals
        if (verse != null && (verse.length >= len || !chapterNumber.isNullOrBlank())) {
            return if (!chapterNumber.isNullOrBlank()) StyleRegistry.NUMERALS else StyleRegistry.PAGE
        }

        // 4. Peace / rest -> Tide or Paper
        if (lower.contains("peace") || lower.contains("rest") || lower.contains("still") || lower.contains("calm") || lower.contains("quiet") || lower.contains("water") || lower.contains("sea") || lower.contains("soul")) {
            return if (lower.contains("water") || lower.contains("sea") || lower.contains("tide") || lower.contains("wave")) {
                StyleRegistry.TIDE
            } else {
                StyleRegistry.PAPER
            }
        }

        // 5. Courage / strength -> Rays or Cross
        if (lower.contains("strength") || lower.contains("courage") || lower.contains("strong") || lower.contains("power") || lower.contains("bold") || lower.contains("fear not") || lower.contains("overcome") || lower.contains("cross")) {
            return if (lower.contains("cross") || lower.contains("grace") || lower.contains("faith") || lower.contains("blood")) {
                StyleRegistry.CROSS
            } else {
                StyleRegistry.RAYS
            }
        }

        // 6. Guidance -> Path
        if (lower.contains("path") || lower.contains("way") || lower.contains("guide") || lower.contains("lead") || lower.contains("step") || lower.contains("walk") || lower.contains("light to my path")) {
            return StyleRegistry.PATH
        }

        // 7. Identity -> Eight Lights or Windows
        if (lower.startsWith("i am") || lower.contains("child of god") || lower.contains("created") || lower.contains("chosen") || lower.contains("beloved") || lower.contains("heir") || lower.contains("righteous")) {
            return if (len > 35) StyleRegistry.WINDOWS else StyleRegistry.EIGHT_LIGHTS
        }

        // Night default fallback
        if (light == Light.Night) return StyleRegistry.CONSTELLATION

        return StyleRegistry.PAIRING
    }

    fun surpriseMe(seed: Int = Random.nextInt()): Style {
        val styles = StyleRegistry.all
        return styles[Random(seed).nextInt(styles.size)]
    }
}

/** Settings for sharing and exports, persisted in SharedPreferences. */
object ShareSettings {
    private const val PREFS_NAME = "makarios_share_prefs"
    private const val KEY_WORDMARK = "include_wordmark"

    fun isWordmarkEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WORDMARK, true)

    fun setWordmarkEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WORDMARK, enabled)
            .apply()
    }
}

/**
 * Automated WCAG AA Contrast Gate (4.5:1 for body/medium, 3.0:1 for large display 24sp+).
 * If contrast fails, a localized soft vignette is applied underneath the text box so text color stays pristine.
 */
object ContrastGate {
    /** Computes WCAG relative luminance from an ARGB integer color. */
    fun relativeLuminance(color: Int): Float {
        fun channel(c: Int): Float {
            val s = (c and 0xFF) / 255f
            return if (s <= 0.04045f) s / 12.92f else Math.pow(((s + 0.055) / 1.055), 2.4).toFloat()
        }
        val r = channel((color shr 16) and 0xFF)
        val g = channel((color shr 8) and 0xFF)
        val b = channel(color and 0xFF)
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    /** Computes contrast ratio between two ARGB integer colors. */
    fun contrastRatio(colorA: Int, colorB: Int): Float {
        val l1 = relativeLuminance(colorA)
        val l2 = relativeLuminance(colorB)
        val high = max(l1, l2)
        val low = min(l1, l2)
        return (high + 0.05f) / (low + 0.05f)
    }

    /** Samples background pixels in a grid across the bounding rect and calculates minimum/mean contrast. */
    fun sampleContrast(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int, textColor: Int): Float {
        if (bitmap.isRecycled) return 5f
        val w = bitmap.width
        val h = bitmap.height
        val x0 = left.coerceIn(0, w - 1)
        val y0 = top.coerceIn(0, h - 1)
        val x1 = right.coerceIn(x0, w - 1)
        val y1 = bottom.coerceIn(y0, h - 1)
        if (x1 <= x0 || y1 <= y0) return 5f

        val samplesX = 6
        val samplesY = 6
        var totalContrast = 0f
        var count = 0

        for (ix in 0..samplesX) {
            val x = x0 + ((x1 - x0) * ix) / samplesX
            for (iy in 0..samplesY) {
                val y = y0 + ((y1 - y0) * iy) / samplesY
                val pixel = bitmap.getPixel(x, y)
                totalContrast += contrastRatio(pixel, textColor)
                count++
            }
        }
        return if (count > 0) totalContrast / count else 5f
    }
}
