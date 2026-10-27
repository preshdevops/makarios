package com.makarios.app.ui.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.drawLight
import com.makarios.app.util.ExportFormat
import kotlin.math.*

// ---------------------------------------------------------------------------
// 1. ARCH STYLE (Frame) - Romanesque rounded arch line border
// ---------------------------------------------------------------------------
object ArchStyle : BaseStyle("arch", "Arch", category = "Frame", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        // Draw Romanesque Arch Frame
        val archPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.copy(alpha = 0.5f).toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2f * s
        }
        val p = AndroidPath().apply {
            val left = safe.left - 12f * s
            val right = safe.right + 12f * s
            val top = safe.top - 8f * s
            val bottom = safe.bottom + 8f * s
            val archH = (right - left) * 0.45f

            moveTo(left, bottom)
            lineTo(left, top + archH)
            arcTo(RectF(left, top, right, top + archH * 2), 180f, 180f)
            lineTo(right, bottom)
            close()
        }
        canvas.drawPath(p, archPaint)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 2. MONOSPACE STYLE (Simple) - Clean typewriter aesthetic with timestamp
// ---------------------------------------------------------------------------
object MonospaceStyle : BaseStyle("monospace", "Monospace", category = "Simple", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        // Draw subtle header tag
        val tagPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.text.copy(alpha = 0.55f).toArgb()
            typeface = Typeface.MONOSPACE
            textSize = 10f * s
            letterSpacing = 0.08f
        }
        canvas.drawText("// DECLARATION", safe.left, safe.top - 6f * s, tagPaint)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.MONOSPACE,
            verseFont = Typeface.MONOSPACE,
            refFont = Typeface.MONOSPACE,
            centerAlign = false,
            includeRule = true,
            declMaxSp = 30f,
            verseMaxSp = 15f
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 3. SANCTUARY STYLE (Scene) - Sacred geometry with subtle radiating vector beams
// ---------------------------------------------------------------------------
object SanctuaryStyle : BaseStyle("sanctuary", "Sanctuary", category = "Scene", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        val beamColor = light.anchorRule.copy(alpha = 0.12f)
        val cx = w * 0.5f
        val cy = h * 0.20f
        for (i in -4..4) {
            val angle = i * 14f * (PI.toFloat() / 180f)
            val endX = cx + sin(angle) * (h * 0.7f)
            val endY = cy + cos(angle) * (h * 0.7f)
            scope.drawLine(beamColor, Offset(cx, cy), Offset(endX, endY), strokeWidth = 1.5f * s)
        }
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 4. PARCHMENT STYLE (Journal) - Warm hand-pressed deckle edge border
// ---------------------------------------------------------------------------
object ParchmentStyle : BaseStyle("parchment", "Parchment", category = "Journal", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        // Deckle edge inner border
        val borderPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.copy(alpha = 0.40f).toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1.5f * s
            pathEffect = DashPathEffect(floatArrayOf(8f * s, 4f * s), 0f)
        }
        val pad = 10f * s
        canvas.drawRect(safe.left - pad, safe.top - pad, safe.right + pad, safe.bottom + pad, borderPaint)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 5. HEADLINE STYLE (Bold) - Large bold display serif pull-quote
// ---------------------------------------------------------------------------
object HeadlineStyle : BaseStyle("headline", "Headline", category = "Bold", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        // Quotation mark flourish
        val quotePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.copy(alpha = 0.25f).toArgb()
            typeface = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD)
            textSize = 64f * s
        }
        canvas.drawText("“", safe.left - 4f * s, safe.top + 20f * s, quotePaint)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = true,
            declMaxSp = 40f
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 6. NOTEBOOK STYLE (Journal) - Ruled notebook lines with red margin guide
// ---------------------------------------------------------------------------
object NotebookStyle : BaseStyle("notebook", "Notebook", category = "Journal", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        // Horizontal ruled lines
        val linePaint = light.text.copy(alpha = 0.08f)
        var y = h * 0.15f
        while (y < h * 0.88f) {
            scope.drawLine(linePaint, Offset(w * 0.05f, y), Offset(w * 0.95f, y), strokeWidth = 1f * s)
            y += 28f * s
        }

        // Vertical margin guide line
        val marginGuide = (if (light.isDark) Color(0xFFE57373) else Color(0xFFD32F2F)).copy(alpha = 0.25f)
        scope.drawLine(marginGuide, Offset(w * 0.16f, h * 0.10f), Offset(w * 0.16f, h * 0.90f), strokeWidth = 1.5f * s)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        // Shift text right of vertical margin guide
        val adjustedSafe = RectF(geom.width * 0.20f, safe.top, safe.right, safe.bottom)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = adjustedSafe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 7. CREST STYLE (Frame) - Classical heraldic diamond crest frame
// ---------------------------------------------------------------------------
object CrestStyle : BaseStyle("crest", "Crest", category = "Frame", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        // Corner diamond accents
        val accentPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.copy(alpha = 0.6f).toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1.5f * s
        }
        val dSize = 8f * s
        fun drawDiamond(cx: Float, cy: Float) {
            val path = AndroidPath().apply {
                moveTo(cx, cy - dSize)
                lineTo(cx + dSize, cy)
                lineTo(cx, cy + dSize)
                lineTo(cx - dSize, cy)
                close()
            }
            canvas.drawPath(path, accentPaint)
        }
        drawDiamond(safe.left, safe.top)
        drawDiamond(safe.right, safe.top)
        drawDiamond(safe.left, safe.bottom)
        drawDiamond(safe.right, safe.bottom)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 8. OASIS STYLE (Scene) - Warm desert sun disc over serene geometric horizon
// ---------------------------------------------------------------------------
object OasisStyle : BaseStyle("oasis", "Oasis", category = "Scene", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        // Soft sun disc
        val sunColor = (if (light.isDark) Color(0xFFFFA726) else Color(0xFFFFB74D)).copy(alpha = 0.35f)
        scope.drawCircle(sunColor, radius = 54f * s, center = Offset(w * 0.80f, h * 0.28f))

        // Clean geometric horizon wave
        val waveColor = light.anchorRule.copy(alpha = 0.18f)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.85f)
            quadraticTo(w * 0.35f, h * 0.82f, w * 0.70f, h * 0.86f)
            quadraticTo(w * 0.85f, h * 0.88f, w, h * 0.84f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        scope.drawPath(path, waveColor)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 9. SERENE STYLE (Simple) - Ultra-refined breathing room, centered quiet elegance
// ---------------------------------------------------------------------------
object SereneStyle : BaseStyle("serene", "Serene", category = "Simple", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.NORMAL),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true,
            declMaxSp = 32f
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 10. IMPACT STYLE (Bold) - Heavy modernist geometric typography, solid accent bar
// ---------------------------------------------------------------------------
object ImpactStyle : BaseStyle("impact", "Impact", category = "Bold", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.BOLD),
            centerAlign = false,
            includeRule = true,
            declMaxSp = 38f
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 11. DIARY STYLE (Journal) - Date stamp header with intimate editorial setting
// ---------------------------------------------------------------------------
object DiaryStyle : BaseStyle("diary", "Diary", category = "Journal", maxChars = 300) {
    override val supportedOptions: Set<StyleOptionField> = setOf(StyleOptionField.DATE)

    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        // Date stamp
        val dateText = (spec.timestampFormatted ?: java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.US).format(java.util.Date())).uppercase()
        val datePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.toArgb()
            typeface = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.BOLD)
            textSize = 11f * s
            letterSpacing = 0.12f
        }
        canvas.drawText(dateText, safe.left, safe.top - 6f * s, datePaint)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 12. DOUBLE LINE STYLE (Frame) - Dual concentric inset geometric frames
// ---------------------------------------------------------------------------
object DoubleLineStyle : BaseStyle("double_line", "Double Line", category = "Frame", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        val framePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.copy(alpha = 0.45f).toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 1.5f * s
        }
        val pad1 = 12f * s
        val pad2 = 16f * s
        canvas.drawRect(safe.left - pad1, safe.top - pad1, safe.right + pad1, safe.bottom + pad1, framePaint)
        canvas.drawRect(safe.left - pad2, safe.top - pad2, safe.right + pad2, safe.bottom + pad2, framePaint)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 13. STARS STYLE (Scene) - Soft field of 8-point vector stars
// ---------------------------------------------------------------------------
object StarsStyle : BaseStyle("stars", "Stars", category = "Scene", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
        val w = geom.width
        val h = geom.height
        val s = geom.scale
        val starColor = (if (light.isDark) Color(0xFFFFE082) else Color(0xFFD4AF37)).copy(alpha = 0.5f)

        fun drawStar(cx: Float, cy: Float, r: Float) {
            scope.drawLine(starColor, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1.2f * s)
            scope.drawLine(starColor, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 1.2f * s)
            val diag = r * 0.65f
            scope.drawLine(starColor, Offset(cx - diag, cy - diag), Offset(cx + diag, cy + diag), strokeWidth = 1f * s)
            scope.drawLine(starColor, Offset(cx - diag, cy + diag), Offset(cx + diag, cy - diag), strokeWidth = 1f * s)
        }

        drawStar(w * 0.18f, h * 0.22f, 10f * s)
        drawStar(w * 0.82f, h * 0.26f, 14f * s)
        drawStar(w * 0.75f, h * 0.78f, 12f * s)
        drawStar(w * 0.22f, h * 0.82f, 8f * s)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 14. PURE STYLE (Simple) - Pure typographic clarity with asymmetric whitespace
// ---------------------------------------------------------------------------
object PureStyle : BaseStyle("pure", "Pure", category = "Simple", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = false,
            declMaxSp = 36f
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 15. BLOCK STYLE (Bold) - Inset solid accent block with high-contrast text
// ---------------------------------------------------------------------------
object BlockStyle : BaseStyle("block", "Block", category = "Bold", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.BOLD),
            centerAlign = true,
            includeRule = true,
            declMaxSp = 36f
        )

        // Draw tinted background container under declaration
        val blockPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.copy(alpha = 0.12f).toArgb()
        }
        val pad = 12f * s
        canvas.drawRoundRect(
            RectF(stack.declBounds.left - pad, stack.declBounds.top - pad, stack.declBounds.right + pad, stack.declBounds.bottom + pad),
            12f * s, 12f * s, blockPaint
        )

        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 16. LEDGER STYLE (Journal) - Vintage accounts ledger grid lines
// ---------------------------------------------------------------------------
object LedgerStyle : BaseStyle("ledger", "Ledger", category = "Journal", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        val gridPaint = light.anchorRule.copy(alpha = 0.08f)
        // Two subtle vertical ledger column rules
        scope.drawLine(gridPaint, Offset(w * 0.12f, h * 0.12f), Offset(w * 0.12f, h * 0.88f), strokeWidth = 1f * s)
        scope.drawLine(gridPaint, Offset(w * 0.88f, h * 0.12f), Offset(w * 0.88f, h * 0.88f), strokeWidth = 1f * s)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 17. PILLAR STYLE (Frame) - Dual classical fluted vertical border lines
// ---------------------------------------------------------------------------
object PillarStyle : BaseStyle("pillar", "Pillar", category = "Frame", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        val pillarColor = light.anchorRule.copy(alpha = 0.40f)
        // Left pillar lines
        scope.drawLine(pillarColor, Offset(w * 0.05f, h * 0.12f), Offset(w * 0.05f, h * 0.88f), strokeWidth = 2f * s)
        scope.drawLine(pillarColor, Offset(w * 0.065f, h * 0.14f), Offset(w * 0.065f, h * 0.86f), strokeWidth = 1f * s)
        // Right pillar lines
        scope.drawLine(pillarColor, Offset(w * 0.95f, h * 0.12f), Offset(w * 0.95f, h * 0.88f), strokeWidth = 2f * s)
        scope.drawLine(pillarColor, Offset(w * 0.935f, h * 0.14f), Offset(w * 0.935f, h * 0.86f), strokeWidth = 1f * s)
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 18. DAWN BREAK STYLE (Scene) - Multi-layered vector sunrise contours
// ---------------------------------------------------------------------------
object DawnBreakStyle : BaseStyle("dawn_break", "Dawn Break", category = "Scene", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
        val w = geom.width
        val h = geom.height
        val s = geom.scale

        val sunColor = (if (light.isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)).copy(alpha = 0.28f)
        scope.drawCircle(sunColor, radius = 90f * s, center = Offset(w * 0.5f, h * 0.88f))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getNewsreader(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = true,
            includeRule = true
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 19. AIR STYLE (Simple) - Expansive letter-spacing, airy modern grotesque
// ---------------------------------------------------------------------------
object AirStyle : BaseStyle("air", "Air", category = "Simple", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)
        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.NORMAL),
            centerAlign = false,
            includeRule = true,
            declMaxSp = 30f
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}

// ---------------------------------------------------------------------------
// 20. PUNCH STYLE (Bold) - Striking heavy emphasis with framing brackets
// ---------------------------------------------------------------------------
object PunchStyle : BaseStyle("punch", "Punch", category = "Bold", maxChars = 300) {
    override fun drawArt(scope: DrawScope, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        scope.drawLight(light, Size(geom.width, geom.height))
    }

    override fun drawTypography(canvas: AndroidCanvas, bitmap: Bitmap, geom: LayoutGeometry, light: Light, spec: StyleSpec) {
        val s = geom.scale
        val safe = StyleLayoutEngine.computeSafeBounds(geom.width, geom.height, geom.format)

        // Corner framing brackets
        val bracketPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.toArgb()
            style = AndroidPaint.Style.STROKE
            strokeWidth = 3f * s
        }
        val bLen = 20f * s
        // Top Left
        canvas.drawLine(safe.left, safe.top + bLen, safe.left, safe.top, bracketPaint)
        canvas.drawLine(safe.left, safe.top, safe.left + bLen, safe.top, bracketPaint)
        // Bottom Right
        canvas.drawLine(safe.right, safe.bottom - bLen, safe.right, safe.bottom, bracketPaint)
        canvas.drawLine(safe.right - bLen, safe.bottom, safe.right, safe.bottom, bracketPaint)

        val stack = StyleLayoutEngine.layoutVerticalStack(
            width = geom.width,
            height = geom.height,
            safeBounds = safe,
            spec = spec,
            light = light,
            canvasScale = s,
            declFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.BOLD),
            verseFont = Typeface.create(StyleTypefaces.getNewsreaderItalic(), Typeface.ITALIC),
            refFont = Typeface.create(StyleTypefaces.getHankenGrotesk(), Typeface.BOLD),
            centerAlign = false,
            includeRule = true,
            declMaxSp = 36f
        )
        StyleLayoutEngine.drawVerticalStack(canvas, stack, light)
    }
}
