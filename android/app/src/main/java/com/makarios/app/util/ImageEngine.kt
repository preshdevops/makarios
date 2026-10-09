package com.makarios.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.makarios.app.R
import com.makarios.app.data.Affirmation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

/**
 * ImageOutputFormat
 *
 * One unified image engine with three standard output sizes:
 * 1. STORY & WALLPAPER: 1080 x 1920 (9:16 aspect ratio)
 * 2. SQUARE:            1080 x 1080 (1:1 aspect ratio)
 * 3. PORTRAIT_POST:     1080 x 1350 (4:5 aspect ratio)
 *
 * Each format defines its exact pixel dimension (width >= 1080), fixed DP layout size
 * under LocalDensity 3.0, and safe zone ratios.
 */
enum class ImageOutputFormat(
    val id: String,
    val displayName: String,
    val width: Int,
    val height: Int,
    val targetDpWidth: Dp,
    val targetDpHeight: Dp,
    val density: Float = 3.0f,
    val topSafeRatio: Float = 0.14f,
    val bottomSafeRatio: Float = 0.14f,
    val horizontalSafeRatio: Float = 0.08f
) {
    // 1. Story & Wallpaper: 1080 x 1920 (9:16)
    // 360dp x 640dp @ density 3.0 = 1080 x 1920 px
    // Top & bottom 14% clearance (268.8px) for Story UI overlays
    STORY(
        id = "story",
        displayName = "Story & Wallpaper",
        width = 1080,
        height = 1920,
        targetDpWidth = 360.dp,
        targetDpHeight = 640.dp,
        density = 3.0f,
        topSafeRatio = 0.14f,
        bottomSafeRatio = 0.14f,
        horizontalSafeRatio = 0.08f
    ),

    // 2. Square: 1080 x 1080 (1:1)
    // 360dp x 360dp @ density 3.0 = 1080 x 1080 px
    SQUARE(
        id = "square",
        displayName = "Square Post",
        width = 1080,
        height = 1080,
        targetDpWidth = 360.dp,
        targetDpHeight = 360.dp,
        density = 3.0f,
        topSafeRatio = 0.08f,
        bottomSafeRatio = 0.08f,
        horizontalSafeRatio = 0.08f
    ),

    // 3. Portrait Post: 1080 x 1350 (4:5)
    // 360dp x 450dp @ density 3.0 = 1080 x 1350 px
    PORTRAIT_POST(
        id = "portrait_post",
        displayName = "Portrait Post",
        width = 1080,
        height = 1350,
        targetDpWidth = 360.dp,
        targetDpHeight = 450.dp,
        density = 3.0f,
        topSafeRatio = 0.08f,
        bottomSafeRatio = 0.08f,
        horizontalSafeRatio = 0.08f
    );

    companion object {
        val WALLPAPER = STORY

        fun fromSocialPlatform(platform: ShareHelper.SocialPlatform): ImageOutputFormat = when (platform) {
            ShareHelper.SocialPlatform.INSTAGRAM_STORY,
            ShareHelper.SocialPlatform.SNAPCHAT -> STORY
            ShareHelper.SocialPlatform.INSTAGRAM_POST -> SQUARE
            ShareHelper.SocialPlatform.X_TWITTER,
            ShareHelper.SocialPlatform.WHATSAPP -> PORTRAIT_POST
        }
    }
}

/**
 * ImageEngine
 *
 * Impeccable, on-device graphic export engine for Makarios.
 *
 * Specifications:
 * - One image engine, three outputs: Story/Wallpaper (1080x1920), Square (1080x1080), Portrait post (1080x1350).
 * - Render offscreen from dedicated fixed-size composables/views, never by capturing the on-screen card.
 * - Background photo: decode at target size or larger; never use thumbnails or downsampled grid images.
 * - Text legibility: bottom-weighted black scrim (transparent to ~65%), soft text shadows, 8% safe margins,
 *   keep text out of top and bottom 14% for Story UI overlays.
 * - Export: PNG for text-on-photo; JPEG fallback >= 92 (never below 85).
 * - Sharing: cache file via FileProvider content URI; never share thumbnail or CDN URL.
 * - Debug: logs exported pixel dimensions; tested with width >= 1080 assertion.
 *
 * iOS Counterpart Architecture:
 * - SwiftUI ImageRenderer with proposedSize set to target (1080x1920, 1080x1080, 1080x1350) and scale = 1.0.
 * - Exports uiImage?.pngData(), never an on-screen Image view.
 */
object ImageEngine {

    const val TAG = "ImageEngine"
    const val DEFAULT_JPEG_QUALITY = 92
    const val MIN_JPEG_QUALITY = 85

    fun clampJpegQuality(quality: Int): Int = quality.coerceIn(MIN_JPEG_QUALITY, 100)

    /**
     * Upgrades any photographic background URL to its full resolution for export.
     * Replaces any thumbnail parameters (w=320) with target size w=1920 at quality 90.
     */
    fun upgradePhotoUrlForExport(url: String): String {
        if (url.isBlank()) return url
        if (url.contains("images.unsplash.com")) {
            var upgraded = url
            upgraded = upgraded.replace(Regex("[?&]w=\\d+"), "")
            upgraded = upgraded.replace(Regex("[?&]q=\\d+"), "")
            val sep = if (upgraded.contains("?")) "&" else "?"
            return "$upgraded${sep}w=1920&q=90"
        }
        return url
    }

    /**
     * Decodes background photo at the target output size or larger (>= 1080px)
     * with hardware bitmaps disabled so it can be recorded or drawn freely.
     */
    suspend fun decodePhotoAtTargetSize(
        context: Context,
        url: String,
        targetWidth: Int = 1080,
        targetHeight: Int = 1920
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val highResUrl = upgradePhotoUrlForExport(url)
            val reqWidth = maxOf(targetWidth, 1080)
            val reqHeight = maxOf(targetHeight, 1080)
            val request = coil.request.ImageRequest.Builder(context)
                .data(highResUrl)
                .size(reqWidth, reqHeight)
                .scale(coil.size.Scale.FILL)
                .allowHardware(false)
                .build()
            val result = coil.Coil.imageLoader(context).execute(request)
            if (result is coil.request.SuccessResult) {
                val bmp = result.drawable.toBitmap()
                Log.d(TAG, "Decoded high-res photo: ${bmp.width}x${bmp.height} (requested ${reqWidth}x${reqHeight})")
                bmp
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed decoding background photo", e)
            null
        }
    }

    /**
     * Writes exported bitmap to cache as PNG (or JPEG >= 92).
     * Logs exported pixel dimensions and returns the cached File.
     */
    fun exportToCache(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = DEFAULT_JPEG_QUALITY,
        prefix: String = "makarios_export"
    ): File {
        val extension = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"
        val effectiveQuality = if (format == Bitmap.CompressFormat.JPEG) clampJpegQuality(quality) else 100

        val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(imagesFolder, "${prefix}_${System.currentTimeMillis()}.$extension")

        FileOutputStream(file).use { out ->
            if (!bitmap.compress(format, effectiveQuality, out)) {
                throw IOException("Failed to compress bitmap into cache file")
            }
        }

        Log.d(TAG, "Exported image dimensions: ${bitmap.width}x${bitmap.height} (${file.name}, ${file.length()} bytes)")
        return file
    }

    /**
     * Gets a content URI for sharing via FileProvider.
     */
    fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * High-fidelity offscreen Canvas renderer.
     * Renders directly to a fixed pixel size Bitmap (1080x1920, 1080x1080, 1080x1350)
     * enforcing bottom-weighted black scrim (0 to 65%), 8% horizontal safe margins,
     * and top/bottom 14% exclusion zones on Story.
     */
    fun renderCanvasBitmap(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String,
        style: WallpaperRenderer.RenderStyle = WallpaperRenderer.STYLES[0],
        format: ImageOutputFormat = ImageOutputFormat.STORY,
        photoBitmap: Bitmap? = null
    ): Bitmap {
        val width = format.width
        val height = format.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isPhoto = photoBitmap != null

        // 1. Photo Background with center-crop
        if (photoBitmap != null) {
            val matrix = Matrix()
            val scale = max(width.toFloat() / photoBitmap.width, height.toFloat() / photoBitmap.height)
            val dx = (width - photoBitmap.width * scale) * 0.5f
            val dy = (height - photoBitmap.height * scale) * 0.5f
            matrix.setScale(scale, scale)
            matrix.postTranslate(dx, dy)
            val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
            canvas.drawBitmap(photoBitmap, matrix, photoPaint)

            // Bottom-weighted black scrim: transparent at top to about 65% black at bottom
            val scrimShader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    0x00000000,          // Transparent at top
                    0x26000000,          // ~15% black at 35%
                    0x73000000,          // ~45% black at 65%
                    0xA6000000.toInt()   // ~65% black at 100%
                ),
                floatArrayOf(0f, 0.35f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
            val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = scrimShader }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        } else {
            // Gradient ground
            val bgShader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                style.backgroundColors,
                null,
                Shader.TileMode.CLAMP
            )
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isDither = true
                shader = bgShader
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }

        // Palette & Soft Shadow (45% black, 6px blur, 2px offset)
        val primaryTextColor = if (isPhoto) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val secondaryTextColor = if (isPhoto) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val accentColor = if (isPhoto) 0xFFFBBF24.toInt() else style.accentColor

        // Fonts
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular) ?: Typeface.SANS_SERIF

        // Margins & Exclusion zones
        val horizontalMargin = width * format.horizontalSafeRatio // 86.4px (8%)
        val contentMaxWidth = (width - 2 * horizontalMargin).toInt()
        val topSafeY = height * format.topSafeRatio              // 268.8px for Story (14%)
        val bottomSafeY = height * (1f - format.bottomSafeRatio) // 1651.2px for Story

        // Top category badge
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 28f
            letterSpacing = 0.12f
            color = accentColor
            textAlign = Paint.Align.CENTER
            if (isPhoto) setShadowLayer(6f, 0f, 2f, 0x73000000.toInt())
        }
        val headerY = topSafeY + 36f
        canvas.drawText(category.uppercase(), width / 2f, headerY, headerPaint)

        // Center Declaration
        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = if (format == ImageOutputFormat.SQUARE) 60f else 68f
            color = primaryTextColor
            if (isPhoto) setShadowLayer(6f, 0f, 2f, 0x73000000.toInt())
        }
        val declLayout = createCenteredStaticLayout("“$cleanDeclaration”", declPaint, contentMaxWidth)

        // Scripture Plaque
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = 38f
            color = secondaryTextColor
            if (isPhoto) setShadowLayer(6f, 0f, 2f, 0x73000000.toInt())
        }
        val scriptLayout = if (cleanScripture.isNotBlank()) {
            createCenteredStaticLayout("“$cleanScripture”", scriptPaint, (contentMaxWidth * 0.90f).toInt())
        } else null

        // Reference
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 28f
            color = accentColor
            textAlign = Paint.Align.CENTER
            if (isPhoto) setShadowLayer(6f, 0f, 2f, 0x73000000.toInt())
        }

        // Layout heights calculation
        val declHeight = declLayout.height.toFloat()
        val scriptHeight = scriptLayout?.height?.toFloat() ?: 0f
        val dividerHeight = 32f
        val refHeight = 34f
        val spacing = 28f
        val totalBlockHeight = declHeight + dividerHeight + scriptHeight + refHeight + (3 * spacing)

        // Center vertically inside safe boundary
        val availableSafeHeight = bottomSafeY - (headerY + 40f) - 60f
        val startY = (headerY + 40f) + ((availableSafeHeight - totalBlockHeight) * 0.5f).coerceAtLeast(20f)

        // Draw declaration
        canvas.save()
        canvas.translate((width - contentMaxWidth) / 2f, startY)
        declLayout.draw(canvas)
        canvas.restore()

        var currentY = startY + declHeight + spacing

        // Sacred Divider (hairline with diamond)
        drawSacredDivider(canvas, width / 2f, currentY + dividerHeight / 2f, 160f, 2f, accentColor)
        currentY += dividerHeight + spacing

        // Draw scripture
        if (scriptLayout != null) {
            val scriptWidth = (contentMaxWidth * 0.90f).toInt()
            canvas.save()
            canvas.translate((width - scriptWidth) / 2f, currentY)
            scriptLayout.draw(canvas)
            canvas.restore()
            currentY += scriptHeight + spacing
        }

        // Draw reference
        val cleanRef = reference.trim().removePrefix("—").trim()
        canvas.drawText("— $cleanRef", width / 2f, currentY + 24f, refPaint)

        // Subtle signature above bottom safe margin
        val wordmarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = 30f
            color = primaryTextColor
            alpha = if (isPhoto) 150 else 90
            textAlign = Paint.Align.CENTER
            if (isPhoto) setShadowLayer(6f, 0f, 2f, 0x73000000.toInt())
        }
        canvas.drawText("makarios", width / 2f, bottomSafeY - 10f, wordmarkPaint)

        Log.d(TAG, "Exported image dimensions: ${bitmap.width}x${bitmap.height} (${format.displayName})")
        return bitmap
    }

    private fun createCenteredStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        maxWidth: Int
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.30f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1.30f, 0f, false)
        }
    }

    private fun drawSacredDivider(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        totalWidth: Float,
        lineThickness: Float,
        color: Int
    ) {
        val half = totalWidth / 2f
        val diamondRadius = 6f
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = lineThickness
            alpha = 130
        }
        canvas.drawLine(centerX - half, centerY, centerX - diamondRadius * 1.5f, centerY, linePaint)
        canvas.drawLine(centerX + diamondRadius * 1.5f, centerY, centerX + half, centerY, linePaint)

        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
            alpha = 170
        }
        val diamondPath = android.graphics.Path().apply {
            moveTo(centerX, centerY - diamondRadius)
            lineTo(centerX + diamondRadius, centerY)
            lineTo(centerX, centerY + diamondRadius)
            lineTo(centerX - diamondRadius, centerY)
            close()
        }
        canvas.drawPath(diamondPath, diamondPaint)
    }
}
