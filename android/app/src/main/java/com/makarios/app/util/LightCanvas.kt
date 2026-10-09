package com.makarios.app.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.makarios.app.R
import com.makarios.app.ui.theme.Light
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

enum class ExportFormat(val width: Int, val height: Int) {
    Square(1080, 1080),
    Portrait(1080, 1350),
    Story(1080, 1920), // 9:16
    X(1600, 900),
    Wallpaper(-1, -1) // Native resolution resolved at runtime
}

class LightCanvas(private val context: Context) {

    fun render(
        declaration: String,
        verseText: String,
        verseReference: String,
        light: Light,
        format: ExportFormat,
        deviceWidth: Int = 1080,
        deviceHeight: Int = 1920,
        userPhotoUri: Uri? = null,
        outputFile: File
    ): File {
        val w = if (format == ExportFormat.Wallpaper) deviceWidth else format.width
        val h = if (format == ExportFormat.Wallpaper) deviceHeight else format.height

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Photo (Optional) or Base Gradient
        if (userPhotoUri != null) {
            drawUserPhoto(canvas, w, h, userPhotoUri)
        } else {
            drawLightGradient(canvas, w, h, light)
        }

        // 2. Draw Text and Anchor
        drawPairing(canvas, w, h, declaration, verseText, verseReference, light, format)

        // 3. Save
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        // If > 8MB, compress as JPEG
        if (outputFile.length() > 8 * 1024 * 1024) {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
        }
        
        bitmap.recycle()
        return outputFile
    }

    private fun drawLightGradient(canvas: Canvas, w: Int, h: Int, light: Light) {
        val paint = Paint()
        paint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            light.top.value.toInt() or 0xFF000000.toInt(),
            light.bottom.value.toInt() or 0xFF000000.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Grain
        val grainBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.grain_tile)
        val grainPaint = Paint().apply {
            shader = BitmapShader(grainBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), grainPaint)
        grainBitmap.recycle()
    }

    private fun drawUserPhoto(canvas: Canvas, w: Int, h: Int, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.setTargetSampleSize(calculateInSampleSize(info.size.width, info.size.height, w, h))
                decoder.isMutableRequired = true
            }
            // Scale center crop
            val scale = max(w.toFloat() / decoded.width, h.toFloat() / decoded.height)
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            matrix.postTranslate((w - decoded.width * scale) / 2f, (h - decoded.height * scale) / 2f)
            canvas.drawBitmap(decoded, matrix, null)
            decoded.recycle()
        } else {
            // Fallback for pre-P could use BitmapFactory
            val inputStream = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (decoded != null) {
                val scale = max(w.toFloat() / decoded.width, h.toFloat() / decoded.height)
                val matrix = Matrix()
                matrix.postScale(scale, scale)
                matrix.postTranslate((w - decoded.width * scale) / 2f, (h - decoded.height * scale) / 2f)
                canvas.drawBitmap(decoded, matrix, null)
                decoded.recycle()
            }
        }
        
        // 35% dark overlay
        val overlayPaint = Paint().apply { color = Color.argb((255 * 0.35f).toInt(), 0, 0, 0) }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), overlayPaint)
    }
    
    private fun calculateInSampleSize(srcWidth: Int, srcHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun drawPairing(
        canvas: Canvas, w: Int, h: Int,
        declaration: String, verseText: String, verseReference: String,
        light: Light, format: ExportFormat
    ) {
        // Safe margins 8% on sides, 14% top/bottom
        val marginX = (w * 0.08f).toInt()
        val marginY = (h * 0.14f).toInt()
        val usableWidth = w - marginX * 2

        // Fonts
        val newsreader = ResourcesCompat.getFont(context, R.font.newsreader)
        val newsreaderItalic = ResourcesCompat.getFont(context, R.font.newsreader_italic)
        val hanken = ResourcesCompat.getFont(context, R.font.hanken_grotesk)

        // Type sizes relative to canvas width
        // Reference: Today declaration 34sp on ~412dp screen (~8.25% of width). 
        // We'll scale proportionally, but have a floor of 5% of width.
        var declTextSize = w * 0.0825f
        if (declTextSize < w * 0.05f) declTextSize = w * 0.05f

        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(newsreader, Typeface.NORMAL)
            textSize = declTextSize
            color = light.text.value.toInt() or 0xFF000000.toInt()
            fontFeatureSettings = "lnum, tnum"
        }

        val versePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(newsreaderItalic, Typeface.ITALIC)
            textSize = w * 0.04f // approx 17sp relative
            color = light.text.value.toInt() or 0xFF000000.toInt()
            fontFeatureSettings = "lnum, tnum"
        }

        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(hanken, Typeface.NORMAL) // Should be 500 weight, assume variable handles or use default
            textSize = w * 0.031f // approx 13sp relative
            color = (light.text.copy(alpha = light.secondaryAlpha)).value.toInt() or 0xFF000000.toInt()
            fontFeatureSettings = "lnum, tnum"
        }

        // Layouts
        val declLayout = StaticLayout.Builder.obtain(declaration, 0, declaration.length, declPaint, usableWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            .build()
            
        val verseLayout = StaticLayout.Builder.obtain(verseText, 0, verseText.length, versePaint, usableWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.3f)
            .setIncludePad(false)
            .build()
            
        val refLayout = StaticLayout.Builder.obtain(verseReference, 0, verseReference.length, refPaint, usableWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()

        val anchorHeight = w * 0.005f
        val anchorWidth = w * 0.07f
        val gap1 = w * 0.06f // between decl and anchor
        val gap2 = w * 0.04f // between anchor and verse
        val gap3 = w * 0.02f // between verse and ref

        val totalHeight = declLayout.height + gap1 + anchorHeight + gap2 + verseLayout.height + gap3 + refLayout.height

        // Y Positioning
        var startY = (h - totalHeight) * 0.4f // optically centered around 40% height
        
        if (format == ExportFormat.Wallpaper) {
            // keep top 32% clear, put pairing between 45% and 75%
            startY = h * 0.45f
        } else {
            // Clamp to safe margins
            if (startY < marginY) startY = marginY.toFloat()
        }

        // Draw
        canvas.save()
        canvas.translate(marginX.toFloat(), startY)
        
        declLayout.draw(canvas)
        canvas.translate(0f, declLayout.height + gap1)
        
        // Draw Anchor
        val anchorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = light.anchorRule.value.toInt() or 0xFF000000.toInt()
        }
        canvas.drawRect(0f, 0f, anchorWidth, anchorHeight, anchorPaint)
        
        canvas.translate(0f, anchorHeight + gap2)
        verseLayout.draw(canvas)
        
        canvas.translate(0f, verseLayout.height + gap3)
        refLayout.draw(canvas)
        
        canvas.restore()
    }
}
