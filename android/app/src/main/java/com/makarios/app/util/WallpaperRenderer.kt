package com.makarios.app.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.makarios.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

/**
 * WallpaperRenderer
 *
 * Impeccable, on-device graphic rendering engine for Makarios.
 * Produces sacred, high-resolution typography artifacts and wallpapers
 * with Cormorant Garamond display serif, grounding scripture, and radiant gradients.
 */
object WallpaperRenderer {

    enum class WallpaperTarget {
        HOME_SCREEN,
        LOCK_SCREEN,
        BOTH
    }

    enum class OutputFormat(
        val displayName: String,
        val width: Int,
        val height: Int,
        val isWallpaper: Boolean
    ) {
        STORY("Story (9:16)", 1080, 1920, false),
        SQUARE("Square (1:1)", 1080, 1080, false),
        STATUS("Status (4:5)", 1080, 1350, false),
        X_CARD("Landscape (16:9)", 1200, 675, false),
        WALLPAPER("Phone Wallpaper", 1080, 2400, true);

        companion object {
            fun fromIndex(index: Int): OutputFormat {
                return when (index) {
                    0 -> STORY
                    1 -> SQUARE
                    2 -> STATUS
                    3 -> X_CARD
                    4 -> WALLPAPER
                    else -> STORY
                }
            }
        }
    }

    data class RenderStyle(
        val name: String,
        val backgroundColors: IntArray,
        val primaryTextColor: Int,
        val secondaryTextColor: Int,
        val accentColor: Int,
        val frameColor: Int,
        val isDark: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as RenderStyle
            return name == other.name && backgroundColors.contentEquals(other.backgroundColors)
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + backgroundColors.contentHashCode()
            return result
        }
    }

    val STYLES = listOf(
        // 0: Alabaster Dawn — Alabaster linen ground with warm terracotta
        RenderStyle(
            name = "Alabaster",
            backgroundColors = intArrayOf(
                0xFFFAF7F2.toInt(),
                0xFFF3EDE4.toInt()
            ),
            primaryTextColor = 0xFF2C2622.toInt(),      // Deep warm Espresso
            secondaryTextColor = 0xFF655C54.toInt(),    // Warm Stone
            accentColor = 0xFFA85842.toInt(),           // Terracotta
            frameColor = 0x24A85842.toInt(),            // Terracotta hairline
            isDark = false
        ),
        // 1: Sunlit Gold — Morning dawn sunlight
        RenderStyle(
            name = "Sunlit Gold",
            backgroundColors = intArrayOf(
                0xFFFDF7EA.toInt(),
                0xFFF6E8CA.toInt()
            ),
            primaryTextColor = 0xFF2C2622.toInt(),
            secondaryTextColor = 0xFF615132.toInt(),
            accentColor = 0xFFD4A038.toInt(),           // SunlitGold
            frameColor = 0x33D4A038.toInt(),
            isDark = false
        ),
        // 2: Morning Sage — Quiet morning eucalyptus
        RenderStyle(
            name = "Morning Sage",
            backgroundColors = intArrayOf(
                0xFFF2F6F3.toInt(),
                0xFFE2EBE5.toInt()
            ),
            primaryTextColor = 0xFF243329.toInt(),
            secondaryTextColor = 0xFF4D6153.toInt(),
            accentColor = 0xFF607768.toInt(),           // Sage
            frameColor = 0x2A607768.toInt(),
            isDark = false
        ),
        // 3: Rose Dawn — Terracotta sunrise
        RenderStyle(
            name = "Rose Dawn",
            backgroundColors = intArrayOf(
                0xFFE88A6E.toInt(),
                0xFFD47355.toInt(),
                0xFFB85A3E.toInt()
            ),
            primaryTextColor = 0xFFFFFFFF.toInt(),
            secondaryTextColor = 0xFFFDF0EC.toInt(),
            accentColor = 0xFFFFF0EC.toInt(),
            frameColor = 0x36FFFFFF.toInt(),
            isDark = true
        ),
        // 4: Twilight Sanctuary — Sacred candlelit sanctuary
        RenderStyle(
            name = "Twilight",
            backgroundColors = intArrayOf(
                0xFF382F2A.toInt(),
                0xFF28211D.toInt(),
                0xFF1B1613.toInt()
            ),
            primaryTextColor = 0xFFFFFFFF.toInt(),
            secondaryTextColor = 0xEDECE7E1.toInt(),
            accentColor = 0xFFDEAC46.toInt(),           // Sacred Amber Gold
            frameColor = 0x2EEDE7E1.toInt(),
            isDark = true
        )
    )

    fun getStyle(index: Int): RenderStyle {
        return STYLES.getOrElse(index) { STYLES[0] }
    }

    /**
     * Render an affirmation into an exquisite, production-grade Bitmap.
     */
    /**
     * Render an affirmation into an exquisite, production-grade Bitmap.
     * Supports both YouVersion-style photographic backgrounds with cinematic protective scrims
     * and sacred radiant color gradient themes.
     */
    fun renderBitmap(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String = "DECLARATION",
        style: RenderStyle = STYLES[0],
        format: OutputFormat = OutputFormat.WALLPAPER,
        photoBitmap: Bitmap? = null
    ): Bitmap {
        val width = format.width
        val height = format.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isPhotoActive = photoBitmap != null

        if (photoBitmap != null) {
            // 1. Draw Photographic Background with CenterCrop Scaling
            val matrix = Matrix()
            val scale = max(width.toFloat() / photoBitmap.width, height.toFloat() / photoBitmap.height)
            val dx = (width - photoBitmap.width * scale) * 0.5f
            val dy = (height - photoBitmap.height * scale) * 0.5f
            matrix.setScale(scale, scale)
            matrix.postTranslate(dx, dy)
            val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
            canvas.drawBitmap(photoBitmap, matrix, photoPaint)

            // 2. Cinematic Protective Scrim for Pristine Readability (YouVersion style)
            val scrimShader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    0x4D000000.toInt(), // 30% dark at top
                    0x660E0B08.toInt(), // 40% dark upper mid
                    0x990E0B08.toInt(), // 60% dark lower mid
                    0xE60A0806.toInt()  // 90% dark espresso base for scripture & reference
                ),
                floatArrayOf(0f, 0.30f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
            val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = scrimShader }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        } else {
            // 1. Background Gradient
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

            // 2. Soft Ambient Radial Glow
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isDither = true
                val glowColor = if (style.isDark) 0x18FFFFFF else 0x12FFFFFF
                shader = RadialGradient(
                    width / 2f,
                    height * 0.48f,
                    width * 0.65f,
                    intArrayOf(glowColor, 0x00000000),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
        }

        // Active Theme Colors
        val activePrimaryColor = if (isPhotoActive) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val activeSecondaryColor = if (isPhotoActive) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val activeAccentColor = if (isPhotoActive) 0xFFFBBF24.toInt() else style.accentColor
        val activeFrameColor = if (isPhotoActive) 0x4DFFFFFF.toInt() else style.frameColor

        // 3. Hairline Inset Sacred Border
        val frameInset = min(width, height) * 0.042f
        val frameRadius = min(width, height) * 0.035f
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = min(width, height) * 0.0016f
            color = activeFrameColor
        }
        val frameRect = RectF(
            frameInset,
            frameInset,
            width - frameInset,
            height - frameInset
        )
        canvas.drawRoundRect(frameRect, frameRadius, frameRadius, framePaint)

        // Draw delicate corner ornaments
        drawCornerOrnaments(canvas, frameRect, frameRadius, activeAccentColor)

        // Load sacred typography
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular)
            ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic)
            ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular)
            ?: Typeface.SANS_SERIF

        // Dynamic text widths & max boundaries
        val contentMaxWidth = (width - (frameInset * 2.8f)).toInt().coerceAtLeast(400)

        // 4. Header Category Badge
        val headerText = "M A K A R I O S   ·   ${category.uppercase().replace(" ", "   ")}"
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = min(width, height) * 0.016f
            color = activeAccentColor
            alpha = if (isPhotoActive || style.isDark) 240 else 220
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) {
                setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
            }
        }
        val headerHeight = headerPaint.textSize

        // 5. Declaration Text
        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val fullDeclaration = "“$cleanDeclaration”"

        val declFontSize = calculateDeclarationSize(min(width, height), cleanDeclaration.length)
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = declFontSize
            color = activePrimaryColor
            if (isPhotoActive) {
                setShadowLayer(8f, 0f, 2f, 0xCC000000.toInt())
            }
            // Note: Paint.Align.CENTER is intentionally omitted here because StaticLayout
            // handles centered alignment across [0, contentMaxWidth] internally via ALIGN_CENTER.
        }
        val declLayout = createCenteredStaticLayout(fullDeclaration, declPaint, contentMaxWidth)

        // 6. Ornamental Center Divider
        val dividerWidth = min(width, height) * 0.16f
        val dividerHeight = min(width, height) * 0.0016f
        val dividerSpacing = min(width, height) * 0.024f

        // 7. Grounding Scripture Text
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        val fullScripture = if (cleanScripture.isNotBlank()) "“$cleanScripture”" else ""

        val scriptFontSize = min(width, height) * 0.027f
        val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = scriptFontSize
            color = activeSecondaryColor
            if (isPhotoActive) {
                setShadowLayer(6f, 0f, 2f, 0xBB000000.toInt())
            }
            // Note: Paint.Align.CENTER is intentionally omitted here for StaticLayout compatibility.
        }
        val scriptLayout = if (fullScripture.isNotBlank()) {
            createCenteredStaticLayout(fullScripture, scriptPaint, (contentMaxWidth * 0.92f).toInt())
        } else null

        // 8. Scripture Reference
        val cleanReference = reference.trim().uppercase()
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = min(width, height) * 0.018f
            color = activeAccentColor
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) {
                setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
            }
        }
        val refHeight = refPaint.textSize

        // 9. Vertical Balancing & Safe Zones
        val spaceAfterHeader = min(width, height) * 0.030f
        val spaceAfterDecl = min(width, height) * 0.024f
        val spaceAfterDiv = min(width, height) * 0.024f
        val spaceAfterScript = min(width, height) * 0.016f

        val totalContentHeight = headerHeight + spaceAfterHeader +
                declLayout.height + spaceAfterDecl +
                dividerHeight + spaceAfterDiv +
                (scriptLayout?.height?.toFloat() ?: 0f) + spaceAfterScript +
                refHeight

        // On wallpaper format, shift optical center downward slightly (35% to 75%)
        // so the phone lock-screen clock & date (top 25%) don't collide with the declaration.
        val startY = if (format.isWallpaper) {
            val opticalCenter = height * 0.54f
            (opticalCenter - (totalContentHeight / 2f)).coerceAtLeast(height * 0.28f)
        } else {
            ((height - totalContentHeight) / 2f).coerceAtLeast(frameInset * 1.5f)
        }

        var currentY = startY

        // Draw Header
        currentY += headerHeight
        canvas.drawText(headerText, width / 2f, currentY, headerPaint)
        currentY += spaceAfterHeader

        // Draw Declaration
        canvas.save()
        canvas.translate((width - contentMaxWidth) / 2f, currentY)
        declLayout.draw(canvas)
        canvas.restore()
        currentY += declLayout.height + spaceAfterDecl

        // Draw Ornamental Divider with diamond center
        drawSacredDivider(canvas, width / 2f, currentY, dividerWidth, dividerHeight, activeAccentColor)
        currentY += dividerHeight + spaceAfterDiv

        // Draw Grounding Scripture
        if (scriptLayout != null) {
            val scriptMaxWidth = (contentMaxWidth * 0.92f).toInt()
            canvas.save()
            canvas.translate((width - scriptMaxWidth) / 2f, currentY)
            scriptLayout.draw(canvas)
            canvas.restore()
            currentY += scriptLayout.height + spaceAfterScript
        }

        // Draw Scripture Reference
        currentY += refHeight
        val trackedRef = cleanReference.map { "$it " }.joinToString("").trim()
        canvas.drawText("— $trackedRef —", width / 2f, currentY, refPaint)

        // 10. Subtle Footer Wordmark
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = min(width, height) * 0.018f
            color = activePrimaryColor
            alpha = if (isPhotoActive) 140 else if (style.isDark) 90 else 80
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) {
                setShadowLayer(3f, 0f, 1f, 0x88000000.toInt())
            }
        }
        val footerY = height - (frameInset * 1.6f)
        canvas.drawText("makarios  ·  speak truth  ·  walk blessed", width / 2f, footerY, footerPaint)

        return bitmap
    }

    /**
     * Loads a Bitmap from a network or cache URL using Coil with hardware bitmaps disabled.
     */
    suspend fun fetchBitmapFromUrl(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = coil.request.ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = coil.Coil.imageLoader(context).execute(request)
            if (result is coil.request.SuccessResult) {
                result.drawable.toBitmap()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateDeclarationSize(baseDimension: Int, length: Int): Float {
        return when {
            length < 50 -> baseDimension * 0.046f
            length < 100 -> baseDimension * 0.040f
            length < 180 -> baseDimension * 0.034f
            else -> baseDimension * 0.029f
        }
    }

    private fun createCenteredStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        maxWidth: Int
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.28f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                maxWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.28f,
                0f,
                false
            )
        }
    }

    private fun drawCornerOrnaments(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        accentColor: Int
    ) {
        val ornamentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = accentColor
            alpha = 110
        }

        val size = radius * 0.6f
        val offset = radius * 0.45f

        // Top-Left corner tick
        canvas.drawLine(rect.left + offset, rect.top + offset, rect.left + offset + size, rect.top + offset, ornamentPaint)
        canvas.drawLine(rect.left + offset, rect.top + offset, rect.left + offset, rect.top + offset + size, ornamentPaint)

        // Top-Right corner tick
        canvas.drawLine(rect.right - offset, rect.top + offset, rect.right - offset - size, rect.top + offset, ornamentPaint)
        canvas.drawLine(rect.right - offset, rect.top + offset, rect.right - offset, rect.top + offset + size, ornamentPaint)

        // Bottom-Left corner tick
        canvas.drawLine(rect.left + offset, rect.bottom - offset, rect.left + offset + size, rect.bottom - offset, ornamentPaint)
        canvas.drawLine(rect.left + offset, rect.bottom - offset, rect.left + offset, rect.bottom - offset - size, ornamentPaint)

        // Bottom-Right corner tick
        canvas.drawLine(rect.right - offset, rect.bottom - offset, rect.right - offset - size, rect.bottom - offset, ornamentPaint)
        canvas.drawLine(rect.right - offset, rect.bottom - offset, rect.right - offset, rect.bottom - offset - size, ornamentPaint)
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
        val diamondRadius = totalWidth * 0.045f

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = lineThickness
            alpha = 140
        }

        // Left hairline
        canvas.drawLine(centerX - half, centerY, centerX - diamondRadius * 1.6f, centerY, linePaint)

        // Right hairline
        canvas.drawLine(centerX + diamondRadius * 1.6f, centerY, centerX + half, centerY, linePaint)

        // Center diamond
        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
            alpha = 180
        }
        val diamondPath = Path().apply {
            moveTo(centerX, centerY - diamondRadius)
            lineTo(centerX + diamondRadius, centerY)
            lineTo(centerX, centerY + diamondRadius)
            lineTo(centerX - diamondRadius, centerY)
            close()
        }
        canvas.drawPath(diamondPath, diamondPaint)
    }

    /**
     * Save the rendered bitmap to the system gallery under "Pictures/Makarios".
     */
    fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String = "Makarios"
    ): Uri? {
        val filename = "Makarios_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Makarios")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    throw IOException("Failed to compress bitmap into PNG stream")
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            e.printStackTrace()
            null
        }
    }

    /**
     * Set the rendered bitmap as the system wallpaper (Home, Lock, or Both).
     */
    fun setAsSystemWallpaper(
        context: Context,
        bitmap: Bitmap,
        target: WallpaperTarget = WallpaperTarget.BOTH
    ): Boolean {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val which = when (target) {
                    WallpaperTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                    WallpaperTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                    WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, which)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
