package com.makarios.app.ui.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
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

    fun getNewsreader(): Typeface = newsreader ?: Typeface.SERIF
    fun getNewsreaderItalic(): Typeface = newsreaderItalic ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
    fun getHankenGrotesk(): Typeface = hankenGrotesk ?: Typeface.SANS_SERIF
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
            val s = w / 390f
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
                ExportFormat.Portrait -> w * 0.072f // 10% tighter margins than Story
                ExportFormat.X -> w * 0.06f
                ExportFormat.Square -> w * 0.08f
                else -> w * 0.08f
            }
            val topSafe = when (format) {
                ExportFormat.Story -> h * 0.14f // 14% top safe zone
                ExportFormat.Wallpaper -> h * 0.22f // Top 22% free for lock screen clock
                ExportFormat.Square -> h * 0.09f
                ExportFormat.Portrait -> h * 0.10f
                ExportFormat.X -> h * 0.10f
            }
            val bottomSafe = when (format) {
                ExportFormat.Story, ExportFormat.Wallpaper -> h * 0.86f // 14% bottom safe zone
                ExportFormat.Square -> h * 0.91f
                ExportFormat.Portrait -> h * 0.88f
                ExportFormat.X -> h * 0.90f
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

/** Draws smooth mountain ridges with seed-deterministic Bezier curves. */
fun DrawScope.drawRidgePath(w: Float, h: Float, y0: Float, amp: Float, seed: Int, color: Color) {
    val r = Random(seed)
    val points = (0..6).map { i ->
        Offset(w * i / 6f, y0 + amp * sin(i * 1.3f + seed) + (r.nextFloat() - 0.5f) * 0.8f * amp)
    }
    val path = Path().apply {
        moveTo(0f, h)
        lineTo(points[0].x, points[0].y)
        for (i in 0 until 6) {
            val m = Offset((points[i].x + points[i + 1].x) / 2f, (points[i].y + points[i + 1].y) / 2f)
            quadraticBezierTo(points[i].x, points[i].y, m.x, m.y)
        }
        lineTo(points.last().x, points.last().y)
        lineTo(w, h)
        close()
    }
    drawPath(path, color)
}

/** Helper to draw the subtle liturgical wordmark at the bottom. */
fun drawWordmark(
    canvas: AndroidCanvas,
    geom: LayoutGeometry,
    light: Light,
    include: Boolean = true
) {
    if (!include) return
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.DITHER_FLAG).apply {
        color = light.text.copy(alpha = 0.72f).toArgb()
        typeface = StyleTypefaces.getNewsreaderItalic()
        textSize = 15f * geom.scale
        textAlign = if (geom.isLandscapeX) AndroidPaint.Align.LEFT else AndroidPaint.Align.CENTER
        isSubpixelText = true
    }
    val x = if (geom.isLandscapeX) geom.margin else geom.width / 2f
    val y = geom.height * 0.94f
    canvas.drawText("makarios", x, y, paint)
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
    val minDeclPx = 16f * s
    var dl = if (includeDeclaration) makeLayout(spec.declaration, Typeface.create(newsreader, Typeface.BOLD), declPx, if (centerAlign) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL) else null

    // Autofit declaration lines
    while (dl != null && dl.height > geom.height * 0.38f && declPx > minDeclPx) {
        declPx -= 1.5f * s
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
        vPx -= 1f * s
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
        scope.drawLight(light, Size(geom.width, geom.height))
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
            val numPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(type, Typeface.BOLD)
                textSize = (if (geom.isLandscapeX) 260f else if (geom.isSquare) 280f else 380f) * s
                textAlign = AndroidPaint.Align.CENTER
            }
            val numX = if (geom.isLandscapeX) w * 0.74f else w * 0.5f
            val numY = if (geom.isLandscapeX) h * 0.62f else if (geom.isSquare) h * 0.72f else h * 0.51f

            val path = AndroidPath()
            numPaint.getTextPath(chapter, 0, chapter.length, numX, numY, path)

            c.nativeCanvas.save()
            c.nativeCanvas.clipPath(path)

            // Mini sunset landscape inside the clipped numeral
            val gradPaint = AndroidPaint().apply {
                shader = LinearGradient(0f, numY - 300f * s, 0f, numY + 100f * s, Color(0xFFFBE3C4).toArgb(), Color(0xFF6B2E1E).toArgb(), Shader.TileMode.CLAMP)
            }
            c.nativeCanvas.drawPaint(gradPaint)
            c.nativeCanvas.drawCircle(numX + 70f * s, numY - 60f * s, 40f * s, AndroidPaint().apply { color = Color(0xFFFFD9A8).toArgb() })

            // Mini ridges
            scope.drawRidgePath(w, h, numY + 10f * s, 14f * s, 3, Color(0xFF6B2E1E))
            scope.drawRidgePath(w, h, numY + 45f * s, 12f * s, 5, Color(0xFF432017))

            c.nativeCanvas.restore()
        }
        scope.drawGrainOverlay(Light.Midday, Size(w, h))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val top = if (geom.isSquare) geom.topSafe else if (geom.isLandscapeX) geom.topSafe else geom.height * 0.56f
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

        // Pale sun in sky
        val sunX = if (geom.isLandscapeX) w * 0.78f else w * 0.35f
        val sunY = if (geom.isSquare) h * 0.35f else h * 0.38f
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

        val sunX = if (geom.isLandscapeX) w * 0.74f else w * 0.5f
        val sunY = if (geom.isSquare) h * 0.32f else h * 0.30f

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
