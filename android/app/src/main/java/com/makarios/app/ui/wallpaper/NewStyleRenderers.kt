package com.makarios.app.ui.wallpaper

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.makarios.app.data.SeedDeclarations
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ExportFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.*
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Shared text helper: Shrinks text in 2% steps until it fits within maxWidth/maxHeight.
// ---------------------------------------------------------------------------
internal fun autoShrinkLayout(
    text: String,
    typeface: Typeface?,
    initialSizePx: Float,
    minSizePx: Float,
    maxWidthPx: Int,
    maxHeightPx: Float,
    textColor: Int,
    alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    lineSpacingMultiplier: Float = 1.18f
): Pair<StaticLayout, Float> {
    val safeWidth = maxWidthPx.coerceAtLeast(60)
    var size = initialSizePx
    var layout: StaticLayout
    while (true) {
        val paint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            this.typeface = typeface
            this.textSize = size
            this.color = textColor
            this.isSubpixelText = true
        }
        layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
            .setIncludePad(false)
            .setLineSpacing(0f, lineSpacingMultiplier)
            .setAlignment(alignment)
            .build()
        if (layout.height <= maxHeightPx && layout.width <= safeWidth) break
        if (size <= minSizePx) break
        size *= 0.98f // 2% decrements
    }
    return layout to size
}

internal fun drawTextLayout(canvas: AndroidCanvas, layout: StaticLayout, x: Float, y: Float) {
    canvas.save()
    canvas.translate(x, y)
    layout.draw(canvas)
    canvas.restore()
}

// ---------------------------------------------------------------------------
// 22. STACK STYLE (Type that speaks)
// Each word its own size, loud and left-aligned ("I am" light italic, "NOT" huge, "AFRAID" medium indented)
// ---------------------------------------------------------------------------
object StackStyle : BaseStyle("stack", "Stack", category = "Type", supportsLights = true, maxChars = 70) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val words = spec.declaration.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val lines = when {
            words.size <= 2 -> listOf(words.joinToString(" "))
            words.size == 3 -> listOf(words[0], words[1].uppercase(), words[2].uppercase())
            words.size == 4 -> listOf("${words[0]} ${words[1]}", words[2].uppercase(), words[3].uppercase())
            else -> {
                val mid = words.size / 2
                listOf(
                    words.take(mid).joinToString(" "),
                    words.slice(mid until words.size - 1).joinToString(" ").uppercase(),
                    words.lastOrNull()?.uppercase().orEmpty()
                )
            }
        }

        val leftX = geom.margin
        val availW = if (geom.isLandscapeX) (geom.width * 0.52f - geom.margin).toInt() else (geom.width - geom.margin * 2f).toInt()
        var curY = if (geom.isLandscapeX) geom.topSafe + 16f * s else geom.topSafe + 28f * s

        val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()
        val newsreaderBold = runCatching { Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD) }.getOrNull()
        val hanken = StyleTypefaces.getHankenGrotesk()

        // Line 1: Light italic
        if (lines.isNotEmpty()) {
            val (l1, _) = autoShrinkLayout(lines[0], newsreaderItalic, 36f * s, 18f * s, availW, geom.height * 0.16f, light.text.toArgb())
            drawTextLayout(canvas, l1, leftX, curY)
            curY += l1.height + 8f * s
        }

        // Line 2: Huge bold caps
        if (lines.size >= 2) {
            val (l2, _) = autoShrinkLayout(lines[1], newsreaderBold, 64f * s, 26f * s, availW, geom.height * 0.22f, light.text.toArgb())
            drawTextLayout(canvas, l2, leftX, curY)
            curY += l2.height + 10f * s
        }

        // Line 3: Medium indented
        if (lines.size >= 3) {
            val indent = 28f * s
            val (l3, _) = autoShrinkLayout(lines[2], hanken, 34f * s, 16f * s, availW - indent.toInt(), geom.height * 0.16f, light.text.toArgb())
            drawTextLayout(canvas, l3, leftX + indent, curY)
            curY += l3.height + 16f * s
        }

        // Verse and reference
        val verseText = spec.verse.orEmpty()
        val refText = (spec.reference ?: "").uppercase()
        if (geom.isLandscapeX) {
            val rightX = geom.width * 0.56f
            val rightW = (geom.width - rightX - geom.margin).toInt()
            var rY = geom.topSafe + 32f * s
            if (verseText.isNotBlank()) {
                val (vl, _) = autoShrinkLayout("“$verseText”", newsreaderItalic, 22f * s, 14f * s, rightW, geom.height * 0.40f, light.text.copy(alpha = 0.85f).toArgb())
                drawTextLayout(canvas, vl, rightX, rY)
                rY += vl.height + 12f * s
            }
            if (refText.isNotBlank()) {
                val (rl, _) = autoShrinkLayout(refText, hanken, 13f * s, 10f * s, rightW, 30f * s, light.text.copy(alpha = 0.65f).toArgb())
                drawTextLayout(canvas, rl, rightX, rY)
            }
        } else {
            val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
            canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
            curY += 14f * s

            if (verseText.isNotBlank()) {
                val (vl, _) = autoShrinkLayout("“$verseText”", newsreaderItalic, 19f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
                drawTextLayout(canvas, vl, leftX, curY)
                curY += vl.height + 10f * s
            }
            if (refText.isNotBlank()) {
                val (rl, _) = autoShrinkLayout(refText, hanken, 13f * s, 10f * s, availW, 30f * s, light.text.copy(alpha = 0.65f).toArgb())
                drawTextLayout(canvas, rl, leftX, curY)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 23. DROP CAP STYLE (Type that speaks)
// Giant first letter in the sun colour (#B5532B), rest of declaration beside and under it
// ---------------------------------------------------------------------------
object DropCapStyle : BaseStyle("drop_cap", "Drop cap", category = "Type", supportsLights = true, maxChars = 120) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val text = spec.declaration.trim()
        val firstLetter = text.take(1).uppercase()
        val restText = text.drop(1).trimStart()

        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        val startY = geom.topSafe + 24f * s

        // Giant first letter in sun color
        val sunColor = if (light.dark) AndroidColor.parseColor("#E8A860") else AndroidColor.parseColor("#B5532B")
        val dropPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = StyleTypefaces.getNewsreader()
            textSize = 96f * s
            color = sunColor
            isSubpixelText = true
        }
        val dropBounds = Rect()
        dropPaint.getTextBounds(firstLetter, 0, firstLetter.length, dropBounds)
        val dropCapW = dropBounds.width() + 18f * s
        val dropCapH = dropBounds.height().toFloat()

        canvas.drawText(firstLetter, leftX, startY + dropCapH, dropPaint)

        // Remaining text wraps beside and under the drop cap
        val newsreader = StyleTypefaces.getNewsreader()
        val besideW = (availW - dropCapW).toInt().coerceAtLeast(100)
        val (restLayout, _) = autoShrinkLayout(restText, newsreader, 28f * s, 16f * s, besideW, geom.height * 0.35f, light.text.toArgb())

        drawTextLayout(canvas, restLayout, leftX + dropCapW, startY + 6f * s)

        val textBottom = startY + max(dropCapH, restLayout.height.toFloat()) + 20f * s

        // Rule & scripture
        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, textBottom, leftX + 48f * s, textBottom + 2f * s, rulePaint)

        var curY = textBottom + 16f * s
        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.20f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, availW, 28f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 24. OUTLINE STYLE (Type that speaks)
// One key word, huge and hollow (stroke 2.5px, 70% sun colour), behind the declaration
// ---------------------------------------------------------------------------
object OutlineStyle : BaseStyle("outline", "Outline", category = "Type", supportsLights = true, maxChars = 90) {
    private val keyLexicon = setOf(
        "STRONG", "LOVED", "FREE", "PEACE", "GRACE", "FAITH", "SHIELD", "LIGHT",
        "VICTORY", "REST", "ALIVE", "JOY", "HOPE", "HEALED", "TRUE", "GOD", "HOLY"
    )

    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val words = spec.declaration.trim().split(Regex("[\\s,;.]+")).filter { it.isNotBlank() }
        val keyword = words.firstOrNull { keyLexicon.contains(it.uppercase()) }?.uppercase()
            ?: words.maxByOrNull { it.length }?.uppercase() ?: "FREE"

        val sunColor = if (light.dark) AndroidColor.argb(178, 232, 168, 96) else AndroidColor.argb(178, 181, 83, 43)
        val outlinePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = runCatching { Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD) }.getOrNull()
            textSize = (if (geom.isLandscapeX) 90f else 120f) * s
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2.5f * s
            color = sunColor
        }

        val outlineY = if (geom.isLandscapeX) geom.height * 0.45f else geom.height * 0.36f
        canvas.drawText(keyword, geom.margin * 0.8f, outlineY, outlinePaint)

        // Foreground declaration and verse
        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 32f, verseSizeSp = 17f, topOverride = outlineY - 30f * s)
    }
}

// ---------------------------------------------------------------------------
// 25. TICKER STYLE (Type that speaks)
// Declaration repeated as faint diagonal-free horizontal stripes across top half (6-10% opacity), paper card below
// ---------------------------------------------------------------------------
object TickerStyle : BaseStyle("ticker", "Ticker", category = "Type", supportsLights = true, maxChars = 100) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val hanken = StyleTypefaces.getHankenGrotesk()
        val repeatText = (spec.declaration.uppercase() + "  •  ").repeat(4)

        // Faint horizontal ticker lines in top half
        val tickerPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 20f * s
            color = light.text.copy(alpha = 0.08f).toArgb()
            letterSpacing = 0.08f
        }
        val topLimit = if (geom.isLandscapeX) geom.height * 0.40f else geom.height * 0.48f
        var ty = geom.topSafe + 10f * s
        while (ty < topLimit) {
            canvas.drawText(repeatText, -geom.width * 0.1f, ty, tickerPaint)
            ty += 32f * s
        }

        // Clean paper card below
        val cardMargin = geom.margin
        val cardTop = topLimit + 12f * s
        val cardW = geom.width - cardMargin * 2f
        val cardH = geom.bottomSafe - cardTop

        val cardBg = if (light.dark) AndroidColor.argb(235, 30, 40, 38) else AndroidColor.argb(240, 253, 250, 243)
        val cardPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = cardBg }
        val borderPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.text.copy(alpha = 0.15f).toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1.5f * s
        }
        val cardRect = RectF(cardMargin, cardTop, cardMargin + cardW, cardTop + cardH)
        canvas.drawRoundRect(cardRect, 18f * s, 18f * s, cardPaint)
        canvas.drawRoundRect(cardRect, 18f * s, 18f * s, borderPaint)

        // Content inside card
        val pad = 20f * s
        var curY = cardTop + pad + 8f * s
        val contentW = (cardW - pad * 2f).toInt()

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 30f * s, 18f * s, contentW, cardH * 0.42f, light.text.toArgb())
        drawTextLayout(canvas, dl, cardMargin + pad, curY)
        curY += dl.height + 12f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(cardMargin + pad, curY, cardMargin + pad + 40f * s, curY + 2f * s, rulePaint)
        curY += 12f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 17f * s, 13f * s, contentW, cardH * 0.28f, light.text.copy(alpha = 0.85f).toArgb())
            drawTextLayout(canvas, vl, cardMargin + pad, curY)
            curY += vl.height + 8f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, contentW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, cardMargin + pad, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 26. SPLIT WEIGHT STYLE (Type that speaks)
// "I am" in light italic, key word in heavy caps, rest light. Never wider than 88%
// ---------------------------------------------------------------------------
object SplitWeightStyle : BaseStyle("split_weight", "Split weight", category = "Type", supportsLights = true, maxChars = 80) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val maxW = (geom.width * 0.88f).toInt()
        val leftX = (geom.width - maxW) / 2f

        val newsreader = StyleTypefaces.getNewsreader()
        val newsreaderBold = runCatching { Typeface.create(newsreader, Typeface.BOLD) }.getOrNull()
        val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()

        var curY = geom.topSafe + 36f * s

        // Hero split layout
        val (dl, _) = autoShrinkLayout(spec.declaration, newsreaderBold, 38f * s, 20f * s, maxW, geom.height * 0.38f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 48f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", newsreaderItalic, 19f * s, 13f * s, maxW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 13f * s, 10f * s, maxW, 28f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 27. SPINE STYLE (Type that speaks)
// Reference runs vertically down left edge like a book spine, declaration to its right
// ---------------------------------------------------------------------------
object SpineStyle : BaseStyle("spine", "Spine", category = "Type", supportsLights = true, maxChars = 110) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val hanken = StyleTypefaces.getHankenGrotesk()
        val spineX = geom.margin + 26f * s
        val textStartX = spineX + 22f * s
        val availW = (geom.width - textStartX - geom.margin).toInt().coerceAtLeast(100)

        // Spine vertical dividing line
        val linePaint = AndroidPaint().apply {
            color = light.text.copy(alpha = 0.22f).toArgb()
            strokeWidth = 1.5f * s
        }
        canvas.drawLine(spineX, geom.topSafe, spineX, geom.bottomSafe, linePaint)

        // Vertical reference down spine
        val ref = (spec.reference ?: "MAKARIOS").uppercase()
        val spinePaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = light.text.copy(alpha = 0.60f).toArgb()
            letterSpacing = 0.16f
        }
        canvas.save()
        canvas.translate(spineX - 10f * s, geom.topSafe + 160f * s)
        canvas.rotate(-90f)
        canvas.drawText(ref, 0f, 0f, spinePaint)
        canvas.restore()

        // Right area: declaration and scripture
        var curY = geom.topSafe + 28f * s
        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 34f * s, 18f * s, availW, geom.height * 0.40f, light.text.toArgb())
        drawTextLayout(canvas, dl, textStartX, curY)
        curY += dl.height + 14f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(textStartX, curY, textStartX + 40f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.25f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, textStartX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 28. POEM STYLE (Type that speaks)
// Centred lines broken at natural phrases, a small star between stanzas
// ---------------------------------------------------------------------------
object PoemStyle : BaseStyle("poem", "Poem", category = "Type", supportsLights = true, maxChars = 130) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val centerW = (geom.width * 0.84f).toInt()
        val leftX = (geom.width - centerW) / 2f
        var curY = geom.topSafe + 36f * s

        // Center aligned poem lines in Newsreader Italic
        val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()
        val (dl, _) = autoShrinkLayout(
            spec.declaration, newsreaderItalic, 32f * s, 18f * s, centerW, geom.height * 0.38f,
            light.text.toArgb(), alignment = Layout.Alignment.ALIGN_CENTER, lineSpacingMultiplier = 1.30f
        )
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        // Star between stanzas (✦)
        val sunColor = if (light.dark) AndroidColor.parseColor("#E8A860") else AndroidColor.parseColor("#B5532B")
        val starPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f * s
            color = sunColor
            textAlign = AndroidPaint.Align.CENTER
        }
        canvas.drawText("✦", geom.width / 2f, curY + 12f * s, starPaint)
        curY += 28f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout(
                "“$verseText”", newsreaderItalic, 18f * s, 13f * s, centerW, geom.height * 0.22f,
                light.text.copy(alpha = 0.80f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER
            )
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(
                ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s,
                light.text.copy(alpha = 0.60f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER
            )
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 29. MARKER STYLE (Type that speaks)
// Key words get a highlighter stroke (sun colour at 35%, bottom 45% of line height)
// ---------------------------------------------------------------------------
object MarkerStyle : BaseStyle("marker", "Marker", category = "Type", supportsLights = true, maxChars = 95) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        val startY = geom.topSafe + 28f * s

        val (dl, chosenSize) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 34f * s, 18f * s, availW, geom.height * 0.40f, light.text.toArgb())

        // Draw translucent highlighter streak behind bottom 45% of first 2 lines
        val sunColor = if (light.dark) AndroidColor.argb(90, 232, 168, 96) else AndroidColor.argb(90, 181, 83, 43)
        val highlightPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = sunColor }

        for (i in 0 until min(dl.lineCount, 2)) {
            val lineTop = startY + dl.getLineTop(i)
            val lineBottom = startY + dl.getLineBottom(i)
            val h = lineBottom - lineTop
            val highlightTop = lineTop + h * 0.55f
            val lineLeft = leftX + dl.getLineLeft(i)
            val lineRight = leftX + dl.getLineRight(i)
            canvas.drawRoundRect(RectF(lineLeft - 4f * s, highlightTop, lineRight + 6f * s, lineBottom + 2f * s), 4f * s, 4f * s, highlightPaint)
        }

        drawTextLayout(canvas, dl, leftX, startY)

        var curY = startY + dl.height + 16f * s
        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 30. DOORWAY STYLE (Type that speaks)
// The logo arch, big, holds a small sunrise scene (clipped to the arch), one line beneath
// ---------------------------------------------------------------------------
object DoorwayStyle : BaseStyle("doorway", "Doorway", category = "Type", supportsLights = true, maxChars = 85) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        // Plain quiet background
        scope.drawRect(color = light.bottom, size = Size(geom.width, geom.height))
        scope.drawGrainOverlay(light)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val archW = if (geom.isLandscapeX) 120f * s else 160f * s
        val archH = archW * 1.35f
        val archLeft = (geom.width - archW) / 2f
        val archTop = if (geom.isLandscapeX) geom.topSafe + 10f * s else geom.topSafe + 24f * s

        // Open doorway arch path
        val archPath = AndroidPath().apply {
            moveTo(archLeft, archTop + archH)
            lineTo(archLeft, archTop + archW * 0.5f)
            arcTo(RectF(archLeft, archTop, archLeft + archW, archTop + archW), 180f, 180f)
            lineTo(archLeft + archW, archTop + archH)
            close()
        }

        // Draw miniature sunrise scene clipped to arch
        canvas.save()
        canvas.clipPath(archPath)
        val skyPaint = AndroidPaint().apply {
            shader = LinearGradient(archLeft, archTop, archLeft, archTop + archH, light.top.toArgb(), light.bottom.toArgb(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(archLeft, archTop, archLeft + archW, archTop + archH, skyPaint)

        // Sun disc inside arch
        val sunColor = if (light.dark) AndroidColor.parseColor("#E8A860") else AndroidColor.parseColor("#B5532B")
        val sunPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = sunColor }
        canvas.drawCircle(archLeft + archW * 0.5f, archTop + archH * 0.65f, archW * 0.22f, sunPaint)

        // Mountains inside arch
        val ridgePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = light.ridge1.toArgb() }
        val ridgePath = AndroidPath().apply {
            moveTo(archLeft, archTop + archH)
            lineTo(archLeft, archTop + archH * 0.72f)
            quadTo(archLeft + archW * 0.4f, archTop + archH * 0.60f, archLeft + archW, archTop + archH * 0.75f)
            lineTo(archLeft + archW, archTop + archH)
            close()
        }
        canvas.drawPath(ridgePath, ridgePaint)
        canvas.restore()

        // Arch outline
        val archOutlinePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.text.toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2.5f * s
        }
        canvas.drawPath(archPath, archOutlinePaint)

        // Ground line under arch
        canvas.drawLine(archLeft - 16f * s, archTop + archH, archLeft + archW + 16f * s, archTop + archH, archOutlinePaint)

        // Declaration directly beneath arch
        val centerW = (geom.width * 0.84f).toInt()
        val textLeft = (geom.width - centerW) / 2f
        var curY = archTop + archH + 24f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 30f * s, 18f * s, centerW, geom.height * 0.28f, light.text.toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
        drawTextLayout(canvas, dl, textLeft, curY)
        curY += dl.height + 10f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s, light.text.copy(alpha = 0.65f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, rl, textLeft, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 31. HORIZON STYLE (Light and scene)
// One hairline across the frame, a half sun sitting on it, the declaration above
// ---------------------------------------------------------------------------
object HorizonStyle : BaseStyle("horizon", "Horizon", category = "Light", supportsLights = true, maxChars = 90) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawRect(color = light.bottom, size = Size(geom.width, geom.height))
        scope.drawGrainOverlay(light)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val horizonY = if (geom.isLandscapeX) geom.height * 0.52f else geom.height * 0.58f

        // Half sun sitting on horizon line
        val sunColor = if (light.dark) AndroidColor.parseColor("#E8A860") else AndroidColor.parseColor("#B5532B")
        val sunRadius = 40f * s
        val sunPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = sunColor }
        val sunPath = AndroidPath().apply {
            moveTo(geom.width / 2f - sunRadius, horizonY)
            arcTo(RectF(geom.width / 2f - sunRadius, horizonY - sunRadius, geom.width / 2f + sunRadius, horizonY + sunRadius), 180f, 180f)
            close()
        }
        canvas.drawPath(sunPath, sunPaint)

        // Horizon hairline across frame
        val linePaint = AndroidPaint().apply {
            color = light.anchorRule.toArgb()
            strokeWidth = 1.5f * s
        }
        canvas.drawLine(0f, horizonY, geom.width, horizonY, linePaint)

        // Declaration above horizon in sky
        val centerW = (geom.width * 0.84f).toInt()
        val leftX = (geom.width - centerW) / 2f
        val maxSkyH = horizonY - sunRadius - geom.topSafe - 16f * s
        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 34f * s, 18f * s, centerW, maxSkyH, light.text.toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
        drawTextLayout(canvas, dl, leftX, horizonY - sunRadius - dl.height - 14f * s)

        // Verse and reference below horizon
        var curY = horizonY + 28f * s
        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, centerW, geom.bottomSafe - curY, light.text.copy(alpha = 0.82f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s, light.text.copy(alpha = 0.65f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 32. ECLIPSE STYLE (Light and scene - Night only)
// A dark disc with a thin ring and soft corona (Night only), the line below
// ---------------------------------------------------------------------------
object EclipseStyle : BaseStyle("eclipse", "Eclipse", category = "Light", supportsLights = true, maxChars = 80) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val nightLight = if (light.dark) light else Light.Night
        scope.drawRect(color = nightLight.bottom, size = Size(geom.width, geom.height))
        scope.drawGrainOverlay(nightLight)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cx = geom.width / 2f
        val cy = if (geom.isLandscapeX) geom.height * 0.42f else geom.height * 0.38f
        val discR = 54f * s

        // Soft corona glow halo behind ring
        val coronaPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, discR * 2.2f, AndroidColor.argb(140, 243, 230, 200), AndroidColor.TRANSPARENT, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, discR * 2.2f, coronaPaint)

        // Dark disc core
        val darkCorePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#0F1716") }
        canvas.drawCircle(cx, cy, discR, darkCorePaint)

        // Thin golden ring
        val ringPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#E8A860")
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2.5f * s
        }
        canvas.drawCircle(cx, cy, discR, ringPaint)

        // Declaration below
        val centerW = (geom.width * 0.84f).toInt()
        val leftX = (geom.width - centerW) / 2f
        var curY = cy + discR + 32f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 32f * s, 18f * s, centerW, geom.height * 0.30f, Cream.toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 12f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s, Cream.copy(alpha = 0.65f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 33. MOON STYLE (Light and scene - Night only)
// A textured moon with two soft craters (Night only), an italic night line below
// ---------------------------------------------------------------------------
object MoonStyle : BaseStyle("moon", "Moon", category = "Light", supportsLights = true, maxChars = 85) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val nightLight = if (light.dark) light else Light.Night
        scope.drawRect(color = nightLight.bottom, size = Size(geom.width, geom.height))
        scope.drawGrainOverlay(nightLight)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cx = geom.width / 2f
        val cy = if (geom.isLandscapeX) geom.height * 0.40f else geom.height * 0.38f
        val moonR = 50f * s

        // Moon disc
        val moonPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FFF4D6") }
        canvas.drawCircle(cx, cy, moonR, moonPaint)

        // Soft craters
        val craterPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(35, 120, 110, 90) }
        canvas.drawCircle(cx - moonR * 0.32f, cy - moonR * 0.20f, moonR * 0.28f, craterPaint)
        canvas.drawCircle(cx + moonR * 0.25f, cy + moonR * 0.25f, moonR * 0.20f, craterPaint)

        // Subtle night shadow curve
        val shadowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(25, 0, 0, 0) }
        canvas.drawCircle(cx + moonR * 0.15f, cy, moonR, shadowPaint)

        // Declaration below in Newsreader Italic
        val centerW = (geom.width * 0.84f).toInt()
        val leftX = (geom.width - centerW) / 2f
        var curY = cy + moonR + 32f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreaderItalic(), 32f * s, 18f * s, centerW, geom.height * 0.32f, Cream.toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 12f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s, Cream.copy(alpha = 0.65f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 34. DUNES STYLE (Light and scene)
// The sky is the page, the ridges are the footer, large declaration at the top
// ---------------------------------------------------------------------------
object DunesStyle : BaseStyle("dunes", "Dunes", category = "Light", supportsLights = true, maxChars = 90) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        scope.drawRect(color = light.top, size = Size(w, h))

        // Dunes ridges at footer bottom 30%
        val ridge1Y = h * 0.72f
        val ridge2Y = h * 0.84f
        scope.drawRidgePath(w, h, ridge1Y, 32f * geom.scale, 42, light.ridge1)
        scope.drawRidgePath(w, h, ridge2Y, 24f * geom.scale, 77, light.ridge2)
        scope.drawGrainOverlay(light)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 28f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 36f * s, 20f * s, availW, geom.height * 0.35f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.20f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 35. RAIN STYLE (Light and scene)
// Fine diagonal rain lines (seeded) over the Rain light
// ---------------------------------------------------------------------------
object RainStyle : BaseStyle("rain", "Rain", category = "Light", supportsLights = true, maxChars = 95) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val rainLight = if (light == Light.Rain || light == Light.Mist) light else Light.Rain
        scope.drawLight(rainLight, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val rng = Random(spec.seed.hashCode())
        val rainPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.text.copy(alpha = 0.14f).toArgb()
            strokeWidth = 1.2f * s
        }

        // Draw fine diagonal rain lines across canvas
        val count = 90
        val angleRad = 20.0 * Math.PI / 180.0
        val dx = (32f * s * cos(angleRad)).toFloat()
        val dy = (32f * s * sin(angleRad)).toFloat()
        for (i in 0 until count) {
            val rx = rng.nextFloat() * geom.width
            val ry = rng.nextFloat() * geom.height
            canvas.drawLine(rx, ry, rx + dx, ry + dy, rainPaint)
        }

        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 34f, verseSizeSp = 17f)
    }
}

// ---------------------------------------------------------------------------
// 36. FIREFLIES STYLE (Light and scene)
// 12 to 16 glowing points (seeded, clear of text box) over Grove
// ---------------------------------------------------------------------------
object FirefliesStyle : BaseStyle("fireflies", "Fireflies", category = "Light", supportsLights = true, maxChars = 85) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val groveLight = if (light.dark) light else Light.Grove
        scope.drawLight(groveLight, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val textBounds = measurePairingBounds(geom, light, spec)
        val rng = Random(spec.seed.hashCode() xor 0x51E2)

        // Draw 14 glowing firefly points clear of text box
        val fireflyCorePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FFF3A8") }
        val fireflyGlowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(80, 255, 243, 168)
        }

        for (i in 0 until 14) {
            val fx = rng.nextFloat() * (geom.width - geom.margin * 2f) + geom.margin
            val fy = rng.nextFloat() * (geom.bottomSafe - geom.topSafe) + geom.topSafe
            val inText = textBounds.contains(fx, fy)
            if (!inText) {
                canvas.drawCircle(fx, fy, 10f * s, fireflyGlowPaint)
                canvas.drawCircle(fx, fy, 2.5f * s, fireflyCorePaint)
            }
        }

        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 34f, verseSizeSp = 17f)
    }
}

// ---------------------------------------------------------------------------
// 37. CANDLE STYLE (Light and scene - Night only)
// One small flame with a soft glow, a thin stick, a quiet line below (Night only)
// ---------------------------------------------------------------------------
object CandleStyle : BaseStyle("candle", "Candle", category = "Light", supportsLights = true, maxChars = 75) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val nightLight = if (light.dark) light else Light.Night
        scope.drawRect(color = nightLight.bottom, size = Size(geom.width, geom.height))
        scope.drawGrainOverlay(nightLight)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cx = geom.width / 2f
        val cy = if (geom.isLandscapeX) geom.height * 0.38f else geom.height * 0.35f

        // Flame soft aura
        val flameAuraPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, 48f * s, AndroidColor.argb(120, 255, 217, 140), AndroidColor.TRANSPARENT, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, 48f * s, flameAuraPaint)

        // Flame teardrop path
        val flamePath = AndroidPath().apply {
            moveTo(cx, cy - 24f * s)
            quadTo(cx + 12f * s, cy - 6f * s, cx + 8f * s, cy + 12f * s)
            quadTo(cx, cy + 18f * s, cx - 8f * s, cy + 12f * s)
            quadTo(cx - 12f * s, cy - 6f * s, cx, cy - 24f * s)
            close()
        }
        val flamePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(cx, cy - 24f * s, cx, cy + 18f * s, AndroidColor.parseColor("#FFFBE8"), AndroidColor.parseColor("#E88C30"), Shader.TileMode.CLAMP)
        }
        canvas.drawPath(flamePath, flamePaint)

        // Candle wick and stick
        val stickPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#D4CEBA")
        }
        canvas.drawRect(cx - 4f * s, cy + 20f * s, cx + 4f * s, cy + 60f * s, stickPaint)

        // Quiet declaration line below
        val centerW = (geom.width * 0.84f).toInt()
        val leftX = (geom.width - centerW) / 2f
        var curY = cy + 85f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreaderItalic(), 30f * s, 18f * s, centerW, geom.height * 0.32f, Cream.toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 12f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s, Cream.copy(alpha = 0.65f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 38. INDEX CARD STYLE (Paper and objects - Fixed paper colours)
// Off-white card, blue ruled lines, red margin line, declaration in italic. Backdrop is chosen Light.
// ---------------------------------------------------------------------------
object IndexCardStyle : BaseStyle("index_card", "Index card", category = "Paper", supportsLights = false, maxChars = 110) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        // Backdrop in chosen light
        scope.drawRect(color = light.bottom, size = Size(geom.width, geom.height))
        scope.drawGrainOverlay(light)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cardMargin = geom.margin * 0.8f
        val cardW = geom.width - cardMargin * 2f
        val cardTop = geom.topSafe + 16f * s
        val cardH = geom.bottomSafe - cardTop

        // Off-white card (#FFFDF8) with drop shadow
        val shadowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(45, 0, 0, 0) }
        canvas.drawRoundRect(RectF(cardMargin + 2f * s, cardTop + 4f * s, cardMargin + cardW + 2f * s, cardTop + cardH + 4f * s), 16f * s, 16f * s, shadowPaint)

        val cardPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FFFDF8") }
        val cardRect = RectF(cardMargin, cardTop, cardMargin + cardW, cardTop + cardH)
        canvas.drawRoundRect(cardRect, 16f * s, 16f * s, cardPaint)

        // Blue ruled lines across card
        val blueLinePaint = AndroidPaint().apply {
            color = AndroidColor.argb(60, 140, 180, 215)
            strokeWidth = 1f * s
        }
        val lineSpacing = 32f * s
        var ly = cardTop + 48f * s
        while (ly < cardTop + cardH - 16f * s) {
            canvas.drawLine(cardMargin, ly, cardMargin + cardW, ly, blueLinePaint)
            ly += lineSpacing
        }

        // Red margin line on left
        val redMarginX = cardMargin + 48f * s
        val redLinePaint = AndroidPaint().apply {
            color = AndroidColor.argb(120, 215, 80, 75)
            strokeWidth = 1.5f * s
        }
        canvas.drawLine(redMarginX, cardTop, redMarginX, cardTop + cardH, redLinePaint)

        // Text inside card to the right of red margin
        val textStartX = redMarginX + 16f * s
        val textW = (cardMargin + cardW - textStartX - 16f * s).toInt().coerceAtLeast(80)
        var curY = cardTop + 36f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreaderItalic(), 32f * s, 18f * s, textW, cardH * 0.45f, AndroidColor.parseColor("#222222"))
        drawTextLayout(canvas, dl, textStartX, curY)
        curY += dl.height + 16f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreader(), 18f * s, 13f * s, textW, cardH * 0.28f, AndroidColor.parseColor("#444444"))
            drawTextLayout(canvas, vl, textStartX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, textW, 26f * s, AndroidColor.parseColor("#888888"))
            drawTextLayout(canvas, rl, textStartX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 39. TORN PAPER STYLE (Paper and objects)
// A paper strip with a deterministic torn edge across the scene
// ---------------------------------------------------------------------------
object TornPaperStyle : BaseStyle("torn_paper", "Torn paper", category = "Paper", supportsLights = false, maxChars = 100) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val topY = geom.topSafe + 30f * s
        val bottomY = geom.bottomSafe - 30f * s
        val rng = Random(spec.seed.hashCode())

        // Torn edge paper path
        val tornPath = AndroidPath().apply {
            moveTo(0f, topY)
            var x = 0f
            while (x < geom.width) {
                val step = 14f * s
                val dy = (rng.nextFloat() - 0.5f) * 10f * s
                x += step
                lineTo(x.coerceAtMost(geom.width), topY + dy)
            }
            lineTo(geom.width, bottomY)
            x = geom.width
            while (x > 0f) {
                val step = 14f * s
                val dy = (rng.nextFloat() - 0.5f) * 10f * s
                x -= step
                lineTo(x.coerceAtLeast(0f), bottomY + dy)
            }
            close()
        }

        // Shadow and paper fill
        val shadowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(50, 0, 0, 0) }
        canvas.save()
        canvas.translate(0f, 4f * s)
        canvas.drawPath(tornPath, shadowPaint)
        canvas.restore()

        val paperPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FAF6EE") }
        canvas.drawPath(tornPath, paperPaint)

        // Text centered on paper strip
        val centerW = (geom.width * 0.82f).toInt()
        val leftX = (geom.width - centerW) / 2f
        var curY = topY + 40f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 32f * s, 18f * s, centerW, (bottomY - topY) * 0.40f, AndroidColor.parseColor("#1F1C18"))
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 14f * s

        val rulePaint = AndroidPaint().apply { color = AndroidColor.parseColor("#B5532B") }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, centerW, (bottomY - topY) * 0.25f, AndroidColor.parseColor("#4A453F"))
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s, AndroidColor.parseColor("#7A746D"))
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 40. STICKY NOTE STYLE (Paper and objects)
// Yellow note, rotated -3 degrees, soft shadow, "Don't forget:" optional for "I am"
// ---------------------------------------------------------------------------
object StickyNoteStyle : BaseStyle("sticky_note", "Sticky note", category = "Paper", supportsLights = false, maxChars = 80) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val noteSize = min(geom.width * 0.85f, geom.height * 0.55f)
        val noteLeft = (geom.width - noteSize) / 2f
        val noteTop = geom.topSafe + 30f * s

        canvas.save()
        canvas.rotate(-3f, geom.width / 2f, noteTop + noteSize / 2f)

        // Drop shadow
        val shadowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(45, 0, 0, 0) }
        canvas.drawRoundRect(RectF(noteLeft + 3f * s, noteTop + 5f * s, noteLeft + noteSize + 3f * s, noteTop + noteSize + 5f * s), 6f * s, 6f * s, shadowPaint)

        // Canary yellow note (#FFF58A)
        val notePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FFF58A") }
        canvas.drawRoundRect(RectF(noteLeft, noteTop, noteLeft + noteSize, noteTop + noteSize), 6f * s, 6f * s, notePaint)

        // Header if "I am"
        val pad = 24f * s
        var curY = noteTop + pad
        val textW = (noteSize - pad * 2f).toInt()
        val lowerDecl = spec.declaration.lowercase()
        if (lowerDecl.startsWith("i am") || lowerDecl.startsWith("i have")) {
            val headerPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                typeface = StyleTypefaces.getHankenGrotesk()
                textSize = 13f * s
                color = AndroidColor.parseColor("#807020")
            }
            canvas.drawText("Don't forget:", noteLeft + pad, curY + 12f * s, headerPaint)
            curY += 24f * s
        }

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 32f * s, 18f * s, textW, noteSize * 0.42f, AndroidColor.parseColor("#222014"))
        drawTextLayout(canvas, dl, noteLeft + pad, curY)
        curY += dl.height + 12f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, textW, 26f * s, AndroidColor.parseColor("#807020"))
            drawTextLayout(canvas, rl, noteLeft + pad, curY)
        }

        canvas.restore()
    }
}

// ---------------------------------------------------------------------------
// 41. LETTER STYLE (Paper and objects)
// "Dear you," declaration in 2nd person, verse, "With love, <reference>"
// ---------------------------------------------------------------------------
object LetterStyle : BaseStyle("letter", "Letter", category = "Paper", supportsLights = false, maxChars = 130) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cardMargin = geom.margin * 0.8f
        val cardW = geom.width - cardMargin * 2f
        val cardTop = geom.topSafe + 16f * s
        val cardH = geom.bottomSafe - cardTop

        // Parchment card
        val cardPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FAF7EF") }
        val borderPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(35, 140, 130, 110)
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1f * s
        }
        val rect = RectF(cardMargin, cardTop, cardMargin + cardW, cardTop + cardH)
        canvas.drawRoundRect(rect, 14f * s, 14f * s, cardPaint)
        canvas.drawRoundRect(rect, 14f * s, 14f * s, borderPaint)

        val pad = 24f * s
        val textW = (cardW - pad * 2f).toInt()
        var curY = cardTop + pad + 10f * s

        // "Dear you," (or recipient)
        val name = spec.recipientName?.trim()?.ifBlank { null } ?: "you"
        val salutation = "Dear $name,"
        val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()
        val salutationPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = newsreaderItalic
            textSize = 24f * s
            color = AndroidColor.parseColor("#2B2824")
        }
        canvas.drawText(salutation, cardMargin + pad, curY, salutationPaint)
        curY += 32f * s

        // Body in second person
        val bodyText = spec.declaration
        val (bl, _) = autoShrinkLayout(bodyText, newsreaderItalic, 26f * s, 16f * s, textW, cardH * 0.36f, AndroidColor.parseColor("#2B2824"))
        drawTextLayout(canvas, bl, cardMargin + pad, curY)
        curY += bl.height + 18f * s

        // Verse quote
        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreader(), 17f * s, 12f * s, textW, cardH * 0.22f, AndroidColor.parseColor("#5A544C"))
            drawTextLayout(canvas, vl, cardMargin + pad, curY)
            curY += vl.height + 20f * s
        }

        // "With love, <reference>"
        val signOff = "With love, ${spec.reference ?: "Makarios"}"
        val (sl, _) = autoShrinkLayout(signOff, newsreaderItalic, 19f * s, 13f * s, textW, 30f * s, AndroidColor.parseColor("#B5532B"))
        drawTextLayout(canvas, sl, cardMargin + pad, curY)
    }
}

// ---------------------------------------------------------------------------
// 42. BIBLE PAGE STYLE (Paper and objects)
// Real page layout with book/chapter small caps at top, verse numbers in anchor color, chosen verse highlighted
// ---------------------------------------------------------------------------
object BiblePageStyle : BaseStyle("bible_page", "Bible page", category = "Paper", supportsLights = false, maxChars = 280) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cardMargin = geom.margin * 0.75f
        val cardW = geom.width - cardMargin * 2f
        val cardTop = geom.topSafe + 12f * s
        val cardH = geom.bottomSafe - cardTop

        // Bible page ivory background (#FAF7EE)
        val pagePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FAF7EE") }
        canvas.drawRoundRect(RectF(cardMargin, cardTop, cardMargin + cardW, cardTop + cardH), 12f * s, 12f * s, pagePaint)

        val pad = 24f * s
        val textW = (cardW - pad * 2f).toInt()
        var curY = cardTop + pad

        // Header: Book & Chapter in small caps
        val ref = spec.reference ?: "COLOSSIANS 1:27"
        val headerText = ref.substringBeforeLast(":").uppercase()
        val headerPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = StyleTypefaces.getHankenGrotesk()
            textSize = 13f * s
            color = AndroidColor.parseColor("#5A544C")
            letterSpacing = 0.14f
        }
        canvas.drawText(headerText, cardMargin + pad, curY + 12f * s, headerPaint)
        curY += 24f * s

        val linePaint = AndroidPaint().apply {
            color = AndroidColor.argb(40, 90, 84, 76)
            strokeWidth = 1f * s
        }
        canvas.drawLine(cardMargin + pad, curY, cardMargin + cardW - pad, curY, linePaint)
        curY += 18f * s

        // Chosen verse highlighted with soft warm background tint
        val verseNum = ref.substringAfterLast(":", "1")
        val fullVerse = "$verseNum  ${spec.verse ?: spec.declaration}"

        val (vl, _) = autoShrinkLayout(fullVerse, StyleTypefaces.getNewsreader(), 22f * s, 14f * s, textW, cardH * 0.40f, AndroidColor.parseColor("#22201C"))

        val highlightBg = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(45, 235, 177, 149) }
        canvas.drawRoundRect(RectF(cardMargin + pad - 6f * s, curY - 4f * s, cardMargin + pad + textW + 6f * s, curY + vl.height + 6f * s), 8f * s, 8f * s, highlightBg)

        drawTextLayout(canvas, vl, cardMargin + pad, curY)
        curY += vl.height + 24f * s

        // Declaration takeaway note below
        val takeAway = "“${spec.declaration}”"
        val (tl, _) = autoShrinkLayout(takeAway, StyleTypefaces.getNewsreaderItalic(), 20f * s, 13f * s, textW, cardH * 0.25f, AndroidColor.parseColor("#B5532B"))
        drawTextLayout(canvas, tl, cardMargin + pad, curY)
    }
}

// ---------------------------------------------------------------------------
// 43. TICKET STYLE (Paper and objects)
// Boarding pass. DESTINATION (topic), GATE (book ch), SEAT (verse). Dashed tear line.
// ---------------------------------------------------------------------------
object TicketStyle : BaseStyle("ticket", "Ticket", category = "Paper", supportsLights = false, maxChars = 90) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cardMargin = geom.margin * 0.8f
        val cardW = geom.width - cardMargin * 2f
        val cardTop = geom.topSafe + 20f * s
        val cardH = geom.bottomSafe - cardTop

        // Ticket cardstock
        val ticketPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FDFBF7") }
        canvas.drawRoundRect(RectF(cardMargin, cardTop, cardMargin + cardW, cardTop + cardH), 16f * s, 16f * s, ticketPaint)

        // Tear line with notch cutouts
        val tearY = cardTop + cardH * 0.32f
        val notchR = 14f * s
        val notchPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = light.bottom.toArgb() }
        canvas.drawCircle(cardMargin, tearY, notchR, notchPaint)
        canvas.drawCircle(cardMargin + cardW, tearY, notchR, notchPaint)

        val dashPaint = AndroidPaint().apply {
            color = AndroidColor.argb(80, 0, 0, 0)
            strokeWidth = 1.5f * s
            pathEffect = DashPathEffect(floatArrayOf(8f * s, 8f * s), 0f)
        }
        canvas.drawLine(cardMargin + notchR + 4f * s, tearY, cardMargin + cardW - notchR - 4f * s, tearY, dashPaint)

        // Top stub: DESTINATION / GATE / SEAT
        val hanken = StyleTypefaces.getHankenGrotesk()
        val lblPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 10f * s
            color = AndroidColor.parseColor("#888888")
            letterSpacing = 0.12f
        }
        val valPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 15f * s
            color = AndroidColor.parseColor("#222222")
            letterSpacing = 0.05f
        }

        val ref = spec.reference ?: "COL 1:27"
        val gate = ref.substringBeforeLast(":").uppercase()
        val seat = ref.substringAfterLast(":", "1")

        val col1X = cardMargin + 24f * s
        val col2X = cardMargin + cardW * 0.50f
        val col3X = cardMargin + cardW * 0.78f

        canvas.drawText("DESTINATION", col1X, cardTop + 28f * s, lblPaint)
        canvas.drawText("PEACE & LIFE", col1X, cardTop + 48f * s, valPaint)

        canvas.drawText("GATE", col2X, cardTop + 28f * s, lblPaint)
        canvas.drawText(gate.take(10), col2X, cardTop + 48f * s, valPaint)

        canvas.drawText("SEAT", col3X, cardTop + 28f * s, lblPaint)
        canvas.drawText(seat, col3X, cardTop + 48f * s, valPaint)

        // Main body below tear line
        val pad = 24f * s
        val textW = (cardW - pad * 2f).toInt()
        var curY = tearY + 28f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 32f * s, 18f * s, textW, cardH * 0.35f, AndroidColor.parseColor("#1A1A1A"))
        drawTextLayout(canvas, dl, cardMargin + pad, curY)
        curY += dl.height + 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 17f * s, 12f * s, textW, cardH * 0.20f, AndroidColor.parseColor("#4A453F"))
            drawTextLayout(canvas, vl, cardMargin + pad, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 44. SEAL STYLE (Paper and objects)
// Wax seal (radial gradient disc, ring of dots) carrying doorway mark, line under it. Footer removed.
// ---------------------------------------------------------------------------
object SealStyle : BaseStyle("seal", "Seal", category = "Paper", supportsLights = false, maxChars = 80) {
    override val showWordmark: Boolean get() = false

    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val cx = geom.width / 2f
        val cy = if (geom.isLandscapeX) geom.height * 0.38f else geom.height * 0.36f
        val sealR = 52f * s

        // Wax seal body (#992222 crimson radial gradient)
        val sealPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, sealR, AndroidColor.parseColor("#BA2A24"), AndroidColor.parseColor("#7A1410"), Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, sealR, sealPaint)

        // Embossed ring of dots around border
        val dotPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(160, 255, 210, 200) }
        val dotCount = 18
        for (i in 0 until dotCount) {
            val ang = i * (2.0 * Math.PI / dotCount)
            val dx = (cx + (sealR - 6f * s) * cos(ang)).toFloat()
            val dy = (cy + (sealR - 6f * s) * sin(ang)).toFloat()
            canvas.drawCircle(dx, dy, 2f * s, dotPaint)
        }

        // Embossed doorway mark inside seal
        val stampPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(210, 255, 230, 220)
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2.5f * s
        }
        val archW = 28f * s
        val archH = 34f * s
        val ax = cx - archW / 2f
        val ay = cy - archH / 2f
        val archPath = AndroidPath().apply {
            moveTo(ax, ay + archH)
            lineTo(ax, ay + archW * 0.5f)
            arcTo(RectF(ax, ay, ax + archW, ay + archW), 180f, 180f)
            lineTo(ax + archW, ay + archH)
        }
        canvas.drawPath(archPath, stampPaint)

        // Declaration centered below seal
        val centerW = (geom.width * 0.84f).toInt()
        val leftX = (geom.width - centerW) / 2f
        var curY = cy + sealR + 32f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 32f * s, 18f * s, centerW, geom.height * 0.32f, light.text.toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 12f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, centerW, 26f * s, light.text.copy(alpha = 0.65f).toArgb(), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 45. BOOKMARK STYLE (Paper and objects)
// Tall narrow strip centred with tassel. Horizontal ribbon on Square/X.
// ---------------------------------------------------------------------------
object BookmarkStyle : BaseStyle("bookmark", "Bookmark", category = "Paper", supportsLights = false, maxChars = 100) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        if (geom.isLandscapeX || geom.isSquare) {
            // Horizontal ribbon strip for wide formats
            val stripH = geom.height * 0.50f
            val stripTop = (geom.height - stripH) / 2f
            val ribbonPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FFFDF8") }
            canvas.drawRect(0f, stripTop, geom.width, stripTop + stripH, ribbonPaint)

            val borderPaint = AndroidPaint().apply {
                color = AndroidColor.parseColor("#B5532B")
                strokeWidth = 2f * s
            }
            canvas.drawLine(0f, stripTop, geom.width, stripTop, borderPaint)
            canvas.drawLine(0f, stripTop + stripH, geom.width, stripTop + stripH, borderPaint)

            val textW = (geom.width * 0.80f).toInt()
            val leftX = (geom.width - textW) / 2f
            var curY = stripTop + 24f * s
            val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 32f * s, 18f * s, textW, stripH * 0.55f, AndroidColor.parseColor("#222222"), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, dl, leftX, curY)
            curY += dl.height + 10f * s

            val ref = (spec.reference ?: "").uppercase()
            if (ref.isNotBlank()) {
                val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, textW, 26f * s, AndroidColor.parseColor("#888888"), alignment = Layout.Alignment.ALIGN_CENTER)
                drawTextLayout(canvas, rl, leftX, curY)
            }
        } else {
            // Vertical bookmark with tassel on Story/Portrait
            val bkW = geom.width * 0.58f
            val bkLeft = (geom.width - bkW) / 2f
            val bkTop = geom.topSafe + 36f * s
            val bkH = geom.bottomSafe - bkTop

            // Tassel cord at top
            val tasselPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.parseColor("#B5532B")
                strokeWidth = 2f * s
            }
            val cx = geom.width / 2f
            canvas.drawLine(cx, bkTop - 30f * s, cx, bkTop, tasselPaint)
            canvas.drawCircle(cx, bkTop - 32f * s, 4f * s, tasselPaint)

            // Bookmark paper (#FFFDF8)
            val bkPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FFFDF8") }
            val shadowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(40, 0, 0, 0) }
            canvas.drawRoundRect(RectF(bkLeft + 2f * s, bkTop + 4f * s, bkLeft + bkW + 2f * s, bkTop + bkH + 4f * s), 12f * s, 12f * s, shadowPaint)
            canvas.drawRoundRect(RectF(bkLeft, bkTop, bkLeft + bkW, bkTop + bkH), 12f * s, 12f * s, bkPaint)

            val pad = 18f * s
            val textW = (bkW - pad * 2f).toInt()
            var curY = bkTop + 28f * s

            val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 28f * s, 16f * s, textW, bkH * 0.45f, AndroidColor.parseColor("#1F1C18"), alignment = Layout.Alignment.ALIGN_CENTER)
            drawTextLayout(canvas, dl, bkLeft + pad, curY)
            curY += dl.height + 14f * s

            val verseText = spec.verse.orEmpty()
            if (verseText.isNotBlank()) {
                val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 16f * s, 12f * s, textW, bkH * 0.28f, AndroidColor.parseColor("#5A544C"), alignment = Layout.Alignment.ALIGN_CENTER)
                drawTextLayout(canvas, vl, bkLeft + pad, curY)
                curY += vl.height + 10f * s
            }
            val ref = (spec.reference ?: "").uppercase()
            if (ref.isNotBlank()) {
                val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 11f * s, 9f * s, textW, 24f * s, AndroidColor.parseColor("#888888"), alignment = Layout.Alignment.ALIGN_CENTER)
                drawTextLayout(canvas, rl, bkLeft + pad, curY)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 46. BIRTHDAY STYLE (Occasions)
// "Happy birthday, <name>", the declaration or verse over their year
// ---------------------------------------------------------------------------
object BirthdayStyle : BaseStyle("birthday", "Birthday", category = "Occasion", supportsLights = true, maxChars = 95) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 24f * s

        // Eyebrow "HAPPY BIRTHDAY, <NAME>"
        val name = spec.recipientName?.trim()?.ifBlank { null } ?: "FRIEND"
        val headerText = "HAPPY BIRTHDAY, $name".uppercase()
        val hanken = StyleTypefaces.getHankenGrotesk()
        val headerPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 13f * s
            color = (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb()
            letterSpacing = 0.14f
        }
        canvas.drawText(headerText, leftX, curY + 12f * s, headerPaint)
        curY += 28f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 36f * s, 20f * s, availW, geom.height * 0.38f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 47. PRAYING FOR YOU STYLE (Occasions)
// Eyebrow "PRAYING FOR YOU", a blessing in large type
// ---------------------------------------------------------------------------
object PrayingForYouStyle : BaseStyle("praying_for_you", "Praying for you", category = "Occasion", supportsLights = true, maxChars = 105) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 28f * s

        // Eyebrow
        val hanken = StyleTypefaces.getHankenGrotesk()
        val eyebrowPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb()
            letterSpacing = 0.16f
        }
        canvas.drawText("PRAYING FOR YOU", leftX, curY + 10f * s, eyebrowPaint)
        curY += 26f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 36f * s, 20f * s, availW, geom.height * 0.38f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 48. GOOD MORNING STYLE (Occasions)
// Two big words, one italic line beneath
// ---------------------------------------------------------------------------
object GoodMorningStyle : BaseStyle("good_morning", "Good morning", category = "Occasion", supportsLights = true, maxChars = 75) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val dawnLight = if (light.dark) light else Light.Dawn
        scope.drawLight(dawnLight, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 28f * s

        // Big display header
        val headerPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = runCatching { Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD) }.getOrNull()
            textSize = 52f * s
            color = light.text.toArgb()
            letterSpacing = -0.02f
        }
        canvas.drawText("Good", leftX, curY + 48f * s, headerPaint)
        curY += 54f * s
        canvas.drawText("morning.", leftX, curY + 48f * s, headerPaint)
        curY += 68f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 16f * s

        // One italic line beneath
        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreaderItalic(), 24f * s, 15f * s, availW, geom.height * 0.28f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 12f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 49. DAY OF THE WEEK STYLE (Occasions)
// The weekday as hero ("Rest." on Sunday, "Begin." on Monday, "Press on." on Wednesday...), declaration beneath
// ---------------------------------------------------------------------------
object DayOfTheWeekStyle : BaseStyle("day_of_the_week", "Day of the week", category = "Occasion", supportsLights = true, maxChars = 85) {
    private val weekdayHeroes = mapOf(
        "SUNDAY" to "Rest.",
        "MONDAY" to "Begin.",
        "TUESDAY" to "Stand.",
        "WEDNESDAY" to "Press on.",
        "THURSDAY" to "Trust.",
        "FRIDAY" to "Finish.",
        "SATURDAY" to "Reflect."
    )

    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 24f * s

        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase()
        val heroWord = weekdayHeroes[today] ?: "Stand."

        val hanken = StyleTypefaces.getHankenGrotesk()
        val dayLabelPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 13f * s
            color = (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb()
            letterSpacing = 0.16f
        }
        canvas.drawText(today, leftX, curY + 12f * s, dayLabelPaint)
        curY += 24f * s

        val heroPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = runCatching { Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD) }.getOrNull()
            textSize = 56f * s
            color = light.text.toArgb()
        }
        canvas.drawText(heroWord, leftX, curY + 52f * s, heroPaint)
        curY += 72f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 28f * s, 17f * s, availW, geom.height * 0.32f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 14f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 50. ANSWERED STYLE (Occasions)
// A rotated rounded-rectangle stamp "ANSWERED", the declaration beneath
// ---------------------------------------------------------------------------
object AnsweredStyle : BaseStyle("answered", "Answered", category = "Occasion", supportsLights = true, maxChars = 85) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        val startY = geom.topSafe + 28f * s

        // Rotated stamp "ANSWERED"
        val stampW = 160f * s
        val stampH = 46f * s
        val stampColor = if (light.dark) AndroidColor.parseColor("#E8A860") else AndroidColor.parseColor("#B5532B")
        val stampPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = stampColor
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2.5f * s
        }

        canvas.save()
        canvas.rotate(-7f, leftX + stampW / 2f, startY + stampH / 2f)
        canvas.drawRoundRect(RectF(leftX, startY, leftX + stampW, startY + stampH), 8f * s, 8f * s, stampPaint)

        val stampTextPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = StyleTypefaces.getHankenGrotesk()
            textSize = 20f * s
            color = stampColor
            letterSpacing = 0.18f
            textAlign = AndroidPaint.Align.CENTER
        }
        canvas.drawText("ANSWERED", leftX + stampW / 2f, startY + 31f * s, stampTextPaint)
        canvas.restore()

        var curY = startY + stampH + 34f * s
        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 34f * s, 18f * s, availW, geom.height * 0.38f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, StyleTypefaces.getHankenGrotesk(), 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 51. SEASON STYLE (Occasions)
// Eyebrow "THIS SEASON I DECLARE", the declaration large
// ---------------------------------------------------------------------------
object SeasonStyle : BaseStyle("season", "Season", category = "Occasion", supportsLights = true, maxChars = 95) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 28f * s

        val eyebrowText = "THIS SEASON I DECLARE"
        val hanken = StyleTypefaces.getHankenGrotesk()
        val eyebrowPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb()
            letterSpacing = 0.16f
        }
        canvas.drawText(eyebrowText, leftX, curY + 10f * s, eyebrowPaint)
        curY += 28f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 36f * s, 20f * s, availW, geom.height * 0.38f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 52. CHAT BUBBLE STYLE (Conversation and series)
// Looks like a message from "Makarios" with the verse, plus a small reply bubble. Footer removed.
// ---------------------------------------------------------------------------
object ChatBubbleStyle : BaseStyle("chat_bubble", "Chat bubble", category = "Conversation", supportsLights = true, maxChars = 90) {
    override val showWordmark: Boolean get() = false

    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawRect(color = light.bottom, size = Size(geom.width, geom.height))
        scope.drawGrainOverlay(light)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val maxBubbleW = (geom.width * 0.78f).toInt()
        var curY = geom.topSafe + 36f * s

        // 1. Incoming message bubble from "Makarios"
        val senderPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = StyleTypefaces.getHankenGrotesk()
            textSize = 12f * s
            color = light.text.copy(alpha = 0.65f).toArgb()
        }
        canvas.drawText("Makarios", leftX + 6f * s, curY + 10f * s, senderPaint)
        curY += 20f * s

        val incomingText = spec.verse ?: spec.declaration
        val (il, _) = autoShrinkLayout(incomingText, StyleTypefaces.getNewsreader(), 22f * s, 14f * s, maxBubbleW - (32f * s).toInt(), geom.height * 0.30f, light.text.toArgb())

        val inBubbleW = il.width + 36f * s
        val inBubbleH = il.height + 26f * s
        val inBubbleBg = if (light.dark) AndroidColor.argb(220, 35, 48, 46) else AndroidColor.argb(240, 245, 238, 226)
        val inPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = inBubbleBg }
        canvas.drawRoundRect(RectF(leftX, curY, leftX + inBubbleW, curY + inBubbleH), 18f * s, 18f * s, inPaint)

        drawTextLayout(canvas, il, leftX + 18f * s, curY + 13f * s)
        curY += inBubbleH + 28f * s

        // 2. Outgoing reply bubble (right-aligned)
        val reply = spec.replyText?.trim()?.ifBlank { null } ?: spec.declaration
        val (ol, _) = autoShrinkLayout(reply, StyleTypefaces.getNewsreader(), 24f * s, 15f * s, maxBubbleW - (32f * s).toInt(), geom.height * 0.28f, Cream.toArgb())

        val outBubbleW = ol.width + 36f * s
        val outBubbleH = ol.height + 26f * s
        val outLeft = geom.width - geom.margin - outBubbleW
        val outBg = if (light.dark) AndroidColor.parseColor("#E8A860") else AndroidColor.parseColor("#B5532B")
        val outPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = outBg }
        canvas.drawRoundRect(RectF(outLeft, curY, outLeft + outBubbleW, curY + outBubbleH), 18f * s, 18f * s, outPaint)

        drawTextLayout(canvas, ol, outLeft + 18f * s, curY + 13f * s)
    }
}

// ---------------------------------------------------------------------------
// 53. FILL IN STYLE (Conversation and series)
// "I am ______" with key word in underline slot in sun colour, "FILL IN YOURS" caption
// ---------------------------------------------------------------------------
object FillInStyle : BaseStyle("fill_in", "Fill in", category = "Conversation", supportsLights = true, maxChars = 85) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 30f * s

        // Caption "FILL IN YOURS"
        val hanken = StyleTypefaces.getHankenGrotesk()
        val capPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb()
            letterSpacing = 0.16f
        }
        canvas.drawText("FILL IN YOURS", leftX, curY + 10f * s, capPaint)
        curY += 28f * s

        // Hero declaration
        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 34f * s, 18f * s, availW, geom.height * 0.38f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
            curY += vl.height + 10f * s
        }
        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 54. PICK ONE STYLE (Conversation and series)
// A poll-style card with three options, one selected. User picks three declarations.
// ---------------------------------------------------------------------------
object PickOneStyle : BaseStyle("pick_one", "Pick one", category = "Conversation", supportsLights = true, maxChars = 90) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val cardW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 24f * s

        val hanken = StyleTypefaces.getHankenGrotesk()
        val capPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 13f * s
            color = (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb()
            letterSpacing = 0.16f
        }
        canvas.drawText("PICK ONE DECLARATION", leftX, curY + 12f * s, capPaint)
        curY += 32f * s

        val seeds = SeedDeclarations.asAffirmations()
        val opt1 = spec.declaration
        val opt2 = spec.recapDeclarations?.getOrNull(0)?.declaration ?: seeds.getOrNull(1)?.declaration ?: "I am loved with an everlasting love."
        val opt3 = spec.recapDeclarations?.getOrNull(1)?.declaration ?: seeds.getOrNull(2)?.declaration ?: "The Lord is my strength and shield."

        val options = listOf(opt1 to true, opt2 to false, opt3 to false)

        val newsreader = StyleTypefaces.getNewsreader()
        options.forEach { (text, isChecked) ->
            val optBg = if (isChecked) (if (light.dark) AndroidColor.argb(220, 45, 60, 55) else AndroidColor.argb(235, 253, 246, 235))
            else (if (light.dark) AndroidColor.argb(140, 25, 35, 32) else AndroidColor.argb(160, 245, 240, 230))
            val optPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = optBg }
            val borderPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = if (isChecked) (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb() else light.text.copy(alpha = 0.12f).toArgb()
                style = AndroidPaint.Style.STROKE
                strokeWidth = if (isChecked) 2f * s else 1f * s
            }

            val textAvailW = cardW - (54f * s).toInt()
            val (tl, _) = autoShrinkLayout(text, newsreader, 20f * s, 14f * s, textAvailW, 70f * s, light.text.toArgb())
            val optH = max(56f * s, tl.height + 24f * s)

            val rect = RectF(leftX, curY, leftX + cardW, curY + optH)
            canvas.drawRoundRect(rect, 14f * s, 14f * s, optPaint)
            canvas.drawRoundRect(rect, 14f * s, 14f * s, borderPaint)

            // Radio circle
            val radioX = leftX + 22f * s
            val radioY = curY + optH / 2f
            val radioPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                style = AndroidPaint.Style.STROKE
                strokeWidth = 2f * s
                color = if (isChecked) (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb() else light.text.copy(alpha = 0.40f).toArgb()
            }
            canvas.drawCircle(radioX, radioY, 9f * s, radioPaint)
            if (isChecked) {
                val fillPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = (if (light.dark) Color(0xFFE8A860) else Color(0xFFB5532B)).toArgb()
                }
                canvas.drawCircle(radioX, radioY, 5f * s, fillPaint)
            }

            drawTextLayout(canvas, tl, leftX + 44f * s, curY + (optH - tl.height) / 2f)
            curY += optH + 12f * s
        }
    }
}

// ---------------------------------------------------------------------------
// 55. WEEK STYLE (Conversation and series)
// Seven dots for the days of this week, filled for the days declared, "Five days of declaring."
// ---------------------------------------------------------------------------
object WeekStyle : BaseStyle("week", "Week", category = "Conversation", supportsLights = true, maxChars = 80) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 28f * s

        // 7 week dots
        val activeCount = (spec.milestoneDays ?: 5).coerceIn(1, 7)
        val dotRadius = 10f * s
        val dotGap = 26f * s
        val activeColor = if (light.dark) AndroidColor.parseColor("#E8A860") else AndroidColor.parseColor("#B5532B")
        val inactiveColor = light.text.copy(alpha = 0.20f).toArgb()

        for (i in 0 until 7) {
            val dx = leftX + dotRadius + i * (dotRadius * 2 + dotGap)
            val dotPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = if (i < activeCount) activeColor else inactiveColor
            }
            canvas.drawCircle(dx, curY + dotRadius, dotRadius, dotPaint)
        }
        curY += dotRadius * 2 + 18f * s

        val streakLabel = "$activeCount days of declaring."
        val hanken = StyleTypefaces.getHankenGrotesk()
        val streakPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 14f * s
            color = light.text.copy(alpha = 0.75f).toArgb()
        }
        canvas.drawText(streakLabel, leftX, curY + 12f * s, streakPaint)
        curY += 32f * s

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 34f * s, 18f * s, availW, geom.height * 0.38f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, availW, 26f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, curY)
        }
    }
}

// ---------------------------------------------------------------------------
// 56. CALL AND ANSWER STYLE (Conversation and series)
// Split frame: verse on Light above, declaration on ink block below
// ---------------------------------------------------------------------------
object CallAndAnswerStyle : BaseStyle("call_and_answer", "Call and answer", category = "Conversation", supportsLights = true, maxChars = 110) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val splitY = geom.height * 0.50f
        scope.drawRect(color = light.top, size = Size(geom.width, splitY))
        scope.drawRect(color = Ink, topLeft = Offset(0f, splitY), size = Size(geom.width, geom.height - splitY))
        scope.drawGrainOverlay(light)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val splitY = geom.height * 0.50f
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()

        // 1. Top half: Scripture verse (The Call)
        val hanken = StyleTypefaces.getHankenGrotesk()
        val callPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = light.text.copy(alpha = 0.65f).toArgb()
            letterSpacing = 0.16f
        }
        canvas.drawText("THE CALL", leftX, geom.topSafe + 16f * s, callPaint)

        val verseText = spec.verse ?: "Christ in you, the hope of glory."
        val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 24f * s, 15f * s, availW, splitY - geom.topSafe - 40f * s, light.text.toArgb())
        drawTextLayout(canvas, vl, leftX, geom.topSafe + 34f * s)

        val ref = (spec.reference ?: "").uppercase()
        if (ref.isNotBlank()) {
            val (rl, _) = autoShrinkLayout(ref, hanken, 12f * s, 10f * s, availW, 24f * s, light.text.copy(alpha = 0.65f).toArgb())
            drawTextLayout(canvas, rl, leftX, geom.topSafe + 38f * s + vl.height)
        }

        // 2. Bottom half: Declaration (The Answer)
        val answerPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = Cream.copy(alpha = 0.65f).toArgb()
            letterSpacing = 0.16f
        }
        canvas.drawText("THE ANSWER", leftX, splitY + 28f * s, answerPaint)

        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 32f * s, 18f * s, availW, geom.bottomSafe - splitY - 40f * s, Cream.toArgb())
        drawTextLayout(canvas, dl, leftX, splitY + 48f * s)
    }
}

// ---------------------------------------------------------------------------
// 57. COORDINATES STYLE (Conversation and series)
// Three boxes: BOOK, CHAPTER, VERSE, then the declaration
// ---------------------------------------------------------------------------
object CoordinatesStyle : BaseStyle("coordinates", "Coordinates", category = "Conversation", supportsLights = true, maxChars = 90) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val leftX = geom.margin
        val availW = (geom.width - geom.margin * 2f).toInt()
        var curY = geom.topSafe + 24f * s

        // Parse coordinates from reference
        val ref = spec.reference ?: "Colossians 1:27"
        val lastSpace = ref.lastIndexOf(' ')
        val bookName = if (lastSpace > 0) ref.substring(0, lastSpace).uppercase() else "BIBLE"
        val chAndV = if (lastSpace > 0) ref.substring(lastSpace + 1) else "1:1"
        val ch = chAndV.substringBefore(':')
        val v = chAndV.substringAfter(':', "1")

        // 3 boxes
        val boxH = 46f * s
        val box1W = availW * 0.50f
        val box2W = availW * 0.22f
        val box3W = availW * 0.22f
        val gap = (availW - box1W - box2W - box3W) / 2f

        val hanken = StyleTypefaces.getHankenGrotesk()
        val boxBg = if (light.dark) AndroidColor.argb(160, 25, 35, 32) else AndroidColor.argb(180, 245, 240, 230)
        val boxPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = boxBg }
        val borderPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.text.copy(alpha = 0.20f).toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1.2f * s
        }
        val lblPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 9f * s
            color = light.text.copy(alpha = 0.55f).toArgb()
            letterSpacing = 0.12f
        }
        val valPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = hanken
            textSize = 14f * s
            color = light.text.toArgb()
            letterSpacing = 0.06f
        }

        // Box 1: BOOK
        var bx = leftX
        canvas.drawRoundRect(RectF(bx, curY, bx + box1W, curY + boxH), 8f * s, 8f * s, boxPaint)
        canvas.drawRoundRect(RectF(bx, curY, bx + box1W, curY + boxH), 8f * s, 8f * s, borderPaint)
        canvas.drawText("BOOK", bx + 10f * s, curY + 16f * s, lblPaint)
        canvas.drawText(bookName.take(12), bx + 10f * s, curY + 34f * s, valPaint)

        // Box 2: CHAPTER
        bx += box1W + gap
        canvas.drawRoundRect(RectF(bx, curY, bx + box2W, curY + boxH), 8f * s, 8f * s, boxPaint)
        canvas.drawRoundRect(RectF(bx, curY, bx + box2W, curY + boxH), 8f * s, 8f * s, borderPaint)
        canvas.drawText("CH", bx + 8f * s, curY + 16f * s, lblPaint)
        canvas.drawText(ch, bx + 8f * s, curY + 34f * s, valPaint)

        // Box 3: VERSE
        bx += box2W + gap
        canvas.drawRoundRect(RectF(bx, curY, bx + box3W, curY + boxH), 8f * s, 8f * s, boxPaint)
        canvas.drawRoundRect(RectF(bx, curY, bx + box3W, curY + boxH), 8f * s, 8f * s, borderPaint)
        canvas.drawText("VERSE", bx + 8f * s, curY + 16f * s, lblPaint)
        canvas.drawText(v, bx + 8f * s, curY + 34f * s, valPaint)

        curY += boxH + 32f * s

        // Declaration below
        val (dl, _) = autoShrinkLayout(spec.declaration, StyleTypefaces.getNewsreader(), 34f * s, 18f * s, availW, geom.height * 0.40f, light.text.toArgb())
        drawTextLayout(canvas, dl, leftX, curY)
        curY += dl.height + 16f * s

        val rulePaint = AndroidPaint().apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(leftX, curY, leftX + 44f * s, curY + 2f * s, rulePaint)
        curY += 14f * s

        val verseText = spec.verse.orEmpty()
        if (verseText.isNotBlank()) {
            val (vl, _) = autoShrinkLayout("“$verseText”", StyleTypefaces.getNewsreaderItalic(), 18f * s, 13f * s, availW, geom.height * 0.22f, light.text.copy(alpha = 0.82f).toArgb())
            drawTextLayout(canvas, vl, leftX, curY)
        }
    }
}
