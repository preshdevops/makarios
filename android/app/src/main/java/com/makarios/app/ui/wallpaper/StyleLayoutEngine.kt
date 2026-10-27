package com.makarios.app.ui.wallpaper

import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import com.makarios.app.ui.theme.Light
import com.makarios.app.util.ExportFormat
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

/**
 * Result of measuring and fitting text into a target bounding box.
 */
data class TextFitResult(
    val layout: StaticLayout,
    val bounds: RectF,
    val fontSizeSp: Float,
    val text: String
)

/**
 * Personalisation options for parameterised styles.
 */
data class StyleOptions(
    val daysCount: Int? = null,
    val recipientName: String? = null,
    val date: String? = null,
    val textAlign: String? = null, // "left", "center", "right"
    val fontChoice: String? = null, // "serif", "sans", "display"
    val accentColorHex: String? = null
) {
    fun toJson(): String {
        val obj = JSONObject()
        daysCount?.let { obj.put("daysCount", it) }
        recipientName?.let { obj.put("recipientName", it) }
        date?.let { obj.put("date", it) }
        textAlign?.let { obj.put("textAlign", it) }
        fontChoice?.let { obj.put("fontChoice", it) }
        accentColorHex?.let { obj.put("accentColorHex", it) }
        return obj.toString()
    }

    companion object {
        fun fromJson(json: String?): StyleOptions {
            if (json.isNullOrBlank()) return StyleOptions()
            return runCatching {
                val obj = JSONObject(json)
                StyleOptions(
                    daysCount = if (obj.has("daysCount")) obj.getInt("daysCount") else null,
                    recipientName = if (obj.has("recipientName")) obj.getString("recipientName") else null,
                    date = if (obj.has("date")) obj.getString("date") else null,
                    textAlign = if (obj.has("textAlign")) obj.getString("textAlign") else null,
                    fontChoice = if (obj.has("fontChoice")) obj.getString("fontChoice") else null,
                    accentColorHex = if (obj.has("accentColorHex")) obj.getString("accentColorHex") else null
                )
            }.getOrDefault(StyleOptions())
        }
    }
}

enum class StyleOptionField {
    DAYS_COUNT,
    RECIPIENT_NAME,
    DATE,
    TEXT_ALIGN,
    FONT_CHOICE,
    ACCENT_COLOR
}

/**
 * Complete layout result for the vertical stack of declaration, rule, verse, and reference.
 */
data class VerticalStackResult(
    val declResult: TextFitResult,
    val declBounds: RectF,
    val ruleBounds: RectF?,
    val verseResult: TextFitResult?,
    val verseBounds: RectF?,
    val refResult: TextFitResult?,
    val refBounds: RectF?,
    val totalBounds: RectF
)

object StyleLayoutEngine {

    /**
     * Measures text with StaticLayout, shrinking the font size until it fits the box.
     * Guaranteed:
     * 1. Fits within [box.width()] and [box.height()].
     * 2. Minimum text size is at least 28px on a 1080 canvas (~11sp at reference scale).
     * 3. Truncates verse text with "..." if still exceeding box at minimum size.
     */
    fun fitText(
        text: String,
        box: RectF,
        font: Typeface?,
        maxSp: Float,
        minSp: Float,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        color: Int = AndroidColor.BLACK,
        canvasScale: Float = 1f,
        allowTruncate: Boolean = false,
        lineSpacingMultiplier: Float = 1.18f
    ): TextFitResult {
        val targetWidth = max(10, box.width().toInt())
        val targetMaxHeight = max(10f, box.height())

        // Minimum text size is at least 28px on a 1080 canvas
        // (1080 / 390 = 2.769). 28 / 2.769 ~= 10.1sp.
        val effectiveMinSp = max(minSp, 10.5f)

        fun createLayout(candidateText: String, sp: Float): StaticLayout {
            val paint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
                this.typeface = font ?: Typeface.DEFAULT
                this.textSize = sp * canvasScale
                this.color = color
                this.isSubpixelText = true
            }
            return StaticLayout.Builder.obtain(candidateText, 0, candidateText.length, paint, targetWidth)
                .setAlignment(align)
                .setIncludePad(false)
                .setLineSpacing(0f, lineSpacingMultiplier)
                .build()
        }

        var currentSp = maxSp
        var bestLayout: StaticLayout? = null
        var bestSp = effectiveMinSp

        while (currentSp >= effectiveMinSp) {
            val l = createLayout(text, currentSp)
            if (l.height <= targetMaxHeight) {
                bestLayout = l
                bestSp = currentSp
                break
            }
            currentSp -= 0.5f
        }

        var finalText = text
        if (bestLayout == null) {
            val minLayout = createLayout(text, effectiveMinSp)
            if (allowTruncate && minLayout.height > targetMaxHeight) {
                val words = text.split(Regex("\\s+"))
                var low = 1
                var high = words.size
                var fittingWords = 1
                while (low <= high) {
                    val mid = (low + high) / 2
                    val candidate = words.take(mid).joinToString(" ") + "..."
                    val l = createLayout(candidate, effectiveMinSp)
                    if (l.height <= targetMaxHeight) {
                        fittingWords = mid
                        low = mid + 1
                    } else {
                        high = mid - 1
                    }
                }
                finalText = words.take(fittingWords).joinToString(" ") + "..."
                bestLayout = createLayout(finalText, effectiveMinSp)
                bestSp = effectiveMinSp
            } else {
                bestLayout = minLayout
                bestSp = effectiveMinSp
            }
        }

        val bounds = RectF(
            box.left,
            box.top,
            box.left + targetWidth,
            box.top + bestLayout.height
        )

        return TextFitResult(bestLayout, bounds, bestSp, finalText)
    }

    /**
     * Computes the safe boundaries for layout based on canvas dimensions and format.
     * Safe margin: 8% of width on left and right, 10% on top and bottom.
     * On Wallpaper lock screen: top 22% is kept clear for the system clock.
     */
    fun computeSafeBounds(
        width: Float,
        height: Float,
        format: ExportFormat,
        isLockScreen: Boolean = false
    ): RectF {
        val marginX = width * 0.08f
        val safeTop = when {
            format == ExportFormat.Wallpaper && isLockScreen -> height * 0.22f
            format == ExportFormat.Wallpaper -> height * 0.16f
            format == ExportFormat.Story -> height * 0.14f
            format == ExportFormat.Portrait -> height * 0.12f
            format == ExportFormat.Square -> height * 0.10f
            format == ExportFormat.X -> height * 0.10f
            else -> height * 0.10f
        }
        val safeBottom = when {
            format == ExportFormat.Story || format == ExportFormat.Wallpaper -> height * 0.88f
            format == ExportFormat.Portrait -> height * 0.88f
            else -> height * 0.90f
        }
        return RectF(marginX, safeTop, width - marginX, safeBottom)
    }

    /**
     * Places declaration, rule, verse, reference in a vertical stack centered inside safe area.
     * Constraints:
     * - Declaration max 55% of safe box height.
     * - Verse max 25% of safe box height.
     * - All items strictly inside safeBounds, no two items ever intersect.
     */
    fun layoutVerticalStack(
        width: Float,
        height: Float,
        safeBounds: RectF,
        spec: StyleSpec,
        light: Light,
        canvasScale: Float,
        declFont: Typeface?,
        verseFont: Typeface?,
        refFont: Typeface?,
        centerAlign: Boolean = false,
        includeRule: Boolean = true,
        includeVerse: Boolean = true,
        includeRef: Boolean = true,
        declMaxSp: Float = 34f,
        verseMaxSp: Float = 17f,
        refMaxSp: Float = 12f
    ): VerticalStackResult {
        val safeW = safeBounds.width()
        val safeH = safeBounds.height()

        val maxDeclH = safeH * 0.55f
        val maxVerseH = safeH * 0.25f
        val maxRefH = safeH * 0.10f

        val align = if (centerAlign) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL

        // 1. Fit declaration
        val declBox = RectF(safeBounds.left, safeBounds.top, safeBounds.right, safeBounds.top + maxDeclH)
        val declResult = fitText(
            text = spec.declaration,
            box = declBox,
            font = declFont,
            maxSp = declMaxSp,
            minSp = 14f,
            align = align,
            color = light.text.toArgb(),
            canvasScale = canvasScale,
            allowTruncate = false
        )

        // 2. Fit verse if present
        val verseText = if (includeVerse) spec.verse.orEmpty().trim() else ""
        val verseResult = if (verseText.isNotBlank()) {
            val verseBox = RectF(safeBounds.left, safeBounds.top, safeBounds.right, safeBounds.top + maxVerseH)
            fitText(
                text = verseText,
                box = verseBox,
                font = verseFont,
                maxSp = verseMaxSp,
                minSp = 11f,
                align = align,
                color = light.text.toArgb(),
                canvasScale = canvasScale,
                allowTruncate = true
            )
        } else null

        // 3. Fit reference if present
        val refText = if (includeRef) spec.reference.orEmpty().trim().uppercase() else ""
        val refResult = if (refText.isNotBlank()) {
            val refBox = RectF(safeBounds.left, safeBounds.top, safeBounds.right, safeBounds.top + maxRefH)
            fitText(
                text = refText,
                box = refBox,
                font = refFont,
                maxSp = refMaxSp,
                minSp = 10.5f,
                align = align,
                color = light.text.toArgb(),
                canvasScale = canvasScale,
                allowTruncate = false
            )
        } else null

        // 4. Measure vertical spacing
        val ruleW = 28f * canvasScale
        val ruleH = if (includeRule) 2f * canvasScale else 0f
        val gap1 = if (includeRule) 18f * canvasScale else 14f * canvasScale
        val gap2 = if (verseResult != null) 14f * canvasScale else 0f
        val gap3 = if (refResult != null) 10f * canvasScale else 0f

        var totalContentH = declResult.layout.height.toFloat()
        if (includeRule) {
            totalContentH += gap1 + ruleH
        }
        if (verseResult != null) {
            totalContentH += gap2 + verseResult.layout.height
        }
        if (refResult != null) {
            totalContentH += gap3 + refResult.layout.height
        }

        // 5. Center the vertical stack inside safe area
        val startY = safeBounds.top + max(0f, (safeH - totalContentH) / 2f)

        var curY = startY
        val finalDeclBounds = RectF(safeBounds.left, curY, safeBounds.right, curY + declResult.layout.height)
        curY += declResult.layout.height

        val finalRuleBounds = if (includeRule) {
            curY += gap1
            val rx = if (centerAlign) (safeBounds.left + safeW / 2f - ruleW / 2f) else safeBounds.left
            val rb = RectF(rx, curY, rx + ruleW, curY + ruleH)
            curY += ruleH
            rb
        } else null

        val finalVerseBounds = if (verseResult != null) {
            curY += gap2
            val vb = RectF(safeBounds.left, curY, safeBounds.right, curY + verseResult.layout.height)
            curY += verseResult.layout.height
            vb
        } else null

        val finalRefBounds = if (refResult != null) {
            curY += gap3
            val rb = RectF(safeBounds.left, curY, safeBounds.right, curY + refResult.layout.height)
            curY += refResult.layout.height
            rb
        } else null

        val totalBounds = RectF(safeBounds.left, startY, safeBounds.right, curY)

        return VerticalStackResult(
            declResult = declResult,
            declBounds = finalDeclBounds,
            ruleBounds = finalRuleBounds,
            verseResult = verseResult,
            verseBounds = finalVerseBounds,
            refResult = refResult,
            refBounds = finalRefBounds,
            totalBounds = totalBounds
        )
    }

    /**
     * Draws the computed vertical stack to an AndroidCanvas.
     */
    fun drawVerticalStack(
        canvas: AndroidCanvas,
        stack: VerticalStackResult,
        light: Light
    ) {
        // Draw Declaration
        canvas.save()
        canvas.translate(stack.declBounds.left, stack.declBounds.top)
        stack.declResult.layout.draw(canvas)
        canvas.restore()

        // Draw Rule
        stack.ruleBounds?.let { rb ->
            val rulePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
                color = light.anchorRule.toArgb()
            }
            canvas.drawRect(rb, rulePaint)
        }

        // Draw Verse
        stack.verseResult?.let { vr ->
            stack.verseBounds?.let { vb ->
                canvas.save()
                canvas.translate(vb.left, vb.top)
                vr.layout.draw(canvas)
                canvas.restore()
            }
        }

        // Draw Reference
        stack.refResult?.let { rr ->
            stack.refBounds?.let { rb ->
                canvas.save()
                canvas.translate(rb.left, rb.top)
                rr.layout.draw(canvas)
                canvas.restore()
            }
        }
    }
}
