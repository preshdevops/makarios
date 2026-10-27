package com.makarios.app.widget

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.wallpaper.StyleTypefaces
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class WidgetLayoutType {
    COMPACT_4X1, // One or two lines, declaration + reference + mark
    SMALL_2X2,   // Short declarations <= 32 chars + reference
    PAIRING_4X2, // Pairing: declaration + verse + reference
    SCENE_4X4    // Full scene: sun disc, light label, declaration, verse, reference, mark
}

object WidgetBitmapRenderer {

    // RemoteViews transaction memory limit: ~1.5 MB
    private const val MAX_BITMAP_BYTES = 1_500_000

    fun render(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        widthPx: Int,
        heightPx: Int,
        layoutType: WidgetLayoutType
    ): Bitmap {
        StyleTypefaces.init(context)

        // Safety against zero / invalid dimensions
        var w = max(widthPx, 100)
        var h = max(heightPx, 50)

        // Cap total bytes under RemoteViews limit
        val bytes = w * h * 4
        if (bytes > MAX_BITMAP_BYTES) {
            val scale = kotlin.math.sqrt(MAX_BITMAP_BYTES.toDouble() / bytes.toDouble()).toFloat()
            w = (w * scale).toInt()
            h = (h * scale).toInt()
        }

        // 0. Check disk cache first to keep memory footprint light
        val cacheDir = java.io.File(context.cacheDir, "widget_cache").apply { mkdirs() }
        val safeId = affirmation.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cacheFile = java.io.File(cacheDir, "${safeId}_${light.name}_${w}x${h}_${layoutType.name}.png")

        if (cacheFile.exists() && cacheFile.length() > 0) {
            val cached = BitmapFactory.decodeFile(cacheFile.absolutePath)
            if (cached != null) return cached
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val density = context.resources.displayMetrics.density

        // 1. Draw liturgical Light background
        drawBackground(canvas, w.toFloat(), h.toFloat(), light, layoutType)

        // 2. Draw layout content
        when (layoutType) {
            WidgetLayoutType.COMPACT_4X1 -> drawCompact4x1(canvas, w.toFloat(), h.toFloat(), affirmation, light, density)
            WidgetLayoutType.SMALL_2X2 -> drawSmall2x2(canvas, w.toFloat(), h.toFloat(), affirmation, light, density)
            WidgetLayoutType.PAIRING_4X2 -> drawPairing4x2(canvas, w.toFloat(), h.toFloat(), affirmation, light, density)
            WidgetLayoutType.SCENE_4X4 -> drawScene4x4(canvas, w.toFloat(), h.toFloat(), affirmation, light, density)
        }

        // Persist to disk cache
        runCatching {
            java.io.FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }

        return bitmap
    }

    private fun drawBackground(
        canvas: Canvas,
        w: Float,
        h: Float,
        light: Light,
        layoutType: WidgetLayoutType
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h,
                light.top.toArgb(), light.bottom.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Sun disc for 4x4 scene
        if (layoutType == WidgetLayoutType.SCENE_4X4) {
            val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = light.disc.toArgb()
                style = Paint.Style.FILL
            }
            val sunRadius = min(w, h) * 0.08f
            canvas.drawCircle(w * 0.78f, h * 0.22f, sunRadius, discPaint)
        }

        // Tactile paper grain
        if (com.makarios.app.util.PerformanceTier.grainEnabled) {
            val grainPaint = Paint().apply {
                shader = BitmapShader(
                    com.makarios.app.util.GrainTileProvider.androidBitmap,
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT
                )
                alpha = (255 * 0.04f).toInt()
            }
            canvas.drawRect(0f, 0f, w, h, grainPaint)
        }
    }

    /** Layout 1: 4x1 Compact (Doorway mark + declaration + reference) */
    private fun drawCompact4x1(
        canvas: Canvas,
        w: Float,
        h: Float,
        aff: Affirmation,
        light: Light,
        density: Float
    ) {
        val padX = w * 0.05f
        val padY = h * 0.12f
        val textColor = light.text.toArgb()
        val secColor = light.text.copy(alpha = light.secondaryAlpha).toArgb()

        // Art (sacred mark) on the left at fixed 56dp (capped only by widget height), text on the right, never overlapping
        val markSize = min(56f * density, h * 0.55f)
        drawSacredMark(canvas, padX, h * 0.5f - markSize * 0.5f, markSize, light)

        val contentLeft = padX + markSize + 12f * density
        val availableWidth = (w - contentLeft - padX).coerceAtLeast(50f)
        val availableHeight = h - padY * 2

        val serif = StyleTypefaces.getNewsreader() ?: Typeface.SERIF
        val sans = StyleTypefaces.getHankenGrotesk() ?: Typeface.SANS_SERIF

        // Text layout with auto step down in 2% increments
        val declText = com.makarios.app.data.widgetTextFor(aff.declaration, aff.shortText)
        val declLayout = autoStepDownLayout(
            text = declText,
            typeface = serif,
            color = textColor,
            maxWidth = availableWidth,
            maxHeight = availableHeight * 0.65f,
            startSp = 16f,
            minSp = 11f,
            density = density
        )

        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sans
            color = secColor
            textSize = 10f * density
        }

        canvas.save()
        val textTop = (h - (declLayout.height + 14f * density)) * 0.5f
        canvas.translate(contentLeft, textTop.coerceAtLeast(padY))
        declLayout.draw(canvas)
        canvas.drawText(aff.reference.uppercase(), 0f, declLayout.height + 12f * density, refPaint)
        canvas.restore()
    }

    /** Layout 2: 2x2 Small (Short declaration <= 32 chars + anchor rule + reference) */
    private fun drawSmall2x2(
        canvas: Canvas,
        w: Float,
        h: Float,
        aff: Affirmation,
        light: Light,
        density: Float
    ) {
        val pad = min(w, h) * 0.10f
        val textColor = light.text.toArgb()
        val secColor = light.text.copy(alpha = light.secondaryAlpha).toArgb()

        // Doorway mark top left
        val markSize = 16f * density
        drawSacredMark(canvas, pad, pad, markSize, light)

        val serif = StyleTypefaces.getNewsreader() ?: Typeface.SERIF
        val sans = StyleTypefaces.getHankenGrotesk() ?: Typeface.SANS_SERIF

        val declText = com.makarios.app.data.widgetTextFor(aff.declaration, aff.shortText)
        val availableWidth = w - pad * 2
        val availableHeight = h * 0.48f

        val declLayout = autoStepDownLayout(
            text = declText,
            typeface = serif,
            color = textColor,
            maxWidth = availableWidth,
            maxHeight = availableHeight,
            startSp = 19f,
            minSp = 13f,
            density = density
        )

        canvas.save()
        val declY = pad + markSize + 10f * density
        canvas.translate(pad, declY)
        declLayout.draw(canvas)
        canvas.restore()

        // Anchor rule
        val ruleY = declY + declLayout.height + 8f * density
        val rulePaint = Paint().apply {
            color = light.anchorRule.toArgb()
            strokeWidth = 2f * density
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(pad, ruleY, pad + 24f * density, ruleY, rulePaint)

        // Reference
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sans
            color = secColor
            textSize = 11f * density
            isFakeBoldText = true
        }
        canvas.drawText(aff.reference, pad, (ruleY + 14f * density).coerceAtMost(h - pad * 0.6f), refPaint)
    }

    /** Layout 3: 4x2 Pairing (Declaration + anchor rule + verse + reference) */
    private fun drawPairing4x2(
        canvas: Canvas,
        w: Float,
        h: Float,
        aff: Affirmation,
        light: Light,
        density: Float
    ) {
        val padX = w * 0.08f
        val padY = h * 0.12f
        val textColor = light.text.toArgb()
        val secColor = light.text.copy(alpha = light.secondaryAlpha).toArgb()

        val serif = StyleTypefaces.getNewsreader() ?: Typeface.SERIF
        val italic = StyleTypefaces.getNewsreaderItalic() ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val sans = StyleTypefaces.getHankenGrotesk() ?: Typeface.SANS_SERIF

        val availableWidth = w - padX * 2

        // Declaration
        val declLayout = autoStepDownLayout(
            text = com.makarios.app.data.widgetTextFor(aff.declaration, aff.shortText),
            typeface = serif,
            color = textColor,
            maxWidth = availableWidth,
            maxHeight = h * 0.36f,
            startSp = 21f,
            minSp = 14f,
            density = density
        )

        // Verse
        val verseLayout = autoStepDownLayout(
            text = "“${aff.scriptureText}”",
            typeface = italic,
            color = textColor,
            maxWidth = availableWidth,
            maxHeight = h * 0.28f,
            startSp = 13f,
            minSp = 10f,
            density = density
        )

        canvas.save()
        var curY = padY
        canvas.translate(padX, curY)
        declLayout.draw(canvas)
        canvas.restore()

        curY += declLayout.height + 8f * density

        // Anchor rule
        val rulePaint = Paint().apply {
            color = light.anchorRule.toArgb()
            strokeWidth = 2f * density
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(padX, curY, padX + 28f * density, curY, rulePaint)
        curY += 8f * density

        // Verse
        canvas.save()
        canvas.translate(padX, curY)
        verseLayout.draw(canvas)
        canvas.restore()

        curY += verseLayout.height + 6f * density

        // Reference
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sans
            color = secColor
            textSize = 11.5f * density
            isFakeBoldText = true
        }
        canvas.drawText(aff.reference, padX, curY + 10f * density, refPaint)

        // Doorway mark at bottom right
        val markSize = 15f * density
        drawSacredMark(canvas, w - padX - markSize, h - padY - markSize, markSize, light)
    }

    /** Layout 4: 4x4 Scene (Light label + sun disc + declaration + rule + verse + reference + wordmark) */
    private fun drawScene4x4(
        canvas: Canvas,
        w: Float,
        h: Float,
        aff: Affirmation,
        light: Light,
        density: Float
    ) {
        val padX = w * 0.09f
        val padY = h * 0.10f
        val textColor = light.text.toArgb()
        val secColor = light.text.copy(alpha = light.secondaryAlpha).toArgb()

        val serif = StyleTypefaces.getNewsreader() ?: Typeface.SERIF
        val italic = StyleTypefaces.getNewsreaderItalic() ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val sans = StyleTypefaces.getHankenGrotesk() ?: Typeface.SANS_SERIF

        val availableWidth = w - padX * 2

        // Top Light label
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sans
            color = secColor
            textSize = 11f * density
            isFakeBoldText = true
            letterSpacing = 0.12f
        }
        canvas.drawText(light.name.uppercase(), padX, padY + 12f * density, labelPaint)

        var curY = padY + 28f * density

        // Big declaration
        val declLayout = autoStepDownLayout(
            text = aff.declaration,
            typeface = serif,
            color = textColor,
            maxWidth = availableWidth,
            maxHeight = h * 0.32f,
            startSp = 24f,
            minSp = 16f,
            density = density
        )

        canvas.save()
        canvas.translate(padX, curY)
        declLayout.draw(canvas)
        canvas.restore()

        curY += declLayout.height + 12f * density

        // Anchor rule
        val rulePaint = Paint().apply {
            color = light.anchorRule.toArgb()
            strokeWidth = 2.5f * density
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(padX, curY, padX + 32f * density, curY, rulePaint)
        curY += 12f * density

        // Verse in Newsreader italic
        val verseLayout = autoStepDownLayout(
            text = "“${aff.scriptureText}”",
            typeface = italic,
            color = textColor,
            maxWidth = availableWidth,
            maxHeight = h * 0.24f,
            startSp = 14.5f,
            minSp = 11f,
            density = density
        )

        canvas.save()
        canvas.translate(padX, curY)
        verseLayout.draw(canvas)
        canvas.restore()

        curY += verseLayout.height + 8f * density

        // Reference
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sans
            color = secColor
            textSize = 12f * density
            isFakeBoldText = true
        }
        canvas.drawText(aff.reference, padX, curY + 12f * density, refPaint)

        // Bottom wordmark + sacred doorway arch
        val bottomY = h - padY * 0.8f
        val markSize = 16f * density
        drawSacredMark(canvas, padX, bottomY - markSize, markSize, light)

        val wordmarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sans
            color = textColor
            textSize = 12f * density
            alpha = (255 * 0.85f).toInt()
        }
        canvas.drawText("makarios", padX + markSize + 8f * density, bottomY - 3f * density, wordmarkPaint)
    }

    /**
     * Stepping font size down in 2% steps to fit within allocated bounds
     * without mid-thought truncation.
     */
    private fun autoStepDownLayout(
        text: String,
        typeface: Typeface,
        color: Int,
        maxWidth: Float,
        maxHeight: Float,
        startSp: Float,
        minSp: Float,
        density: Float,
        lineSpacingMultiplier: Float = 1.18f
    ): StaticLayout {
        var sizePx = startSp * density
        val minSizePx = minSp * density
        val widthInt = max(maxWidth.toInt(), 50)

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.color = color
        }

        while (sizePx >= minSizePx) {
            paint.textSize = sizePx
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, widthInt)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, lineSpacingMultiplier)
                .setIncludePad(false)
                .build()

            if (layout.height <= maxHeight) {
                return layout
            }
            sizePx *= 0.98f // Step down 2%
        }

        paint.textSize = minSizePx
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, widthInt)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, lineSpacingMultiplier)
            .setIncludePad(false)
            .build()
    }

    /** Draws sacred doorway arch mark with half-sun disc */
    private fun drawSacredMark(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        light: Light
    ) {
        val stroke = size * 0.12f
        val archPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = light.text.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (light.isDark) Color.parseColor("#E8A860") else Color.parseColor("#B5532B")
            style = Paint.Style.FILL
        }

        // Half sun inside arch
        val sunRect = RectF(
            x + size * 0.25f,
            y + size * 0.15f,
            x + size * 0.75f,
            y + size * 0.65f
        )
        canvas.drawArc(sunRect, 180f, 180f, true, sunPaint)

        // Doorway arch outline
        val path = Path().apply {
            moveTo(x + stroke * 0.5f, y + size)
            lineTo(x + stroke * 0.5f, y + size * 0.45f)
            arcTo(
                RectF(x + stroke * 0.5f, y + stroke * 0.5f, x + size - stroke * 0.5f, y + size * 0.9f),
                180f, 180f, false
            )
            lineTo(x + size - stroke * 0.5f, y + size)
        }
        canvas.drawPath(path, archPaint)
    }
}
