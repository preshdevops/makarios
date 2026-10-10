package com.makarios.app.ui.wallpaper

import android.content.Context
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
import androidx.core.content.res.ResourcesCompat
import com.makarios.app.R
import com.makarios.app.ui.theme.*
import com.makarios.app.util.ExportFormat
import kotlin.math.*
import kotlin.random.Random

typealias AndroidRectF = android.graphics.RectF

/** Global Typeface cache with safe JVM / Android fallbacks. */
object StyleTypefaces {
    @Volatile private var newsreader: Typeface? = null
    @Volatile private var newsreaderItalic: Typeface? = null
    @Volatile private var hankenGrotesk: Typeface? = null

    fun init(context: Context) {
        if (newsreader == null) {
            newsreader = runCatching { ResourcesCompat.getFont(context, R.font.newsreader) }.getOrNull()
        }
        if (newsreaderItalic == null) {
            newsreaderItalic = runCatching { ResourcesCompat.getFont(context, R.font.newsreader_italic) }.getOrNull()
        }
        if (hankenGrotesk == null) {
            hankenGrotesk = runCatching { ResourcesCompat.getFont(context, R.font.hanken_grotesk) }.getOrNull()
        }
    }

    fun getNewsreader(): Typeface? = newsreader ?: runCatching { Typeface.SERIF }.getOrNull()
    fun getNewsreaderItalic(): Typeface? = newsreaderItalic ?: runCatching { Typeface.create(Typeface.SERIF, Typeface.ITALIC) }.getOrNull()
    fun getHankenGrotesk(): Typeface? = hankenGrotesk ?: runCatching { Typeface.SANS_SERIF }.getOrNull()
}

/** Grain noise texture tile for eliminating color banding and adding tactile paper warmth. */
private val grainNoiseTile by lazy {
    val b = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(256 * 256)
    val rng = Random(0x4D414B23)
    for (i in pixels.indices) {
        val n = rng.nextInt(256)
        pixels[i] = AndroidColor.argb(n, n, n, n)
    }
    b.setPixels(pixels, 0, 256, 0, 0, 256, 256)
    b.asImageBitmap()
}

/** Layout configuration and platform safe zones for a target resolution. */
data class LayoutGeometry(
    val format: ExportFormat,
    val width: Float,
    val height: Float,
    val scale: Float,
    val margin: Float,
    val topSafe: Float,
    val bottomSafe: Float,
    val isLandscapeX: Boolean,
    val isSquare: Boolean
) {
    companion object {
        fun compute(size: IntSize): LayoutGeometry {
            val w = size.width.toFloat()
            val h = size.height.toFloat()
            val s = min(w / 390f, h / 844f)
            val format = when {
                w == 1600f && h == 900f -> ExportFormat.X
                w == 1080f && h == 1080f -> ExportFormat.Square
                w == 1080f && h == 1350f -> ExportFormat.Portrait
                abs(w / h - 1f) < 0.05f -> ExportFormat.Square
                w > h * 1.3f -> ExportFormat.X
                abs(w / h - 0.8f) < 0.05f -> ExportFormat.Portrait
                else -> ExportFormat.Story
            }
            val margin = when (format) {
                ExportFormat.Portrait -> w * 0.08f
                ExportFormat.X -> w * 0.06f
                ExportFormat.Square -> w * 0.08f
                else -> w * 0.08f
            }
            val topSafe = when (format) {
                ExportFormat.Story -> h * 0.20f // 20% top safe zone
                ExportFormat.Wallpaper -> h * 0.22f // Top 22% free for lock screen clock
                ExportFormat.Square -> h * 0.08f // 8% on all sides
                ExportFormat.Portrait -> h * 0.14f // 14% top safe
                ExportFormat.X -> h * 0.08f // top/bottom 8%
            }
            val bottomSafe = when (format) {
                ExportFormat.Story -> h * 0.80f // 20% bottom safe zone
                ExportFormat.Wallpaper -> h * 0.80f // 20% bottom safe zone
                ExportFormat.Square -> h * 0.92f // 8% bottom safe
                ExportFormat.Portrait -> h * 0.86f // 14% bottom safe
                ExportFormat.X -> h * 0.92f // 8% bottom safe
            }
            return LayoutGeometry(
                format = format,
                width = w,
                height = h,
                scale = s,
                margin = margin,
                topSafe = topSafe,
                bottomSafe = bottomSafe,
                isLandscapeX = format == ExportFormat.X,
                isSquare = format == ExportFormat.Square
            )
        }
    }
}

/** Draws grain texture and dither noise over canvas. */
fun DrawScope.drawGrainOverlay(light: Light, size: Size = this.size) {
    drawIntoCanvas { c ->
        c.drawRect(
            0f, 0f, size.width, size.height,
            Paint().apply {
                shader = ImageShader(grainNoiseTile, TileMode.Repeated, TileMode.Repeated)
                alpha = 0.07f
                blendMode = if (light.isLight) BlendMode.Multiply else BlendMode.Screen
            }
        )
    }
}

/** Draws smooth mountain ridges with seed-deterministic Bezier curves overextending 10% past edges. */
fun DrawScope.drawRidgePath(w: Float, h: Float, y0: Float, amp: Float, seed: Int, color: Color) {
    val overscan = w * 0.10f
    val r = Random(seed)
    val points = (0..6).map { i ->
        val x = -overscan + (w + 2 * overscan) * i / 6f
        Offset(x, y0 + amp * sin(i * 1.3f + seed) + (r.nextFloat() - 0.5f) * 0.8f * amp)
    }
    val path = Path().apply {
        moveTo(-overscan, h)
        lineTo(points[0].x, points[0].y)
        for (i in 0 until 6) {
            val m = Offset((points[i].x + points[i + 1].x) / 2f, (points[i].y + points[i + 1].y) / 2f)
            quadraticTo(points[i].x, points[i].y, m.x, m.y)
        }
        lineTo(points.last().x, points.last().y)
        lineTo(w + overscan, h)
        close()
    }
    drawPath(path, color)
}

/** Helper to draw the subtle liturgical wordmark at the bottom left with the mark. */
fun drawWordmark(
    canvas: AndroidCanvas,
    geom: LayoutGeometry,
    light: Light,
    include: Boolean = true
) {
    if (!include) return
    val alpha = 0.70f
    val scale = geom.scale
    val markSize = 16f * scale
    val x = geom.margin
    val y = geom.height * 0.94f

    // Draw logo mark (open doorway arch + half sun)
    val strokeWidth = 4.5f * (markSize / 100f)
    val archPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = light.text.copy(alpha = alpha).toArgb()
        style = AndroidPaint.Style.STROKE
        this.strokeWidth = strokeWidth
        strokeCap = AndroidPaint.Cap.ROUND
        strokeJoin = AndroidPaint.Join.ROUND
    }
    val sunPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = (if (light.isDark) Color(0xFFE8A860) else Color(0xFFB5532B)).copy(alpha = alpha).toArgb()
        style = AndroidPaint.Style.FILL
    }

    val s = markSize / 100f
    val mx = x
    val my = y - markSize * 0.85f

    // Sun: M32 82 a18 18 0 0 1 36 0 Z
    val sunPath = AndroidPath().apply {
        moveTo(mx + 32f * s, my + 82f * s)
        arcTo(AndroidRectF(mx + 32f * s, my + 64f * s, mx + 68f * s, my + 100f * s), 180f, 180f)
        close()
    }
    canvas.drawPath(sunPath, sunPaint)

    // Arch: M27 82 V46 a23 23 0 0 1 46 0 v36
    val archPath = AndroidPath().apply {
        moveTo(mx + 27f * s, my + 82f * s)
        lineTo(mx + 27f * s, my + 46f * s)
        arcTo(AndroidRectF(mx + 27f * s, my + 23f * s, mx + 73f * s, my + 69f * s), 180f, 180f)
        lineTo(mx + 73f * s, my + 82f * s)
    }
    canvas.drawPath(archPath, archPaint)

    // Ground: M18 82 h64
    canvas.drawLine(mx + 18f * s, my + 82f * s, mx + 82f * s, my + 82f * s, archPaint)

    // Text: "makarios" Newsreader Italic 500
    val textPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
        color = light.text.copy(alpha = alpha).toArgb()
        typeface = StyleTypefaces.getNewsreaderItalic()
        textSize = 15f * scale
        letterSpacing = -0.01f
        isSubpixelText = true
    }
    canvas.drawText("makarios", mx + markSize + 6f * scale, y, textPaint)
}

/**
 * Item F: Pre-calculates exact text bounding box for layout and safe disc placement.
 */
fun measurePairingBounds(
    geom: LayoutGeometry,
    light: Light,
    spec: StyleSpec,
    declarationSizeSp: Float = 34f,
    verseSizeSp: Float = 17f,
    topOverride: Float? = null,
    textWidthOverride: Float? = null,
    includeDeclaration: Boolean = true,
    includeVerse: Boolean = true,
    includeReference: Boolean = true,
    centerAlign: Boolean = false
): RectF {
    val textWidth = (textWidthOverride ?: if (geom.isLandscapeX) (geom.width * 0.55f - geom.margin * 2f) else (geom.width - geom.margin * 2f)).coerceAtLeast(100f)
    val s = geom.scale
    val startX = if (centerAlign) geom.width / 2f - textWidth / 2f else geom.margin

    val newsreader = StyleTypefaces.getNewsreader()
    val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()
    val hanken = StyleTypefaces.getHankenGrotesk()

    fun makeLayout(text: String, typeface: Typeface, sizePx: Float): StaticLayout? = runCatching {
        val paint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            this.typeface = typeface
            this.textSize = sizePx
            this.color = light.text.toArgb()
            this.isSubpixelText = true
        }
        StaticLayout.Builder.obtain(text, 0, text.length, paint, textWidth.toInt())
            .setIncludePad(false)
            .setLineSpacing(0f, 1.18f)
            .build()
    }.getOrNull()

    val boldTypeface = runCatching { Typeface.create(newsreader, Typeface.BOLD) }.getOrNull()
    if (boldTypeface == null) {
        val estLines = (spec.declaration.length / 26) + 1
        val estH = estLines * (declarationSizeSp * s * 1.25f) + 50f * s
        val startY = topOverride ?: (geom.topSafe + 20f * s)
        return RectF(startX, startY, startX + textWidth, startY + estH)
    }

    var declPx = declarationSizeSp * s
    val minDeclPx = 14f * s
    var dl = if (includeDeclaration) makeLayout(spec.declaration, boldTypeface, declPx) else null
    if (includeDeclaration && dl == null) {
        val estLines = (spec.declaration.length / 26) + 1
        val estH = estLines * (declarationSizeSp * s * 1.25f) + 50f * s
        val startY = topOverride ?: (geom.topSafe + 20f * s)
        return RectF(startX, startY, startX + textWidth, startY + estH)
    }
    while (dl != null && (dl.height > geom.height * 0.40f || dl.width > textWidth) && declPx > minDeclPx) {
        declPx *= 0.98f
        dl = makeLayout(spec.declaration, Typeface.create(newsreader, Typeface.BOLD), declPx)
    }

    val ruleH = 2f * s
    val gap1 = 20f * s
    val gap2 = 18f * s
    val gap3 = 10f * s

    var vPx = verseSizeSp * s
    val minVPx = 12f * s
    val verseText = spec.verse.orEmpty()
    var vl = if (includeVerse && verseText.isNotBlank()) makeLayout(verseText, Typeface.create(newsreaderItalic, Typeface.ITALIC), vPx) else null

    val refText = (spec.reference ?: "").uppercase()
    val refLayout = if (includeReference && refText.isNotBlank()) makeLayout(refText, Typeface.create(hanken, Typeface.NORMAL), 12f * s) else null

    fun totalHeight(includeV: Boolean): Float {
        var h = 0f
        val currentDl = dl
        if (currentDl != null) h += currentDl.height + gap1 + ruleH + gap2
        val currentVl = vl
        if (includeV && currentVl != null) h += currentVl.height + gap3
        val currentRef = refLayout
        if (currentRef != null) h += currentRef.height
        return h
    }

    var showVerse = vl != null
    val availableH = geom.bottomSafe - geom.topSafe
    while (showVerse && totalHeight(true) > availableH && vPx > minVPx) {
        vPx *= 0.98f
        vl = makeLayout(verseText, Typeface.create(newsreaderItalic, Typeface.ITALIC), vPx)
    }
    if (totalHeight(true) > availableH && dl != null) {
        showVerse = false
    }

    val totalH = totalHeight(showVerse)
    val startY = topOverride ?: when {
        geom.isSquare -> geom.topSafe + (geom.height * 0.42f - geom.topSafe - totalH).coerceAtLeast(0f) * 0.4f
        geom.isLandscapeX -> geom.topSafe + (geom.height - geom.topSafe - totalH).coerceAtLeast(0f) * 0.35f
        else -> geom.topSafe + (availableH - totalH).coerceAtLeast(0f) * 0.30f
    }

    return RectF(startX, startY, startX + textWidth, startY + totalH)
}

/**
 * Item F: Disc and glow positioning logic.
 * The sun/moon disc and its glow must NEVER sit behind the text block or the right rail.
 * - Disc and glow clear the text bounding box by at least 6% of screen width.
 * - On Story (and Wallpaper), the disc may never overlap the top 25% (clock) or the right 14% (reaction rail).
 * - Finds the largest free region between the text block and the ridges.
 */
fun computeSafeDiscPosition(
    geom: LayoutGeometry,
    light: Light,
    textBounds: RectF? = null
): Offset {
    val w = geom.width
    val h = geom.height
    val s = geom.scale
    val discRadius = light.discR * s
    val marginClear = w * 0.06f // >= 6% of width clearance from text

    val isStoryOrWallpaper = geom.format == ExportFormat.Story || geom.format == ExportFormat.Wallpaper
    val minTopY = if (isStoryOrWallpaper) h * 0.25f + discRadius else geom.topSafe + discRadius
    val maxRightX = if (isStoryOrWallpaper) w * 0.86f - discRadius else w - geom.margin - discRadius
    val minLeftX = geom.margin + discRadius
    val maxRidgeY = h * 0.72f - discRadius

    if (textBounds == null) {
        val defaultX = (w * light.discX).coerceIn(minLeftX, maxRightX)
        val defaultY = (h * light.discY).coerceIn(minTopY, maxRidgeY)
        return Offset(defaultX, defaultY)
    }

    val blockedRect = RectF(
        textBounds.left - marginClear,
        textBounds.top - marginClear,
        textBounds.right + marginClear,
        textBounds.bottom + marginClear
    )

    fun isClear(cx: Float, cy: Float): Boolean {
        if (cx - discRadius < minLeftX - discRadius || cx + discRadius > maxRightX + discRadius) return false
        if (cy - discRadius < minTopY - discRadius || cy + discRadius > maxRidgeY + discRadius) return false
        val intersects = !(cx + discRadius < blockedRect.left || cx - discRadius > blockedRect.right || cy + discRadius < blockedRect.top || cy - discRadius > blockedRect.bottom)
        return !intersects
    }

    val naturalX = (w * light.discX).coerceIn(minLeftX, maxRightX)
    val naturalY = (h * light.discY).coerceIn(minTopY, maxRidgeY)
    if (isClear(naturalX, naturalY)) {
        return Offset(naturalX, naturalY)
    }

    if (geom.isLandscapeX) {
        val candX = (w * 0.74f).coerceIn(minLeftX, maxRightX)
        val candY = (h * 0.45f).coerceIn(minTopY, maxRidgeY)
        return Offset(candX, candY)
    }

    val spaceBelow = maxRidgeY - (blockedRect.bottom + discRadius)
    val spaceAbove = (blockedRect.top - discRadius) - minTopY
    val spaceRight = maxRightX - (blockedRect.right + discRadius)
    val spaceLeft = (blockedRect.left - discRadius) - minLeftX

    val candidates = mutableListOf<Offset>()

    // Priority 1: below text (between text block and ridges)
    if (spaceBelow > discRadius * 1.2f) {
        val cy = (blockedRect.bottom + discRadius + marginClear).coerceIn(minTopY, maxRidgeY)
        val cx = naturalX.coerceIn(minLeftX, maxRightX)
        candidates.add(Offset(cx, cy))
    }

    // Priority 2: above text (if text is lower down and clears top 25%)
    if (spaceAbove > discRadius * 1.2f) {
        val cy = (blockedRect.top - discRadius - marginClear).coerceIn(minTopY, maxRidgeY)
        val cx = naturalX.coerceIn(minLeftX, maxRightX)
        candidates.add(Offset(cx, cy))
    }

    // Priority 3: to the right of text
    if (spaceRight > discRadius * 1.2f) {
        val cx = (blockedRect.right + discRadius + marginClear).coerceIn(minLeftX, maxRightX)
        val cy = naturalY.coerceIn(minTopY, maxRidgeY)
        candidates.add(Offset(cx, cy))
    }

    // Priority 4: to the left of text
    if (spaceLeft > discRadius * 1.2f) {
        val cx = (blockedRect.left - discRadius - marginClear).coerceIn(minLeftX, maxRightX)
        val cy = naturalY.coerceIn(minTopY, maxRidgeY)
        candidates.add(Offset(cx, cy))
    }

    val best = candidates.firstOrNull { isClear(it.x, it.y) }
    if (best != null) return best

    val safeY = (blockedRect.bottom + discRadius + marginClear).coerceIn(minTopY, maxRidgeY)
    val safeX = (w * 0.5f).coerceIn(minLeftX, maxRightX)
    return Offset(safeX, safeY)
}

/**
 * Robust text layout helper: measures text, autofits within safe bounds without truncation,
 * applies the contrast gate with soft vignette if necessary, and draws to the canvas.
 */
fun drawAdaptivePairing(
    canvas: AndroidCanvas,
    bitmap: Bitmap,
    geom: LayoutGeometry,
    light: Light,
    spec: StyleSpec,
    declarationSizeSp: Float = 34f,
    verseSizeSp: Float = 17f,
    topOverride: Float? = null,
    textWidthOverride: Float? = null,
    includeDeclaration: Boolean = true,
    includeVerse: Boolean = true,
    includeReference: Boolean = true,
    centerAlign: Boolean = false
) {
    val textWidth = (textWidthOverride ?: if (geom.isLandscapeX) (geom.width * 0.55f - geom.margin * 2f) else (geom.width - geom.margin * 2f)).coerceAtLeast(100f)
    val s = geom.scale
    val startX = if (centerAlign) geom.width / 2f - textWidth / 2f else if (geom.isLandscapeX) geom.margin else geom.margin

    val newsreader = StyleTypefaces.getNewsreader()
    val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()
    val hanken = StyleTypefaces.getHankenGrotesk()

    fun makeLayout(text: String, typeface: Typeface, sizePx: Float, align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL): StaticLayout {
        val paint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            this.typeface = typeface
            this.textSize = sizePx
            this.color = light.text.toArgb()
            this.isSubpixelText = true
        }
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, textWidth.toInt())
            .setAlignment(align)
            .setIncludePad(false)
            .setLineSpacing(0f, 1.18f)
            .setEllipsize(null)
            .setMaxLines(Int.MAX_VALUE)
            .build()
    }

    var declPx = declarationSizeSp * s
    val minDeclPx = 14f * s
    var dl = if (includeDeclaration) makeLayout(spec.declaration, Typeface.create(newsreader, Typeface.BOLD), declPx, if (centerAlign) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL) else null

    // Autofit declaration lines down in 2% increments
    while (dl != null && (dl.height > geom.height * 0.40f || dl.width > textWidth) && declPx > minDeclPx) {
        declPx *= 0.98f
        dl = makeLayout(spec.declaration, Typeface.create(newsreader, Typeface.BOLD), declPx, if (centerAlign) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL)
    }

    val ruleH = 2f * s
    val ruleW = 28f * s
    val gap1 = 20f * s
    val gap2 = 18f * s
    val gap3 = 10f * s

    var vPx = verseSizeSp * s
    val minVPx = 12f * s
    val verseText = spec.verse.orEmpty()
    var vl = if (includeVerse && verseText.isNotBlank()) makeLayout(verseText, Typeface.create(newsreaderItalic, Typeface.ITALIC), vPx, if (centerAlign) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL) else null

    val refText = (spec.reference ?: "").uppercase()
    val refLayout = if (includeReference && refText.isNotBlank()) makeLayout(refText, Typeface.create(hanken, Typeface.NORMAL), 12f * s, if (centerAlign) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL) else null

    fun totalHeight(includeV: Boolean): Float {
        var h = 0f
        if (dl != null) h += dl.height + gap1 + ruleH + gap2
        val currentVl = vl
        if (includeV && currentVl != null) h += currentVl.height + gap3
        if (refLayout != null) h += refLayout.height
        return h
    }

    var showVerse = vl != null
    val availableH = geom.bottomSafe - geom.topSafe
    while (showVerse && totalHeight(true) > availableH && vPx > minVPx) {
        vPx *= 0.98f
        vl = makeLayout(verseText, Typeface.create(newsreaderItalic, Typeface.ITALIC), vPx, if (centerAlign) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL)
    }
    if (totalHeight(true) > availableH && dl != null) {
        showVerse = false
    }

    val totalH = totalHeight(showVerse)
    val startY = topOverride ?: when {
        geom.isSquare -> geom.topSafe + (geom.height * 0.42f - geom.topSafe - totalH).coerceAtLeast(0f) * 0.4f
        geom.isLandscapeX -> geom.topSafe + (geom.height - geom.topSafe - totalH).coerceAtLeast(0f) * 0.35f
        else -> geom.topSafe + (availableH - totalH).coerceAtLeast(0f) * 0.30f
    }

    // Automated Contrast Gate check and soft vignette
    val contrast = ContrastGate.sampleContrast(
        bitmap = bitmap,
        left = startX.toInt(),
        top = startY.toInt(),
        right = (startX + textWidth).toInt(),
        bottom = (startY + totalH).toInt(),
        textColor = light.text.toArgb()
    )
    if (contrast < 4.5f) {
        // Draw soft vignette scrim under text box to bring contrast above WCAG AA
        val scrimPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            val cx = startX + textWidth / 2f
            val cy = startY + totalH / 2f
            val radius = max(textWidth, totalH) * 0.8f
            val scrimColor = if (light.dark) AndroidColor.argb(160, 10, 15, 14) else AndroidColor.argb(140, 255, 250, 240)
            shader = RadialGradient(cx, cy, radius, scrimColor, AndroidColor.TRANSPARENT, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(startX + textWidth / 2f, startY + totalH / 2f, max(textWidth, totalH) * 0.8f, scrimPaint)
    }

    // Draw elements
    var curY = startY
    dl?.let {
        canvas.save()
        canvas.translate(startX, curY)
        it.draw(canvas)
        canvas.restore()
        curY += it.height + gap1

        val rulePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            color = light.anchorRule.toArgb()
        }
        val rx = if (centerAlign) (startX + textWidth / 2f - ruleW / 2f) else startX
        canvas.drawRect(rx, curY, rx + ruleW, curY + ruleH, rulePaint)
        curY += ruleH + gap2
    }

    if (showVerse && vl != null) {
        canvas.save()
        canvas.translate(startX, curY)
        vl.draw(canvas)
        canvas.restore()
        curY += vl.height + gap3
    }

    refLayout?.let {
        canvas.save()
        canvas.translate(startX, curY)
        it.draw(canvas)
        canvas.restore()
    }
}

/** Base Style renderer helper producing a pixel-perfect Bitmap at export resolution. */
abstract class BaseStyle(override val id: String, override val displayName: String) : Style {
    override fun supports(format: ExportFormat): Boolean = true
    override fun textBudget(format: ExportFormat): TextBudget = when (format) {
        ExportFormat.Story, ExportFormat.Wallpaper -> TextBudget(64f, 32f, 36f, 24f)
        ExportFormat.Square -> TextBudget(58f, 30f, 32f, 22f)
        ExportFormat.Portrait -> TextBudget(60f, 30f, 34f, 22f)
        ExportFormat.X -> TextBudget(48f, 26f, 28f, 20f)
    }

    override fun render(spec: StyleSpec, light: Light, size: IntSize): Bitmap {
        val w = size.width
        val h = size.height
        require(w > 0 && h > 0) { "Render dimensions must be positive ($w x $h)" }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val androidCanvas = AndroidCanvas(bitmap)
        val geom = LayoutGeometry.compute(size)

        // Draw offscreen using Compose CanvasDrawScope
        val scope = CanvasDrawScope()
        scope.draw(Density(1f), LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(androidCanvas), Size(w.toFloat(), h.toFloat())) {
            drawArt(this, geom, light, spec)
        }

        // Draw typography & layout adaptation
        drawTypography(androidCanvas, bitmap, geom, light, spec)

        // Draw subtle wordmark
        drawWordmark(androidCanvas, geom, light)

        return bitmap
    }

    abstract fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec)
    abstract fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec)
}

// ---------------------------------------------------------------------------
// 1. PAIRING STYLE (Prompt 0 Base Style)
// ---------------------------------------------------------------------------
object PairingStyle : BaseStyle("pairing", "Pairing") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val textBounds = measurePairingBounds(geom, light, spec, declarationSizeSp = 34f, verseSizeSp = 17f)
        val safeDiscPos = computeSafeDiscPosition(geom, light, textBounds)
        scope.drawLight(light, Size(geom.width, geom.height), discPos = safeDiscPos)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 34f, verseSizeSp = 17f)
    }
}

// ---------------------------------------------------------------------------
// 2. WINDOWS STYLE (Night Floor with 3 Arches)
// ---------------------------------------------------------------------------
object WindowsStyle : BaseStyle("windows", "Windows") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        // Dark floor background
        scope.drawRect(Brush.verticalGradient(listOf(Color(0xFF0A1110), Color(0xFF050908))), size = Size(w, h))

        val offsetX = if (geom.isLandscapeX) w * 0.45f else 0f
        val offsetY = if (geom.isSquare) h * 0.18f else 0f
        val archScale = if (geom.isLandscapeX) s * 0.75f else if (geom.isSquare) s * 0.85f else s

        fun drawArch(x: Float, y: Float, aw: Float, ah: Float, sceneLight: Light) {
            val path = Path().apply {
                moveTo(x, y + ah)
                lineTo(x, y + aw / 2f)
                cubicTo(x, y, x + aw, y, x + aw, y + aw / 2f)
                lineTo(x + aw, y + ah)
                close()
            }
            scope.clipPath(path) {
                drawLight(sceneLight, Size(w, h), grain = false)
            }
            scope.drawPath(path, Color(0xFFC9964A), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f * archScale))
        }

        // Centre arch: 200x490 at x=95, y=150 (Dawn)
        drawArch(offsetX + 95f * archScale, offsetY + 150f * archScale, 200f * archScale, 490f * archScale, Light.Dawn)
        // Left arch: 68x320 at x=12, y=320 (Dusk)
        drawArch(offsetX + 12f * archScale, offsetY + 320f * archScale, 68f * archScale, 320f * archScale, Light.Dusk)
        // Right arch: 68x320 at x=310, y=320 (Mist)
        drawArch(offsetX + 310f * archScale, offsetY + 320f * archScale, 68f * archScale, 320f * archScale, Light.Mist)

        // Light pool trapezoid on the floor
        scope.drawIntoCanvas { c ->
            val p = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    offsetX + 196f * archScale, offsetY + 640f * archScale, 320f * archScale,
                    Color(0x4DF4B98E).toArgb(), AndroidColor.TRANSPARENT, Shader.TileMode.CLAMP
                )
            }
            val pool = AndroidPath().apply {
                moveTo(offsetX + 130f * archScale, offsetY + 620f * archScale)
                lineTo(offsetX + 262f * archScale, offsetY + 620f * archScale)
                lineTo(offsetX + 350f * archScale, offsetY + 900f * archScale)
                lineTo(offsetX + 40f * archScale, offsetY + 900f * archScale)
                close()
            }
            c.nativeCanvas.drawPath(pool, p)
        }
        scope.drawGrainOverlay(Light.Night, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        if (geom.isSquare) {
            drawAdaptivePairing(canvas, bitmap, geom, Light.Night, spec, declarationSizeSp = 28f, verseSizeSp = 15f, topOverride = geom.topSafe)
        } else if (geom.isLandscapeX) {
            drawAdaptivePairing(canvas, bitmap, geom, Light.Night, spec, declarationSizeSp = 30f, verseSizeSp = 16f)
        } else {
            drawAdaptivePairing(canvas, bitmap, geom, Light.Night, spec, declarationSizeSp = 26f, verseSizeSp = 15f, topOverride = geom.height * 0.76f)
        }
    }
}

// ---------------------------------------------------------------------------
// 3. RAYS STYLE (Ember Sun with 29 Wedges)
// ---------------------------------------------------------------------------
object RaysStyle : BaseStyle("rays", "Rays") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawRect(Brush.verticalGradient(listOf(Color(0xFF9C4A26), Color(0xFF6B2E1E))), size = Size(w, h))
        val cx = if (geom.isLandscapeX) w * 0.72f else w * 0.5f
        val cy = if (geom.isSquare) h * 0.68f else h * 0.66f
        val maxR = 700f * s

        // Glowing sun halo
        scope.drawIntoCanvas { c ->
            val p = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(cx, cy, maxR, Color(0x8CFFE2B4).toArgb(), AndroidColor.TRANSPARENT, Shader.TileMode.CLAMP)
            }
            c.nativeCanvas.drawCircle(cx, cy, maxR, p)
        }

        // Sun disc r=44
        scope.drawCircle(Color(0xFFFFF1D6), 44f * s, Offset(cx, cy))

        // 29 wedges (6.4° apart, 2.8° wide, radial alpha 55% to 0)
        repeat(29) { i ->
            val angle = (-90f + i * 6.4f) * (PI / 180f)
            val angle2 = angle + 2.8f * (PI / 180f)
            val path = Path().apply {
                moveTo(cx, cy)
                lineTo(cx + cos(angle).toFloat() * maxR, cy + sin(angle).toFloat() * maxR)
                lineTo(cx + cos(angle2).toFloat() * maxR, cy + sin(angle2).toFloat() * maxR)
                close()
            }
            scope.drawPath(path, Color(0x59FFE2B4))
        }

        // 3 dark ridges
        scope.drawRidgePath(w, h, h * 0.72f, h * 0.020f, 3, Color(0xFF4A1F14))
        scope.drawRidgePath(w, h, h * 0.80f, h * 0.016f, 5, Color(0xFF33150E))
        scope.drawRidgePath(w, h, h * 0.88f, h * 0.013f, 9, Color(0xFF1E0C08))

        scope.drawGrainOverlay(Light.Ember, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = if (geom.isSquare) geom.topSafe else if (geom.isLandscapeX) geom.topSafe else geom.topSafe + 16f * geom.scale
        drawAdaptivePairing(canvas, bitmap, geom, Light.Ember, spec, declarationSizeSp = 32f, verseSizeSp = 16f, topOverride = top)
    }
}

// ---------------------------------------------------------------------------
// 4. NUMERALS STYLE (Verse Chapter Number Clip Path)
// ---------------------------------------------------------------------------
object NumeralsStyle : BaseStyle("numerals", "Numerals") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawRect(Brush.verticalGradient(listOf(Color(0xFFF4B98E), Color(0xFFC9703F), Color(0xFF6B2E1E))), size = Size(w, h))

        val chapter = spec.resolvedChapter
        val type = StyleTypefaces.getNewsreader()

        scope.drawIntoCanvas { c ->
            val nativeCanvas = c.nativeCanvas
            // 1. Outline extraction at modest size (200px) - avoids FreeType/Skia glyph rasterisation clipping
            val baseSize = 200f
            val numPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(type, Typeface.BOLD)
                textSize = baseSize
                textAlign = AndroidPaint.Align.LEFT
            }
            val basePath = AndroidPath()
            numPaint.getTextPath(chapter, 0, chapter.length, 0f, 0f, basePath)

            val bounds = RectF()
            basePath.computeBounds(bounds, true)

            // Desired height: 380sp * scale
            val targetHeight = (if (geom.isLandscapeX) 240f else if (geom.isSquare) 260f else 360f) * s
            val maxWidth = if (geom.isLandscapeX) w * 0.40f else w * 0.80f
            val rawScale = if (bounds.height() > 0f) targetHeight / bounds.height() else (targetHeight / baseSize)
            // Constrain width so 3-digit numerals (119, 150) or combined chapter:verse never bleed outside safe margins
            val scaleFactor = if (bounds.width() * rawScale > maxWidth && bounds.width() > 0f) {
                maxWidth / bounds.width()
            } else {
                rawScale
            }

            val targetCenterX = if (geom.isLandscapeX) w * 0.74f else w * 0.5f
            val targetCenterY = if (geom.isLandscapeX) h * 0.52f else if (geom.isSquare) h * 0.68f else h * 0.44f

            // 2. Scale up using Matrix
            val matrix = AndroidMatrix().apply {
                postTranslate(-bounds.centerX(), -bounds.centerY())
                postScale(scaleFactor, scaleFactor)
                postTranslate(targetCenterX, targetCenterY)
            }

            val numeralPath = AndroidPath()
            basePath.transform(matrix, numeralPath)

            val scaledW = bounds.width() * scaleFactor
            val scaledH = bounds.height() * scaleFactor

            // 3. Clip strictly to the numeral path - NO glow outside the numeral!
            nativeCanvas.save()
            nativeCanvas.clipPath(numeralPath)

            // Mini sunset landscape inside the clipped numeral
            val gradPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
                shader = LinearGradient(
                    0f, targetCenterY - scaledH * 0.55f,
                    0f, targetCenterY + scaledH * 0.55f,
                    Color(0xFFFBE3C4).toArgb(),
                    Color(0xFF6B2E1E).toArgb(),
                    Shader.TileMode.CLAMP
                )
            }
            nativeCanvas.drawPaint(gradPaint)

            // Sun disc and glow inside the numeral clip ONLY
            val sunRadius = (28f * s).coerceAtLeast(14f)
            val sunX = targetCenterX + scaledW * 0.18f
            val sunY = targetCenterY - scaledH * 0.16f

            val glowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    sunX, sunY, sunRadius * 2.8f,
                    intArrayOf(Color(0x99FFE8C2).toArgb(), Color(0x00FFE8C2).toArgb()),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            nativeCanvas.drawCircle(sunX, sunY, sunRadius * 2.8f, glowPaint)

            val sunPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = Color(0xFFFFD9A8).toArgb()
            }
            nativeCanvas.drawCircle(sunX, sunY, sunRadius, sunPaint)

            // Mini sunset ridges inside clipped numeral
            scope.drawRidgePath(w, h, targetCenterY + scaledH * 0.12f, 12f * s, 3, Color(0xFF6B2E1E))
            scope.drawRidgePath(w, h, targetCenterY + scaledH * 0.36f, 10f * s, 5, Color(0xFF432017))

            nativeCanvas.restore()
        }
        scope.drawGrainOverlay(Light.Midday, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = if (geom.isSquare) geom.topSafe else if (geom.isLandscapeX) geom.topSafe else geom.height * 0.58f
        drawAdaptivePairing(canvas, bitmap, geom, Light.Midday, spec, declarationSizeSp = 28f, verseSizeSp = 15f, topOverride = top, centerAlign = !geom.isLandscapeX)
    }
}

// ---------------------------------------------------------------------------
// 5. PAPER STYLE (6 Paper-cut Ridges with Upward Drop Shadows)
// ---------------------------------------------------------------------------
object PaperStyle : BaseStyle("paper", "Paper") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawRect(Color(0xFFE5EEF0), size = Size(w, h))

        // Pale sun in sky positioned safely away from text and safe zones
        val textBounds = measurePairingBounds(geom, Light.Rain, spec, declarationSizeSp = 30f, verseSizeSp = 15f)
        val safeSun = computeSafeDiscPosition(geom, Light.Rain, textBounds)
        val sunX = safeSun.x
        val sunY = safeSun.y
        scope.drawCircle(Color(0x33FFFFFF), 48f * s, Offset(sunX, sunY))
        scope.drawCircle(Color(0xFFEEF3F4), 28f * s, Offset(sunX, sunY))

        val colors = listOf(
            Color(0xFFCBDADD), Color(0xFFAFC8CE), Color(0xFF8EADB5),
            Color(0xFF6F929C), Color(0xFF527984), Color(0xFF3F626E)
        )
        val startRatio = if (geom.isSquare) 0.50f else 0.54f
        val stepRatio = if (geom.isSquare) 0.075f else 0.065f

        colors.forEachIndexed { i, c ->
            val yPos = h * (startRatio + i * stepRatio)

            // Upward drop shadow (blur 3dp, 45% alpha)
            scope.drawIntoCanvas { canvas ->
                val shadowPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(115, 20, 35, 40)
                    maskFilter = BlurMaskFilter(3f * s, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawRect(0f, yPos - 4f * s, w, yPos, shadowPaint)
            }

            // Paper ridge
            scope.drawRidgePath(w, h, yPos, h * 0.018f, 30 + i, c)

            // Thin fog band
            scope.drawRect(
                Color.White.copy(alpha = 0.16f),
                topLeft = Offset(0f, yPos - 2f * s),
                size = Size(w, 3.5f * s)
            )
        }
        scope.drawGrainOverlay(Light.Rain, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = if (geom.isSquare) geom.topSafe else geom.topSafe + 12f * geom.scale
        drawAdaptivePairing(canvas, bitmap, geom, Light.Rain, spec, declarationSizeSp = 32f, verseSizeSp = 16f, topOverride = top)
    }
}

// ---------------------------------------------------------------------------
// 6. CONSTELLATION STYLE (Milky Way & Cross Constellation)
// ---------------------------------------------------------------------------
object ConstellationStyle : BaseStyle("constellation", "Constellation") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawRect(Brush.verticalGradient(listOf(Color(0xFF1F2B2A), Color(0xFF0F1716))), size = Size(w, h))

        // Blurred Milky Way band (rotate -28 degrees, blur 28dp)
        scope.drawIntoCanvas { c ->
            val p = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(0f, h * 0.2f, w, h * 0.7f, Color(0x339FB4B4).toArgb(), AndroidColor.TRANSPARENT, Shader.TileMode.CLAMP)
                maskFilter = BlurMaskFilter(28f * s, BlurMaskFilter.Blur.NORMAL)
            }
            c.nativeCanvas.save()
            c.nativeCanvas.rotate(-28f, w * 0.5f, h * 0.45f)
            c.nativeCanvas.drawOval(-w * 0.1f, h * 0.28f, w * 1.1f, h * 0.55f, p)
            c.nativeCanvas.restore()
        }

        // 230 seeded stars
        val rng = Random(spec.seed)
        repeat(230) {
            val starAlpha = 0.35f + rng.nextFloat() * 0.45f
            val starR = (0.5f + rng.nextFloat() * 1.1f) * s
            val starX = rng.nextFloat() * w
            val starY = h * 0.08f + rng.nextFloat() * h * 0.75f
            scope.drawCircle(Color(0xFFF3E6C8).copy(alpha = starAlpha), starR, Offset(starX, starY))
        }

        // Cross constellation (vertical 4 stars, horizontal 2, hairlines #C9964A, glows)
        val cx = if (geom.isLandscapeX) w * 0.75f else w * 0.68f
        val cy = if (geom.isSquare) h * 0.62f else h * 0.43f
        val points = listOf(
            Offset(cx, cy - 0.14f * h),
            Offset(cx, cy - 0.07f * h),
            Offset(cx, cy),
            Offset(cx, cy + 0.07f * h),
            Offset(cx - 0.07f * w, cy)
        )
        // Hairlines in gold #C9964A
        scope.drawLine(Color(0xFFC9964A), points[0], points[3], 1.2f * s)
        scope.drawLine(Color(0xFFC9964A), points[2], points[4], 1.2f * s)

        // Star glows and vertices
        points.forEach { pt ->
            scope.drawCircle(Color(0x55E8D09A), 6f * s, pt)
            scope.drawCircle(Color(0xFFE8D09A), 2.4f * s, pt)
        }

        // Bottom ridge
        scope.drawRidgePath(w, h, h * 0.91f, h * 0.010f, 90, Color(0xFF070D0C))
        scope.drawGrainOverlay(Light.Night, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = if (geom.isSquare) geom.topSafe else geom.topSafe + 16f * geom.scale
        drawAdaptivePairing(canvas, bitmap, geom, Light.Night, spec, declarationSizeSp = 32f, verseSizeSp = 16f, topOverride = top)
    }
}

// ---------------------------------------------------------------------------
// 7. WORD STYLE (Typographic Poster with Sunset Fill)
// ---------------------------------------------------------------------------
object WordStyle : BaseStyle("word", "Word") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        scope.drawRect(Brush.verticalGradient(listOf(Color(0xFF141F1E), Color(0xFF0A1010))), size = Size(w, h))
        scope.drawGrainOverlay(Light.Night, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale
        val words = spec.declaration.uppercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val lines = if (words.size <= 3) {
            listOf(words.joinToString(" "))
        } else {
            words.chunked(min(3, max(2, (words.size + 2) / 3))).map { it.joinToString(" ") }.take(4)
        }

        val type = StyleTypefaces.getNewsreader()
        val targetWidth = if (geom.isLandscapeX) (w * 0.50f) else (342f * s)
        val combinedPath = AndroidPath()

        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(type, Typeface.BOLD)
        }

        var startY = if (geom.isSquare) geom.topSafe + 35f * s else if (geom.isLandscapeX) geom.topSafe + 40f * s else geom.topSafe + 60f * s
        val lineSpacing = 68f * s

        lines.forEach { line ->
            // Measure & scale font so line fills target width without stretching glyphs
            var fontSize = 48f * s
            paint.textSize = fontSize
            var measured = paint.measureText(line)
            if (measured > 0f) {
                fontSize = (fontSize * (targetWidth / measured)).coerceIn(24f * s, 88f * s)
                paint.textSize = fontSize
                val remainingRatio = ((targetWidth - paint.measureText(line)) / paint.measureText(line)).coerceIn(-0.06f, 0.06f)
                paint.letterSpacing = remainingRatio
            }
            val lineX = if (geom.isLandscapeX) geom.margin else (w - paint.measureText(line)) / 2f
            val linePath = AndroidPath()
            paint.getTextPath(line, 0, line.length, lineX, startY, linePath)
            combinedPath.addPath(linePath)
            startY += lineSpacing
        }

        // Clip combined path and fill with vertical sunset gradient + sun + soft ridge
        canvas.save()
        canvas.clipPath(combinedPath)

        val sunsetColors = intArrayOf(
            Color(0xFFFBE3C4).toArgb(),
            Color(0xFFF4B98E).toArgb(),
            Color(0xFFE08A55).toArgb(),
            Color(0xFFC9703F).toArgb()
        )
        val sunsetShader = LinearGradient(0f, startY - 260f * s, 0f, startY + 20f * s, sunsetColors, null, Shader.TileMode.CLAMP)
        val fillPaint = AndroidPaint().apply { shader = sunsetShader }
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        // Sun disc inside letters
        val sunPaint = AndroidPaint().apply { color = Color(0xFFFFD9A8).toArgb() }
        canvas.drawCircle(w * 0.70f, startY - 140f * s, 48f * s, sunPaint)

        // Soft ridge inside letters
        val ridgePaint = AndroidPaint().apply { color = Color(0xFFC9703F).toArgb() }
        canvas.drawRect(0f, startY - 40f * s, w, h, ridgePaint)
        canvas.restore()

        // Italic verse 20/30 at bottom + reference in caps
        val verseTop = if (geom.isSquare) geom.height * 0.74f else if (geom.isLandscapeX) geom.height * 0.65f else geom.height * 0.76f
        drawAdaptivePairing(
            canvas, bitmap, geom, Light.Night, spec,
            declarationSizeSp = 0f, verseSizeSp = 18f,
            topOverride = verseTop, includeDeclaration = false
        )
    }
}

// ---------------------------------------------------------------------------
// 8. PAGE STYLE (Illuminated Manuscript Parchment)
// ---------------------------------------------------------------------------
object PageStyle : BaseStyle("page", "Page") {
    override fun supports(format: ExportFormat): Boolean = format != ExportFormat.X // Landscape 16:9 not supported

    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        // Parchment gradient #FFF4D6 to #F1DDB0
        scope.drawRect(Brush.verticalGradient(listOf(Color(0xFFFFF4D6), Color(0xFFF1DDB0))), size = Size(w, h))

        // Outer arch: M40,790 L40,250 A155,155 0 0 1 350,250 L350,790 Z (scaled by s)
        val outerArch = Path().apply {
            moveTo(40f * s, 790f * s)
            lineTo(40f * s, 250f * s)
            arcTo(
                androidx.compose.ui.geometry.Rect(40f * s, 95f * s, 350f * s, 405f * s),
                180f, 180f, false
            )
            lineTo(350f * s, 790f * s)
            close()
        }
        scope.drawPath(outerArch, Color(0xFF8A5A14), style = androidx.compose.ui.graphics.drawscope.Stroke(2f * s))

        // Inner arch offset 12dp inside
        val innerArch = Path().apply {
            moveTo(52f * s, 778f * s)
            lineTo(52f * s, 250f * s)
            arcTo(
                androidx.compose.ui.geometry.Rect(52f * s, 107f * s, 338f * s, 393f * s),
                180f, 180f, false
            )
            lineTo(338f * s, 778f * s)
            close()
        }
        scope.drawPath(innerArch, Color(0xFF8A5A14), style = androidx.compose.ui.graphics.drawscope.Stroke(0.8f * s))

        // Top ornament: 2 rules + 3 dots + central diamond
        val topY = 82f * s
        scope.drawLine(Color(0xFF8A5A14), Offset(130f * s, topY), Offset(180f * s, topY), 1f * s)
        scope.drawLine(Color(0xFF8A5A14), Offset(210f * s, topY), Offset(260f * s, topY), 1f * s)
        scope.drawCircle(Color(0xFF8A5A14), 1.8f * s, Offset(188f * s, topY))
        scope.drawCircle(Color(0xFF8A5A14), 1.8f * s, Offset(202f * s, topY))
        val diamond = Path().apply {
            moveTo(195f * s, topY - 4f * s)
            lineTo(199f * s, topY)
            lineTo(195f * s, topY + 4f * s)
            lineTo(191f * s, topY)
            close()
        }
        scope.drawPath(diamond, Color(0xFF8A5A14))

        // Bottom roundel ornament
        val botY = 806f * s
        scope.drawCircle(Color(0xFF8A5A14), 6f * s, Offset(195f * s, botY), style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f * s))
        scope.drawCircle(Color(0xFF8A5A14), 2f * s, Offset(195f * s, botY))

        scope.drawGrainOverlay(Light.Midday, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val goldColor = AndroidColor.rgb(138, 90, 20)
        val inkColor = AndroidColor.rgb(35, 25, 20)
        val verseText = spec.verse.orEmpty().ifBlank { "The Lord is my shepherd; I shall not want." }

        // Small-caps book label
        val bookLabel = (spec.reference?.substringBeforeLast(" ") ?: "THE SCRIPTURES").uppercase()
        val labelPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            typeface = StyleTypefaces.getHankenGrotesk()
            textSize = 10.5f * s
            textAlign = AndroidPaint.Align.CENTER
            letterSpacing = 0.22f
        }
        canvas.drawText(bookLabel, 195f * s, 185f * s, labelPaint)

        // Large gold drop cap (118sp)
        val dropChar = verseText.take(1).uppercase()
        val restVerse = verseText.drop(1)
        val dropPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            typeface = StyleTypefaces.getNewsreader()
            textSize = 118f * s
            textAlign = AndroidPaint.Align.LEFT
        }
        canvas.drawText(dropChar, 68f * s, 340f * s, dropPaint)

        // Verse text set in Newsreader 26/38
        val textPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            typeface = StyleTypefaces.getNewsreader()
            textSize = 24f * s
        }
        val layout = StaticLayout.Builder.obtain(restVerse, 0, restVerse.length, textPaint, (250f * s).toInt())
            .setIncludePad(false)
            .setLineSpacing(0f, 1.25f)
            .build()
        canvas.save()
        canvas.translate(68f * s, 360f * s)
        layout.draw(canvas)
        canvas.restore()

        // Short italic declaration line at the bottom
        val declPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            typeface = StyleTypefaces.getNewsreaderItalic()
            textSize = 15f * s
            textAlign = AndroidPaint.Align.CENTER
        }
        canvas.drawText("“${spec.declaration}”", 195f * s, 745f * s, declPaint)
    }
}

// ---------------------------------------------------------------------------
// 9. EIGHT LIGHTS STYLE (Dotted Semicircle with 8 Day Orbs)
// ---------------------------------------------------------------------------
object EightLightsStyle : BaseStyle("eight_lights", "Eight Lights") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawLight(Light.Night, Size(w, h), grain = false)

        val cx = if (geom.isLandscapeX) w * 0.74f else 195f * s
        val cy = if (geom.isSquare) h * 0.65f else 430f * s
        val r = 148f * s

        // Dotted gold semicircle
        scope.drawIntoCanvas { c ->
            val arcPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = Color(0xFFC9964A).toArgb()
                style = AndroidPaint.Style.STROKE
                strokeWidth = 1.6f * s
                pathEffect = DashPathEffect(floatArrayOf(4f * s, 6f * s), 0f)
            }
            c.nativeCanvas.drawArc(
                RectF(cx - r, cy - r, cx + r, cy + r),
                180f, 180f, false, arcPaint
            )
        }

        // 8 Lights in day order: Dawn, Mist, Rain, Midday, Ember, Dusk, Grove, Night
        val order = listOf(
            Light.Dawn, Light.Mist, Light.Rain, Light.Midday,
            Light.Ember, Light.Dusk, Light.Grove, Light.Night
        )
        order.forEachIndexed { i, l ->
            val angle = PI * (1.0 - (i.toDouble() / 7.0))
            val x = cx + cos(angle).toFloat() * r
            val y = cy - sin(angle).toFloat() * r
            val isCurrent = l == light

            if (isCurrent) {
                // Current light: r34 with ring and glow
                scope.drawCircle(l.glow.copy(alpha = 0.5f), 48f * s, Offset(x, y))
                scope.drawCircle(l.disc.copy(alpha = 0.95f), 34f * s, Offset(x, y))
                scope.drawCircle(Color(0xFFC9964A), 37f * s, Offset(x, y), style = androidx.compose.ui.graphics.drawscope.Stroke(2f * s))
            } else {
                // Others: r17
                scope.drawCircle(l.disc.copy(alpha = 0.85f), 17f * s, Offset(x, y))
            }
        }

        // Label: "THE EIGHT LIGHTS" in caps
        scope.drawIntoCanvas { c ->
            val labelP = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = Color(0xFFC9964A).toArgb()
                textAlign = AndroidPaint.Align.CENTER
                textSize = 12f * s
                letterSpacing = 0.20f
                typeface = StyleTypefaces.getHankenGrotesk()
            }
            c.nativeCanvas.drawText("THE EIGHT LIGHTS", cx, cy - r - 20f * s, labelP)
        }
        scope.drawGrainOverlay(Light.Night, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = if (geom.isSquare) geom.topSafe else if (geom.isLandscapeX) geom.topSafe else geom.height * 0.58f
        drawAdaptivePairing(canvas, bitmap, geom, Light.Night, spec, declarationSizeSp = 28f, verseSizeSp = 15f, topOverride = top, centerAlign = !geom.isLandscapeX)
    }
}

// ---------------------------------------------------------------------------
// 10. CROSS STYLE (Dark Ember Wall & Luminous Cross)
// ---------------------------------------------------------------------------
object CrossStyle : BaseStyle("cross", "Cross") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        // Dark Ember wall #2A1410 to #150A08
        scope.drawRect(Brush.verticalGradient(listOf(Color(0xFF2A1410), Color(0xFF150A08))), size = Size(w, h))

        // Floor #3A1C14 to #0E0605
        val floorY = if (geom.isSquare) h * 0.68f else h * 0.62f
        scope.drawRect(
            Brush.verticalGradient(listOf(Color(0xFF3A1C14), Color(0xFF0E0605))),
            topLeft = Offset(0f, floorY),
            size = Size(w, h - floorY)
        )

        val crossX = if (geom.isLandscapeX) w * 0.72f else 195f * s
        val crossY = if (geom.isSquare) h * 0.35f else 285f * s

        // Blurred halo (26dp)
        scope.drawIntoCanvas { c ->
            val halo = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.argb(120, 255, 230, 180)
                maskFilter = BlurMaskFilter(26f * s, BlurMaskFilter.Blur.NORMAL)
            }
            c.nativeCanvas.drawCircle(crossX, crossY, 90f * s, halo)
        }

        // Two translucent beam trapezoids falling to the floor
        val beamPath = Path().apply {
            moveTo(crossX - 50f * s, crossY)
            lineTo(crossX + 50f * s, crossY)
            lineTo(crossX + 160f * s, floorY + 120f * s)
            lineTo(crossX - 160f * s, floorY + 120f * s)
            close()
        }
        scope.drawPath(beamPath, Color(0x22FFF1D6))

        // Lit floor patch
        scope.drawCircle(Color(0x33FFC98A), 110f * s, Offset(crossX, floorY + 40f * s))

        // Luminous cross: vertical 46x330 at x=172,y=120; horizontal 190x46 at x=100,y=215 (fill #FFF1D6 to #FFC98A)
        val crossGrad = Brush.verticalGradient(listOf(Color(0xFFFFF1D6), Color(0xFFFFC98A)))
        val vertRect = androidx.compose.ui.geometry.Rect(crossX - 23f * s, crossY - 165f * s, crossX + 23f * s, crossY + 165f * s)
        val horizRect = androidx.compose.ui.geometry.Rect(crossX - 95f * s, crossY - 70f * s, crossX + 95f * s, crossY - 24f * s)

        scope.drawRect(crossGrad, topLeft = Offset(vertRect.left, vertRect.top), size = Size(vertRect.width, vertRect.height))
        scope.drawRect(crossGrad, topLeft = Offset(horizRect.left, horizRect.top), size = Size(horizRect.width, horizRect.height))

        scope.drawGrainOverlay(Light.Ember, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = if (geom.isLandscapeX) geom.topSafe else geom.height * 0.72f
        drawAdaptivePairing(
            canvas, bitmap, geom, Light.Ember, spec,
            declarationSizeSp = 26f, verseSizeSp = 15f,
            topOverride = top, centerAlign = !geom.isLandscapeX
        )
    }
}

// ---------------------------------------------------------------------------
// 11. PATH STYLE (Dawn Sky & Perspective Road)
// ---------------------------------------------------------------------------
object PathStyle : BaseStyle("path", "Path") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawLight(Light.Dawn, Size(w, h), grain = false)

        val vanX = if (geom.isLandscapeX) w * 0.74f else 195f * s
        val vanY = if (geom.isSquare) h * 0.58f else 430f * s

        // Green fields
        val field = Path().apply {
            moveTo(0f, vanY)
            lineTo(w, vanY)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        scope.drawPath(field, Color(0xFF4E6A3D))

        // Sun at the horizon
        scope.drawCircle(Color(0xFFFFE8B8), 32f * s, Offset(vanX, vanY - 6f * s))

        // Perspective road
        val road = Path().apply {
            moveTo(vanX - 10f * s, vanY)
            lineTo(vanX + 10f * s, vanY)
            lineTo(vanX + 120f * s, h)
            lineTo(vanX - 120f * s, h)
            close()
        }
        scope.drawPath(road, Brush.verticalGradient(listOf(Color(0xFFB89068), Color(0xFF8C6642))))

        // Dashed centre line growing with perspective
        var curY = h * 0.98f
        var dashLen = 42f * s
        var dashW = 5f * s
        while (curY > vanY + 14f * s) {
            scope.drawLine(Color(0xFFFFF4D6), Offset(vanX, curY), Offset(vanX, curY - dashLen), dashW)
            curY -= dashLen * 1.8f
            dashLen *= 0.72f
            dashW = (dashW * 0.76f).coerceAtLeast(1.2f * s)
        }

        // Tree silhouettes along the road edges
        val treeColor = Color(0xFF2A3820)
        listOf(
            Offset(vanX - 35f * s, vanY + 20f * s) to 8f * s,
            Offset(vanX + 38f * s, vanY + 22f * s) to 9f * s,
            Offset(vanX - 85f * s, vanY + 95f * s) to 22f * s,
            Offset(vanX + 90f * s, vanY + 100f * s) to 24f * s
        ).forEach { (pos, r) ->
            scope.drawCircle(treeColor, r, pos)
        }

        scope.drawGrainOverlay(Light.Dawn, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = geom.topSafe + 8f * geom.scale
        drawAdaptivePairing(canvas, bitmap, geom, Light.Dawn, spec, declarationSizeSp = 30f, verseSizeSp = 15f, topOverride = top)
    }
}

// ---------------------------------------------------------------------------
// 12. TIDE STYLE (Mist Sun with Reflection & 16 Sine Waves)
// ---------------------------------------------------------------------------
object TideStyle : BaseStyle("tide", "Tide") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawLight(Light.Mist, Size(w, h), grain = false)

        val textBounds = measurePairingBounds(geom, Light.Mist, spec, declarationSizeSp = 30f, verseSizeSp = 16f)
        val safeSun = computeSafeDiscPosition(geom, Light.Mist, textBounds)
        val sunX = safeSun.x
        val sunY = safeSun.y

        // Sun with vertical reflection column
        scope.drawCircle(Color(0xFFF6F8F1), 32f * s, Offset(sunX, sunY))
        scope.drawRect(
            Brush.verticalGradient(listOf(Color(0x55F6F8F1), Color(0x00F6F8F1))),
            topLeft = Offset(sunX - 18f * s, sunY + 30f * s),
            size = Size(36f * s, h - (sunY + 30f * s))
        )

        // 16 layered sine waves (y from 330, step 30, amplitude 3 + 1.3*i, stroke #2A1B14 at 10% to 32%)
        val startY = if (geom.isSquare) h * 0.45f else 330f * s
        val stepY = if (geom.isSquare) h * 0.032f else 30f * s

        for (i in 0 until 16) {
            val y = startY + i * stepY
            val amp = (3f + 1.3f * i) * s
            val waveAlpha = 0.10f + (i / 15f) * 0.22f
            val path = Path().apply {
                moveTo(0f, y)
                cubicTo(w * 0.25f, y - amp, w * 0.75f, y + amp, w, y)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            // Alternate waves filled with translucent sea green
            if (i % 2 == 1) {
                scope.drawPath(path, Color(0x334E7A6B))
            }
            scope.drawPath(path, Color(0xFF2A1B14).copy(alpha = waveAlpha), style = androidx.compose.ui.graphics.drawscope.Stroke((1f + i * 0.12f) * s))
        }

        scope.drawGrainOverlay(Light.Mist, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = geom.topSafe + 10f * geom.scale
        drawAdaptivePairing(canvas, bitmap, geom, Light.Mist, spec, declarationSizeSp = 30f, verseSizeSp = 15f, topOverride = top)
    }
}

// ---------------------------------------------------------------------------
// 13. FOR YOU (GIFT) STYLE
// ---------------------------------------------------------------------------
object ForYouStyle : BaseStyle("for_you", "For you") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val rewrittenDecl = if (spec.isPronounRewritten) spec.declaration else com.makarios.app.util.PronounRewriter.rewrite(spec.declaration)
        val giftSpec = spec.copy(declaration = rewrittenDecl)
        val textBounds = measurePairingBounds(geom, light, giftSpec, declarationSizeSp = 30f, verseSizeSp = 15f)
        val safeDiscPos = computeSafeDiscPosition(geom, light, textBounds)
        scope.drawLight(light, Size(geom.width, geom.height), discPos = safeDiscPos)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val textWidth = (if (geom.isLandscapeX) (geom.width * 0.55f - geom.margin * 2f) else (geom.width - geom.margin * 2f)).coerceAtLeast(100f)
        val startX = geom.margin
        var curY = geom.topSafe + 12f * s

        val hanken = StyleTypefaces.getHankenGrotesk()
        val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()

        // Eyebrow: "I DECLARED THIS OVER YOU"
        val eyebrowPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = hanken
            textSize = 11f * s
            color = light.anchorRule.toArgb()
            letterSpacing = 0.12f
            isSubpixelText = true
        }
        val eyebrowLayout = runCatching {
            StaticLayout.Builder.obtain("I DECLARED THIS OVER YOU", 0, 24, eyebrowPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        eyebrowLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 6f * s
        }

        // Recipient line: "For Ada" or "For you"
        val recipientText = if (!spec.recipientName.isNullOrBlank()) "For ${spec.recipientName.trim()}" else "For you"
        val recipientPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = newsreaderItalic
            textSize = 22f * s
            color = light.text.toArgb()
            isSubpixelText = true
        }
        val recipientLayout = runCatching {
            StaticLayout.Builder.obtain(recipientText, 0, recipientText.length, recipientPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        recipientLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 16f * s
        }

        val rewrittenDecl = if (spec.isPronounRewritten) spec.declaration else com.makarios.app.util.PronounRewriter.rewrite(spec.declaration)
        val giftSpec = spec.copy(declaration = rewrittenDecl)
        drawAdaptivePairing(canvas, bitmap, geom, light, giftSpec, declarationSizeSp = 30f, verseSizeSp = 15f, topOverride = curY)
    }
}

// ---------------------------------------------------------------------------
// 14. POSTCARD STYLE
// ---------------------------------------------------------------------------
object PostcardStyle : BaseStyle("postcard", "Postcard") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        // Background paper tone
        scope.drawRect(if (light.dark) Color(0xFF141C1B) else Color(0xFFFAF7F2), size = Size(w, h))

        // Inset border 1.5px
        val inset = 18f * s
        scope.drawRect(
            color = light.text.copy(alpha = 0.20f),
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2f, h - inset * 2f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * s)
        )

        // Bottom landscape scene (lower 36%)
        val sceneTop = h * 0.64f
        val sceneHeight = h - sceneTop - inset
        scope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(light.top.copy(alpha = 0.45f), light.bottom.copy(alpha = 0.90f)),
                startY = sceneTop,
                endY = h - inset
            ),
            topLeft = Offset(inset, sceneTop),
            size = Size(w - inset * 2f, sceneHeight)
        )

        // Mountain ridge inside bottom scene
        scope.drawRidgePath(w - inset * 2f, h - inset, sceneTop + sceneHeight * 0.5f, 22f * s, spec.seed, light.ridge1.copy(alpha = 0.85f))

        // Sun in bottom scene
        scope.drawCircle(light.disc, 26f * s, Offset(w * 0.72f, sceneTop + 32f * s))

        scope.drawGrainOverlay(light, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val s = geom.scale
        val inset = 18f * s

        // Dashed stamp box top right holding the mark
        val stampW = 46f * s
        val stampH = 56f * s
        val stampX = w - inset - stampW - 12f * s
        val stampY = inset + 12f * s

        val stampPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.text.copy(alpha = 0.35f).toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1.2f * s
            pathEffect = DashPathEffect(floatArrayOf(4f * s, 3f * s), 0f)
        }
        canvas.drawRect(stampX, stampY, stampX + stampW, stampY + stampH, stampPaint)

        // Mark centered inside stamp
        val markPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1.5f * s
            strokeCap = AndroidPaint.Cap.ROUND
        }
        val cx = stampX + stampW / 2f
        val cy = stampY + stampH * 0.55f
        val ms = stampW * 0.45f
        canvas.drawLine(cx - ms / 2f, cy + ms * 0.35f, cx + ms / 2f, cy + ms * 0.35f, markPaint)
        val archPath = AndroidPath().apply {
            moveTo(cx - ms * 0.35f, cy + ms * 0.35f)
            lineTo(cx - ms * 0.35f, cy - ms * 0.1f)
            arcTo(AndroidRectF(cx - ms * 0.35f, cy - ms * 0.45f, cx + ms * 0.35f, cy + ms * 0.25f), 180f, 180f)
            lineTo(cx + ms * 0.35f, cy + ms * 0.35f)
        }
        canvas.drawPath(archPath, markPaint)

        // Text in upper-middle area (between stamp and bottom scene)
        val textTop = inset + 40f * s
        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 28f, verseSizeSp = 14f, topOverride = textTop)
    }
}

// ---------------------------------------------------------------------------
// 15. STREAK STYLE
// ---------------------------------------------------------------------------
object StreakStyle : BaseStyle("streak", "Streak") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val s = geom.scale
        val textWidth = (if (geom.isLandscapeX) (w * 0.55f - geom.margin * 2f) else (w - geom.margin * 2f)).coerceAtLeast(100f)
        val startX = geom.margin
        var curY = geom.topSafe + 16f * s

        val newsreader = StyleTypefaces.getNewsreader()
        val hanken = StyleTypefaces.getHankenGrotesk()

        // Giant milestone numeral (e.g. 30, 100, 365)
        val milestoneNumber = (spec.milestoneDays ?: 30).toString()
        val numeralSizePx = min(w * 0.32f, 130f * s)
        val numeralPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = Typeface.create(newsreader, Typeface.BOLD)
            textSize = numeralSizePx
            color = light.anchorRule.toArgb()
            isSubpixelText = true
        }
        val numeralLayout = runCatching {
            StaticLayout.Builder.obtain(milestoneNumber, 0, milestoneNumber.length, numeralPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        numeralLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 4f * s
        }

        // Caption: "DAYS OF DECLARING"
        val captionPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = light.text.copy(alpha = 0.85f).toArgb()
            letterSpacing = 0.16f
            isSubpixelText = true
        }
        val captionLayout = runCatching {
            StaticLayout.Builder.obtain("DAYS OF DECLARING", 0, 17, captionPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        captionLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 16f * s
        }

        // 28x2dp rule
        val rulePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(startX, curY, startX + 28f * s, curY + 2f * s, rulePaint)
        curY += 2f * s + 18f * s

        // Declaration & reference
        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 30f, verseSizeSp = 15f, topOverride = curY)
    }
}

// ---------------------------------------------------------------------------
// 16. STICKER STYLE (Transparent ARGB PNG with rounded cream card)
// ---------------------------------------------------------------------------
object StickerStyle : BaseStyle("sticker", "Sticker") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        // Outer canvas is completely transparent for Instagram Stories sticker overlay!
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        val cardMargin = w * 0.08f
        val cardWidth = w - cardMargin * 2f
        val cardHeight = min(h * 0.65f, 520f * s)
        val cardTop = (h - cardHeight) / 2f
        val radius = 26f * s

        // Cream rounded card
        val cardColor = if (light.dark) Color(0xFF1E2827) else Color(0xFFFFF6E9)
        scope.drawRoundRect(
            color = cardColor,
            topLeft = Offset(cardMargin, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )

        // Subtle 1.5dp inner border
        scope.drawRoundRect(
            color = light.text.copy(alpha = 0.12f),
            topLeft = Offset(cardMargin, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f * s)
        )

        // Mini horizon landscape band inside card at bottom
        val miniSceneH = cardHeight * 0.28f
        val miniSceneTop = cardTop + cardHeight - miniSceneH
        scope.drawRoundRect(
            brush = Brush.verticalGradient(listOf(light.top.copy(alpha = 0.35f), light.bottom.copy(alpha = 0.65f))),
            topLeft = Offset(cardMargin + 2f * s, miniSceneTop),
            size = Size(cardWidth - 4f * s, miniSceneH - 2f * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.8f, radius * 0.8f)
        )
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        val cardMargin = w * 0.08f
        val cardWidth = w - cardMargin * 2f
        val cardHeight = min(h * 0.65f, 520f * s)
        val cardTop = (h - cardHeight) / 2f

        val innerPadding = 24f * s
        val textWidth = cardWidth - innerPadding * 2f
        val startX = cardMargin + innerPadding
        val startY = cardTop + innerPadding

        drawAdaptivePairing(
            canvas, bitmap, geom, light, spec,
            declarationSizeSp = 28f,
            verseSizeSp = 14f,
            topOverride = startY,
            textWidthOverride = textWidth
        )
    }
}

// ---------------------------------------------------------------------------
// 17. POLAROID STYLE (Tilted -2deg off-white frame)
// ---------------------------------------------------------------------------
object PolaroidStyle : BaseStyle("polaroid", "Polaroid") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        // Off-white paper background
        scope.drawRect(Color(0xFFF4EFEA), size = Size(w, h))
        scope.drawGrainOverlay(Light.Dawn, Size(w, h))

        // Center coordinates for rotated frame
        val cx = w / 2f
        val cy = h / 2f
        val cardW = w * 0.82f
        val cardH = min(h * 0.74f, 600f * s)
        val photoPadding = 18f * s
        val photoW = cardW - photoPadding * 2f
        val photoH = photoW * 0.95f

        scope.drawIntoCanvas { c ->
            val ac = c.nativeCanvas
            ac.save()
            ac.rotate(-2f, cx, cy)

            // Polaroid shadow & white card
            val left = cx - cardW / 2f
            val top = cy - cardH / 2f
            val cardPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.WHITE
                setShadowLayer(14f * s, 0f, 6f * s, AndroidColor.argb(45, 30, 20, 15))
            }
            ac.drawRoundRect(AndroidRectF(left, top, left + cardW, top + cardH), 12f * s, 12f * s, cardPaint)

            // Inner photo window
            val photoLeft = left + photoPadding
            val photoTop = top + photoPadding
            val photoRect = AndroidRectF(photoLeft, photoTop, photoLeft + photoW, photoTop + photoH)
            ac.save()
            ac.clipRect(photoRect)

            // Render mini Light landscape into the photo cutout
            val photoBmp = Bitmap.createBitmap(photoW.toInt().coerceAtLeast(10), photoH.toInt().coerceAtLeast(10), Bitmap.Config.ARGB_8888)
            val photoCanvas = AndroidCanvas(photoBmp)
            val miniGeom = LayoutGeometry.compute(IntSize(photoW.toInt(), photoH.toInt()))
            val miniScope = CanvasDrawScope()
            miniScope.draw(Density(1f), LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(photoCanvas), Size(photoW, photoH)) {
                drawLight(light, Size(photoW, photoH))
            }
            ac.drawBitmap(photoBmp, photoLeft, photoTop, null)
            photoBmp.recycle()
            ac.restore()

            // Photo frame inner border
            val photoBorderPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.argb(25, 0, 0, 0)
                style = AndroidPaint.Style.STROKE
                strokeWidth = 1f * s
            }
            ac.drawRect(photoRect, photoBorderPaint)

            ac.restore()
        }
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        val cx = w / 2f
        val cy = h / 2f
        val cardW = w * 0.82f
        val cardH = min(h * 0.74f, 600f * s)
        val photoPadding = 18f * s
        val photoW = cardW - photoPadding * 2f
        val photoH = photoW * 0.95f

        val left = cx - cardW / 2f
        val top = cy - cardH / 2f
        val captionTop = top + photoPadding + photoH + 16f * s
        val textWidth = (cardW - photoPadding * 2f).toInt()

        val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()
        val hanken = StyleTypefaces.getHankenGrotesk()

        canvas.save()
        canvas.rotate(-2f, cx, cy)

        // Declaration as polaroid bottom caption
        val captionPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = newsreaderItalic
            textSize = 21f * s
            color = AndroidColor.rgb(38, 26, 20)
            isSubpixelText = true
        }
        val captionLayout = runCatching {
            StaticLayout.Builder.obtain(spec.declaration, 0, spec.declaration.length, captionPaint, textWidth)
                .setIncludePad(false).build()
        }.getOrNull()

        var currentY = captionTop
        captionLayout?.let {
            canvas.save()
            canvas.translate(left + photoPadding, currentY)
            it.draw(canvas)
            canvas.restore()
            currentY += it.height + 6f * s
        }

        // Reference
        val refText = (spec.reference ?: "").uppercase()
        if (refText.isNotBlank()) {
            val refPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
                typeface = hanken
                textSize = 10f * s
                color = AndroidColor.argb(170, 70, 50, 40)
                letterSpacing = 0.10f
                isSubpixelText = true
            }
            val refLayout = runCatching {
                StaticLayout.Builder.obtain(refText, 0, refText.length, refPaint, textWidth)
                    .setIncludePad(false).build()
            }.getOrNull()
            refLayout?.let {
                canvas.save()
                canvas.translate(left + photoPadding, currentY)
                it.draw(canvas)
                canvas.restore()
            }
        }

        canvas.restore()
    }
}

// ---------------------------------------------------------------------------
// 18. VERSE FIRST STYLE (Scripture leads in large italic, then "AND I SAY")
// ---------------------------------------------------------------------------
object VerseFirstStyle : BaseStyle("verse_first", "Verse first") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val textBounds = measurePairingBounds(geom, light, spec, declarationSizeSp = 30f, verseSizeSp = 28f)
        val safeDiscPos = computeSafeDiscPosition(geom, light, textBounds)
        scope.drawLight(light, Size(geom.width, geom.height), discPos = safeDiscPos)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val textWidth = (if (geom.isLandscapeX) (geom.width * 0.55f - geom.margin * 2f) else (geom.width - geom.margin * 2f)).coerceAtLeast(100f)
        val startX = geom.margin
        var curY = geom.topSafe + 16f * s

        val newsreader = StyleTypefaces.getNewsreader()
        val newsreaderItalic = StyleTypefaces.getNewsreaderItalic()
        val hanken = StyleTypefaces.getHankenGrotesk()

        // 1. Scripture verse leads in prominent italic
        val verseText = spec.verse ?: "The Lord is my light and my salvation; whom shall I fear?"
        val versePaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = newsreaderItalic
            textSize = 28f * s
            color = light.text.toArgb()
            isSubpixelText = true
        }
        val verseLayout = runCatching {
            StaticLayout.Builder.obtain("“$verseText”", 0, verseText.length + 2, versePaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        verseLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 8f * s
        }

        // Reference
        val refText = (spec.reference ?: "").uppercase()
        if (refText.isNotBlank()) {
            val refPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
                typeface = hanken
                textSize = 11f * s
                color = light.anchorRule.toArgb()
                letterSpacing = 0.12f
                isSubpixelText = true
            }
            val refLayout = runCatching {
                StaticLayout.Builder.obtain(refText, 0, refText.length, refPaint, textWidth.toInt())
                    .setIncludePad(false).build()
            }.getOrNull()
            refLayout?.let {
                canvas.save()
                canvas.translate(startX, curY)
                it.draw(canvas)
                canvas.restore()
                curY += it.height + 20f * s
            }
        }

        // 2. Transition label: "AND I SAY"
        val labelPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = light.anchorRule.toArgb()
            letterSpacing = 0.16f
            isSubpixelText = true
        }
        val labelLayout = runCatching {
            StaticLayout.Builder.obtain("AND I SAY", 0, 9, labelPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        labelLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 8f * s
        }

        // 3. Declaration beneath
        val declPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = Typeface.create(newsreader, Typeface.BOLD)
            textSize = 28f * s
            color = light.text.toArgb()
            isSubpixelText = true
        }
        val declLayout = runCatching {
            StaticLayout.Builder.obtain(spec.declaration, 0, spec.declaration.length, declPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        declLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
        }
    }
}

// ---------------------------------------------------------------------------
// 19. THE HOUR STYLE (Large timestamp + date stamp + pairing)
// ---------------------------------------------------------------------------
object TheHourStyle : BaseStyle("the_hour", "The hour") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val textWidth = (if (geom.isLandscapeX) (geom.width * 0.55f - geom.margin * 2f) else (geom.width - geom.margin * 2f)).coerceAtLeast(100f)
        val startX = geom.margin
        var curY = geom.topSafe + 14f * s

        val newsreader = StyleTypefaces.getNewsreader()
        val hanken = StyleTypefaces.getHankenGrotesk()

        // 1. Large time ("6:02")
        val timeString = spec.hourFormatted ?: "6:02"
        val timePaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = Typeface.create(newsreader, Typeface.BOLD)
            textSize = 58f * s
            color = light.text.toArgb()
            isSubpixelText = true
        }
        val timeLayout = runCatching {
            StaticLayout.Builder.obtain(timeString, 0, timeString.length, timePaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        timeLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 4f * s
        }

        // 2. Date / Light stamp ("DAWN, SAT 10 OCT")
        val stampString = "${light.name.uppercase()}, ${spec.timestampFormatted ?: "SAT 10 OCT"}"
        val stampPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = hanken
            textSize = 12f * s
            color = light.anchorRule.toArgb()
            letterSpacing = 0.14f
            isSubpixelText = true
        }
        val stampLayout = runCatching {
            StaticLayout.Builder.obtain(stampString, 0, stampString.length, stampPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        stampLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 14f * s
        }

        // 3. 28x2dp rule
        val rulePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = light.anchorRule.toArgb() }
        canvas.drawRect(startX, curY, startX + 28f * s, curY + 2f * s, rulePaint)
        curY += 2f * s + 18f * s

        // 4. Pairing beneath
        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 30f, verseSizeSp = 15f, topOverride = curY)
    }
}

// ---------------------------------------------------------------------------
// 20. CAROUSEL RECAP STYLE (Multi-card weekly recap cover)
// ---------------------------------------------------------------------------
object CarouselRecapStyle : BaseStyle("carousel_recap", "Carousel (recap)") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        scope.drawLight(light, Size(w, h))

        // Three mini framed cards side-by-side or cascading in the lower half
        val cardW = (w - geom.margin * 2f - 24f * s) / 3f
        val cardH = cardW * 1.5f
        val cardTop = h * 0.58f

        for (i in 0 until 3) {
            val cardLeft = geom.margin + i * (cardW + 12f * s)
            // Mini card background
            scope.drawRoundRect(
                color = light.bottom.copy(alpha = 0.85f),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW, cardH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f * s, 12f * s)
            )
            // Mini card border
            scope.drawRoundRect(
                color = light.text.copy(alpha = 0.18f),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW, cardH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f * s, 12f * s),
                style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f * s)
            )
            // Mini sun inside each card
            scope.drawCircle(
                color = light.disc.copy(alpha = 0.70f),
                radius = 8f * s,
                center = Offset(cardLeft + cardW * 0.5f, cardTop + 24f * s)
            )
        }
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val textWidth = (if (geom.isLandscapeX) (geom.width * 0.55f - geom.margin * 2f) else (geom.width - geom.margin * 2f)).coerceAtLeast(100f)
        val startX = geom.margin
        var curY = geom.topSafe + 16f * s

        val newsreader = StyleTypefaces.getNewsreader()
        val hanken = StyleTypefaces.getHankenGrotesk()

        // Eyebrow: "WEEKLY RECAP"
        val eyebrowPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = hanken
            textSize = 11f * s
            color = light.anchorRule.toArgb()
            letterSpacing = 0.14f
            isSubpixelText = true
        }
        val eyebrowLayout = runCatching {
            StaticLayout.Builder.obtain("WEEKLY RECAP", 0, 12, eyebrowPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        eyebrowLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 8f * s
        }

        // Heading: "Three things I declared this week"
        val count = spec.recapDeclarations?.size ?: 3
        val headingTitle = "$count things I declared this week"
        val headingPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = Typeface.create(newsreader, Typeface.BOLD)
            textSize = 34f * s
            color = light.text.toArgb()
            isSubpixelText = true
        }
        val headingLayout = runCatching {
            StaticLayout.Builder.obtain(headingTitle, 0, headingTitle.length, headingPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        headingLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 16f * s
        }

        // Subtitle declaration
        drawAdaptivePairing(canvas, bitmap, geom, light, spec, declarationSizeSp = 24f, verseSizeSp = 14f, topOverride = curY)
    }
}

// ---------------------------------------------------------------------------
// 21. UNDERLINED STYLE (Huge declaration + hand-drawn cubic Bezier stroke)
// ---------------------------------------------------------------------------
object UnderlinedStyle : BaseStyle("underlined", "Underlined") {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val textBounds = measurePairingBounds(geom, light, spec, declarationSizeSp = 38f, verseSizeSp = 16f)
        val safeDiscPos = computeSafeDiscPosition(geom, light, textBounds)
        scope.drawLight(light, Size(geom.width, geom.height), discPos = safeDiscPos)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val textWidth = (if (geom.isLandscapeX) (geom.width * 0.55f - geom.margin * 2f) else (geom.width - geom.margin * 2f)).coerceAtLeast(100f)
        val startX = geom.margin
        var curY = geom.topSafe + 16f * s

        val newsreader = StyleTypefaces.getNewsreader()

        // 1. Huge declaration
        val declPaint = TextPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
            typeface = Typeface.create(newsreader, Typeface.BOLD)
            textSize = 38f * s
            color = light.text.toArgb()
            isSubpixelText = true
        }
        val declLayout = runCatching {
            StaticLayout.Builder.obtain(spec.declaration, 0, spec.declaration.length, declPaint, textWidth.toInt())
                .setIncludePad(false).build()
        }.getOrNull()

        var declW = textWidth
        declLayout?.let {
            canvas.save()
            canvas.translate(startX, curY)
            it.draw(canvas)
            canvas.restore()
            curY += it.height + 8f * s
            declW = min(textWidth, it.getLineWidth(0) + 12f * s)
        }

        // 2. Hand-drawn cubic Bezier stroke in sun colour
        val rng = Random(spec.seed)
        val wave1 = (rng.nextFloat() * 4f - 2f) * s
        val wave2 = (rng.nextFloat() * 4f - 2f) * s
        val wave3 = (rng.nextFloat() * 3f - 1.5f) * s

        val strokePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 3.6f * s
            strokeCap = AndroidPaint.Cap.ROUND
        }
        val strokePath = AndroidPath().apply {
            moveTo(startX, curY)
            cubicTo(
                startX + declW * 0.35f, curY + 4f * s + wave1,
                startX + declW * 0.65f, curY - 3f * s + wave2,
                startX + declW, curY + 2f * s + wave3
            )
        }
        canvas.drawPath(strokePath, strokePaint)
        curY += 24f * s

        // 3. Verse and reference beneath
        drawAdaptivePairing(
            canvas, bitmap, geom, light, spec,
            declarationSizeSp = 30f,
            verseSizeSp = 16f,
            topOverride = curY,
            includeDeclaration = false
        )
    }
}

