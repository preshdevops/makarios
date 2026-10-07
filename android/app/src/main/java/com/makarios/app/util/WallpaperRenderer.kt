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
        val platformName: String,
        val width: Int,
        val height: Int,
        val isWallpaper: Boolean,
        val aspectDescription: String
    ) {
        STORY("Instagram Story", "Instagram", 1080, 1920, false, "9:16 Story"),
        SQUARE("Instagram Post", "Instagram", 1080, 1080, false, "1:1 Archival Card"),
        SNAPCHAT("Snapchat Story", "Snapchat", 1080, 1920, false, "9:16 Frosted Lens"),
        X_CARD("X / Twitter Card", "X", 1200, 675, false, "16:9 Pull-Quote"),
        STATUS("WhatsApp Status", "WhatsApp", 1080, 1350, false, "4:5 Blessing Card"),
        WALLPAPER("Phone Wallpaper", "Lock Screen", 1080, 2400, true, "9:20 Wallpaper");

        companion object {
            fun fromIndex(index: Int): OutputFormat {
                return when (index) {
                    0 -> STORY
                    1 -> SQUARE
                    2 -> SNAPCHAT
                    3 -> X_CARD
                    4 -> STATUS
                    5 -> WALLPAPER
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
        // 0: Alabaster Dawn — Alabaster linen ground with deep olive
        RenderStyle(
            name = "Alabaster",
            backgroundColors = intArrayOf(
                0xFFFAF7F2.toInt(),
                0xFFF3EDE4.toInt()
            ),
            primaryTextColor = 0xFF2C2622.toInt(),      // Deep warm Espresso
            secondaryTextColor = 0xFF655C54.toInt(),    // Warm Stone
            accentColor = 0xFF4A5A3C.toInt(),           // Olive
            frameColor = 0x244A5A3C.toInt(),            // Olive hairline
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
        // 3: Rose Dawn — Morning sunrise
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
    /**
     * Render an affirmation into an exquisite, production-grade Bitmap tailored specifically
     * to the chosen social media or device platform format.
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
        return when (format) {
            OutputFormat.STORY -> renderInstagramStory(context, declaration, scripture, reference, category, style, photoBitmap)
            OutputFormat.SQUARE -> renderInstagramPost(context, declaration, scripture, reference, category, style, photoBitmap)
            OutputFormat.SNAPCHAT -> renderSnapchatStory(context, declaration, scripture, reference, category, style, photoBitmap)
            OutputFormat.X_CARD -> renderXCard(context, declaration, scripture, reference, category, style, photoBitmap)
            OutputFormat.STATUS -> renderWhatsAppCard(context, declaration, scripture, reference, category, style, photoBitmap)
            OutputFormat.WALLPAPER -> renderLockscreenWallpaper(context, declaration, scripture, reference, category, style, photoBitmap)
        }
    }

    /**
     * 1. INSTAGRAM STORY (9:16 — 1080 x 1920)
     * Tailored safe zones: top 240px for story header, bottom 260px for reply bar.
     * Features illuminated translucent scripture plaque and radiant vertical balance.
     */
    private fun renderInstagramStory(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String,
        style: RenderStyle,
        photoBitmap: Bitmap?
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isPhotoActive = photoBitmap != null

        // 1. Background & Scrim
        drawBackgroundAndScrim(canvas, width, height, style, photoBitmap)

        // Palette
        val activePrimaryColor = if (isPhotoActive) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val activeSecondaryColor = if (isPhotoActive) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val activeAccentColor = if (isPhotoActive) 0xFFFBBF24.toInt() else style.accentColor
        val activeFrameColor = if (isPhotoActive) 0x4DFFFFFF.toInt() else style.frameColor

        // 2. Delicate hairline frame
        val frameInset = 46f
        val frameRadius = 26f
        val frameRect = RectF(frameInset, frameInset, width - frameInset, height - frameInset)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = activeFrameColor
        }
        canvas.drawRoundRect(frameRect, frameRadius, frameRadius, framePaint)
        drawCornerOrnaments(canvas, frameRect, frameRadius, activeAccentColor)

        // Fonts
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular) ?: Typeface.SANS_SERIF

        // 3. Top Header Seal (safe zone: y = 290)
        val headerY = 290f
        val headerText = category.uppercase()
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 13f
            color = activeAccentColor
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
        }
        canvas.drawText(headerText, width / 2f, headerY, headerPaint)

        // 4. Hero Declaration (y = 480 to 980)
        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val fullDeclaration = "“$cleanDeclaration”"
        val maxContentWidth = 880
        val declFontSize = calculateDeclarationSize(width, cleanDeclaration.length) * 1.08f
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = declFontSize
            color = activePrimaryColor
            if (isPhotoActive) setShadowLayer(8f, 0f, 2f, 0xCC000000.toInt())
        }
        val declLayout = createCenteredStaticLayout(fullDeclaration, declPaint, maxContentWidth)

        // 5. Grounding Scripture Plaque
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        val fullScripture = if (cleanScripture.isNotBlank()) "“$cleanScripture”" else ""
        val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = 30f
            color = activeSecondaryColor
            if (isPhotoActive) setShadowLayer(6f, 0f, 2f, 0xBB000000.toInt())
        }
        val scriptLayout = if (fullScripture.isNotBlank()) {
            createCenteredStaticLayout(fullScripture, scriptPaint, 760)
        } else null

        // 6. Scripture Reference
        val cleanReference = reference.trim().uppercase()
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 19f
            color = activeAccentColor
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
        }

        // Layout vertical distribution inside [360, 1560]
        val declStartY = 460f
        canvas.save()
        canvas.translate((width - maxContentWidth) / 2f, declStartY)
        declLayout.draw(canvas)
        canvas.restore()

        val dividerY = declStartY + declLayout.height + 40f
        drawSacredDivider(canvas, width / 2f, dividerY, 220f, 1.8f, activeAccentColor)

        // Draw Scripture in illuminated plaque
        if (scriptLayout != null) {
            val plaqueTop = dividerY + 44f
            val plaqueHeight = scriptLayout.height + 110f
            val plaqueRect = RectF(110f, plaqueTop, width - 110f, plaqueTop + plaqueHeight)

            // Plaque background & border
            val plaqueBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPhotoActive || style.isDark) 0x2E000000.toInt() else 0x14000000.toInt()
                this.style = Paint.Style.FILL
            }
            val plaqueStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                strokeWidth = 1.4f
                color = activeAccentColor
                alpha = 90
            }
            canvas.drawRoundRect(plaqueRect, 22f, 22f, plaqueBgPaint)
            canvas.drawRoundRect(plaqueRect, 22f, 22f, plaqueStrokePaint)

            // Scripture text
            canvas.save()
            canvas.translate((width - 760) / 2f, plaqueTop + 30f)
            scriptLayout.draw(canvas)
            canvas.restore()

            // Reference inside plaque
            val trackedRef = cleanReference.map { "$it " }.joinToString("").trim()
            canvas.drawText("— $trackedRef —", width / 2f, plaqueTop + 40f + scriptLayout.height + 24f, refPaint)
        }

        return bitmap
    }

    /**
     * 2. INSTAGRAM POST (1:1 Archival Card — 1080 x 1080)
     * Museum-grade double hairline architectural frame with gold corner diamond nodes,
     * monumental quotation mark, and left-accented scripture plate.
     */
    private fun renderInstagramPost(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String,
        style: RenderStyle,
        photoBitmap: Bitmap?
    ): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isPhotoActive = photoBitmap != null

        // 1. Background & Scrim
        drawBackgroundAndScrim(canvas, width, height, style, photoBitmap)

        val activePrimaryColor = if (isPhotoActive) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val activeSecondaryColor = if (isPhotoActive) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val activeAccentColor = if (isPhotoActive) 0xFFFBBF24.toInt() else style.accentColor

        // 2. Archival Double Hairline Frame
        val outerInset = 42f
        val innerInset = 54f
        val outerRect = RectF(outerInset, outerInset, width - outerInset, height - outerInset)
        val innerRect = RectF(innerInset, innerInset, width - innerInset, height - innerInset)

        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 2.0f
            color = activeAccentColor
            alpha = 160
        }
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 1.0f
            color = activeAccentColor
            alpha = 90
        }
        canvas.drawRoundRect(outerRect, 22f, 22f, outerPaint)
        canvas.drawRoundRect(innerRect, 16f, 16f, innerPaint)

        // Corner diamond nodes
        drawCornerDiamonds(canvas, innerRect, 14f, activeAccentColor)

        // Fonts
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular) ?: Typeface.SANS_SERIF

        // 3. Category Header
        val headerText = category.uppercase()
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 13f
            color = activeAccentColor
            letterSpacing = 0.12f
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
        }
        canvas.drawText(headerText, width / 2f, 114f, headerPaint)

        // 4. Hero Declaration with Monumental Floating Quote Mark
        val quoteMarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = 80f
            color = activeAccentColor
            alpha = 180
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("“", width / 2f, 190f, quoteMarkPaint)

        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val declFontSize = calculateDeclarationSize(width, cleanDeclaration.length) * 1.05f
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = declFontSize
            color = activePrimaryColor
            if (isPhotoActive) setShadowLayer(8f, 0f, 2f, 0xCC000000.toInt())
        }
        val declMaxWidth = 880
        val declLayout = createCenteredStaticLayout(cleanDeclaration, declPaint, declMaxWidth)

        val declY = 220f
        canvas.save()
        canvas.translate((width - declMaxWidth) / 2f, declY)
        declLayout.draw(canvas)
        canvas.restore()

        // 5. Three-Diamond Ornamental Divider
        val dividerY = declY + declLayout.height + 34f
        drawThreeDiamondDivider(canvas, width / 2f, dividerY, 9f, 20f, activeAccentColor)

        // 6. Grounding Scripture Plaque with Gold Left Border
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        if (cleanScripture.isNotBlank()) {
            val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = cormorantItalic
                textSize = 27f
                color = activeSecondaryColor
                if (isPhotoActive) setShadowLayer(6f, 0f, 2f, 0xBB000000.toInt())
            }
            val scriptMaxWidth = 780
            val scriptLayout = createCenteredStaticLayout("“$cleanScripture”", scriptPaint, scriptMaxWidth)

            val plaqueTop = dividerY + 36f
            val plaqueHeight = scriptLayout.height + 70f
            val plaqueRect = RectF(110f, plaqueTop, width - 110f, plaqueTop + plaqueHeight)

            val plaqueBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPhotoActive || style.isDark) 0x33000000.toInt() else 0x16000000.toInt()
                this.style = Paint.Style.FILL
            }
            canvas.drawRoundRect(plaqueRect, 16f, 16f, plaqueBg)

            // Accent bar on left edge
            val leftBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeAccentColor
                strokeWidth = 4.5f
                this.style = Paint.Style.STROKE
            }
            canvas.drawLine(plaqueRect.left, plaqueRect.top + 16f, plaqueRect.left, plaqueRect.bottom - 16f, leftBar)

            // Scripture text
            canvas.save()
            canvas.translate((width - scriptMaxWidth) / 2f, plaqueTop + 20f)
            scriptLayout.draw(canvas)
            canvas.restore()

            // Reference below scripture
            val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = workSansRegular
                textSize = 17f
                color = activeAccentColor
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val cleanReference = reference.trim().uppercase()
            canvas.drawText("◆  $cleanReference  ◆", width / 2f, plaqueTop + scriptLayout.height + 50f, refPaint)
        }

        return bitmap
    }

    /**
     * 3. SNAPCHAT STORY (9:16 — 1080 x 1920)
     * Modern Sacred Frosted Lens with rounded floating container card,
     * vibrant contrast, and amber gold badge tag.
     */
    private fun renderSnapchatStory(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String,
        style: RenderStyle,
        photoBitmap: Bitmap?
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isPhotoActive = photoBitmap != null

        // 1. Background & Atmosphere
        drawBackgroundAndScrim(canvas, width, height, style, photoBitmap)

        val activePrimaryColor = if (isPhotoActive) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val activeSecondaryColor = if (isPhotoActive) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val activeAccentColor = if (isPhotoActive) 0xFFFBBF24.toInt() else style.accentColor

        // 2. Floating Frosted Container Card
        val cardLeft = 70f
        val cardRight = width - 70f
        val cardTop = 300f
        val cardBottom = 1580f
        val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)

        // Drop shadow for container
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x66000000.toInt()
            maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(cardRect, 36f, 36f, shadowPaint)

        // Card fill (translucent frosted tone)
        val cardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isPhotoActive || style.isDark) 0xD01C1714.toInt() else 0xE8FBF8F4.toInt()
            this.style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 36f, 36f, cardFill)

        // Card luminous border
        val cardStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 2.4f
            color = activeAccentColor
            alpha = 140
        }
        canvas.drawRoundRect(cardRect, 36f, 36f, cardStroke)

        // Fonts
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular) ?: Typeface.SANS_SERIF

        // 3. Category label — simple centered text at top of card
        val catPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 13f
            color = activeAccentColor
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(category.uppercase(), width / 2f, cardTop + 60f, catPaint)

        // 4. Declaration inside card
        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val declFontSize = calculateDeclarationSize(width, cleanDeclaration.length) * 1.10f
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = declFontSize
            color = if (style.isDark || isPhotoActive) 0xFFFFFFFF.toInt() else 0xFF2C2622.toInt()
        }
        val declLayout = createCenteredStaticLayout("“$cleanDeclaration”", declPaint, 800)
        val declY = cardTop + 100f
        canvas.save()
        canvas.translate((width - 800) / 2f, declY)
        declLayout.draw(canvas)
        canvas.restore()

        // 5. Sacred Divider
        val divY = declY + declLayout.height + 40f
        drawSacredDivider(canvas, width / 2f, divY, 200f, 1.8f, activeAccentColor)

        // 6. Scripture Inset Card inside container
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        if (cleanScripture.isNotBlank()) {
            val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = cormorantItalic
                textSize = 29f
                color = if (style.isDark || isPhotoActive) 0xEEFFFFFF.toInt() else 0xFF4D443D.toInt()
            }
            val scriptLayout = createCenteredStaticLayout("“$cleanScripture”", scriptPaint, 720)

            val insetTop = divY + 40f
            val insetHeight = scriptLayout.height + 95f
            val insetRect = RectF(120f, insetTop, width - 120f, insetTop + insetHeight)

            val insetBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (style.isDark || isPhotoActive) 0x33000000.toInt() else 0x18000000.toInt()
                this.style = Paint.Style.FILL
            }
            val insetBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                strokeWidth = 1.4f
                color = activeAccentColor
                alpha = 90
            }
            canvas.drawRoundRect(insetRect, 22f, 22f, insetBg)
            canvas.drawRoundRect(insetRect, 22f, 22f, insetBorder)

            canvas.save()
            canvas.translate((width - 720) / 2f, insetTop + 24f)
            scriptLayout.draw(canvas)
            canvas.restore()

            // Reference Pill
            val cleanReference = reference.trim().uppercase()
            val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = workSansRegular
                textSize = 18f
                color = activeAccentColor
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("— $cleanReference —", width / 2f, insetTop + scriptLayout.height + 62f, refPaint)
        }

        return bitmap
    }

    /**
     * 4. X / TWITTER CARD (16:9 Landscape — 1200 x 675)
     * Editorial Broadsheet Pull-Quote Layout:
     * Left vertical accent bar, monumental quote mark, left-aligned broad declaration,
     * fine horizontal separator, and right-aligned Makarios signature.
     */
    private fun renderXCard(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String,
        style: RenderStyle,
        photoBitmap: Bitmap?
    ): Bitmap {
        val width = 1200
        val height = 675
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isPhotoActive = photoBitmap != null

        // 1. Background & Scrim
        drawBackgroundAndScrim(canvas, width, height, style, photoBitmap)

        val activePrimaryColor = if (isPhotoActive) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val activeSecondaryColor = if (isPhotoActive) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val activeAccentColor = if (isPhotoActive) 0xFFFBBF24.toInt() else style.accentColor

        // 2. Fine Outer Border
        val frameInset = 28f
        val frameRect = RectF(frameInset, frameInset, width - frameInset, height - frameInset)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 1.4f
            color = activeAccentColor
            alpha = 100
        }
        canvas.drawRoundRect(frameRect, 18f, 18f, framePaint)

        // Fonts
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular) ?: Typeface.SANS_SERIF

        // 3. Left Vertical Accent Bar (Editorial pull-quote signature)
        val barLeft = 68f
        val barRight = 75f
        val barTop = 90f
        val barBottom = height - 90f
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeAccentColor
            this.style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(barLeft, barTop, barRight, barBottom), 3f, 3f, barPaint)

        // 4. Header Bar (Top Right)
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 13f
            letterSpacing = 0.12f
            color = activeAccentColor
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(category.uppercase(), width - 68f, 74f, headerPaint)

        // 5. Massive Quote Mark Glyph
        val quoteMarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = 72f
            color = activeAccentColor
            alpha = 190
        }
        canvas.drawText("“", 98f, 150f, quoteMarkPaint)

        // 6. Left-Aligned Declaration Text
        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val contentWidth = 1010
        val declFontSize = calculateDeclarationSize(height, cleanDeclaration.length) * 0.95f
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = declFontSize
            color = activePrimaryColor
            if (isPhotoActive) setShadowLayer(6f, 0f, 2f, 0xCC000000.toInt())
        }
        val declLayout = createLeftStaticLayout(cleanDeclaration, declPaint, contentWidth)

        val declY = 135f
        canvas.save()
        canvas.translate(98f, declY)
        declLayout.draw(canvas)
        canvas.restore()

        // 7. Horizontal Separator Line
        val ruleY = declY + declLayout.height + 24f
        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeAccentColor
            strokeWidth = 1.2f
            alpha = 110
        }
        canvas.drawLine(98f, ruleY, 98f + 260f, ruleY, rulePaint)

        // 8. Grounding Scripture below rule
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = 23f
            color = activeSecondaryColor
            if (isPhotoActive) setShadowLayer(4f, 0f, 1f, 0xBB000000.toInt())
        }
        val scriptLayout = if (cleanScripture.isNotBlank()) {
            createLeftStaticLayout("“$cleanScripture”", scriptPaint, contentWidth)
        } else null

        if (scriptLayout != null) {
            canvas.save()
            canvas.translate(98f, ruleY + 16f)
            scriptLayout.draw(canvas)
            canvas.restore()
        }

        // 9. Bottom Row: Scripture Reference Pill & Brand Wordmark
        val bottomY = height - 56f
        val cleanReference = reference.trim().uppercase()
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 15f
            isFakeBoldText = true
            color = activeAccentColor
        }
        canvas.drawText("— $cleanReference", 98f, bottomY, refPaint)

        return bitmap
    }

    /**
     * 5. WHATSAPP STATUS / CHAT CARD (4:5 — 1080 x 1350)
     * Sacred Devotional Letter / Blessing Card with high-contrast compression-proof clarity,
     * cross divider, and devotional sharing footer.
     */
    private fun renderWhatsAppCard(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String,
        style: RenderStyle,
        photoBitmap: Bitmap?
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isPhotoActive = photoBitmap != null

        // 1. Background & Scrim
        drawBackgroundAndScrim(canvas, width, height, style, photoBitmap)

        val activePrimaryColor = if (isPhotoActive) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val activeSecondaryColor = if (isPhotoActive) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val activeAccentColor = if (isPhotoActive) 0xFFFBBF24.toInt() else style.accentColor

        // 2. Inset Devotional Border with Sacred Cross Top Emblem
        val frameInset = 40f
        val frameRect = RectF(frameInset, frameInset, width - frameInset, height - frameInset)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 1.8f
            color = activeAccentColor
            alpha = 150
        }
        canvas.drawRoundRect(frameRect, 24f, 24f, framePaint)
        drawCornerOrnaments(canvas, frameRect, 24f, activeAccentColor)

        // Fonts
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular) ?: Typeface.SANS_SERIF

        // 3. Category Header
        val headerText = category.uppercase()
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 15f
            letterSpacing = 0.14f
            color = activeAccentColor
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
        }
        canvas.drawText(headerText, width / 2f, 110f, headerPaint)

        // 4. Hero Declaration
        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val declMaxWidth = 860
        val declFontSize = calculateDeclarationSize(width, cleanDeclaration.length) * 1.06f
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = declFontSize
            color = activePrimaryColor
            if (isPhotoActive) setShadowLayer(8f, 0f, 2f, 0xCC000000.toInt())
        }
        val declLayout = createCenteredStaticLayout("“$cleanDeclaration”", declPaint, declMaxWidth)

        val declY = 170f
        canvas.save()
        canvas.translate((width - declMaxWidth) / 2f, declY)
        declLayout.draw(canvas)
        canvas.restore()

        // 5. Sacred Divider
        val divY = declY + declLayout.height + 40f
        drawSacredDivider(canvas, width / 2f, divY, 220f, 1.8f, activeAccentColor)

        // 6. Grounding Scripture Parchment Inset
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        if (cleanScripture.isNotBlank()) {
            val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = cormorantItalic
                textSize = 28f
                color = activeSecondaryColor
                if (isPhotoActive) setShadowLayer(6f, 0f, 2f, 0xBB000000.toInt())
            }
            val scriptMaxWidth = 760
            val scriptLayout = createCenteredStaticLayout("“$cleanScripture”", scriptPaint, scriptMaxWidth)

            val plaqueTop = divY + 40f
            val plaqueHeight = scriptLayout.height + 95f
            val plaqueRect = RectF(100f, plaqueTop, width - 100f, plaqueTop + plaqueHeight)

            val plaqueBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPhotoActive || style.isDark) 0x2AFFFFFF.toInt() else 0x16000000.toInt()
                this.style = Paint.Style.FILL
            }
            val plaqueStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                strokeWidth = 1.4f
                color = activeAccentColor
                alpha = 90
            }
            canvas.drawRoundRect(plaqueRect, 20f, 20f, plaqueBg)
            canvas.drawRoundRect(plaqueRect, 20f, 20f, plaqueStroke)

            canvas.save()
            canvas.translate((width - scriptMaxWidth) / 2f, plaqueTop + 24f)
            scriptLayout.draw(canvas)
            canvas.restore()

            // Reference inside plaque
            val cleanReference = reference.trim().uppercase()
            val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = workSansRegular
                textSize = 18f
                color = activeAccentColor
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val trackedRef = cleanReference.map { "$it " }.joinToString("").trim()
            canvas.drawText("— $trackedRef —", width / 2f, plaqueTop + scriptLayout.height + 62f, refPaint)
        }

        return bitmap
    }

    /**
     * 6. LOCKSCREEN / HOME PHONE WALLPAPER (9:20 — 1080 x 2400)
     * Preserves top 28% (670px) completely cleared for system clock, date, notification icons,
     * and lockscreen complication widgets.
     */
    private fun renderLockscreenWallpaper(
        context: Context,
        declaration: String,
        scripture: String,
        reference: String,
        category: String,
        style: RenderStyle,
        photoBitmap: Bitmap?
    ): Bitmap {
        val width = 1080
        val height = 2400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isPhotoActive = photoBitmap != null

        // 1. Background & Scrim
        drawBackgroundAndScrim(canvas, width, height, style, photoBitmap)

        val activePrimaryColor = if (isPhotoActive) 0xFFFFFFFF.toInt() else style.primaryTextColor
        val activeSecondaryColor = if (isPhotoActive) 0xEEFFFFFF.toInt() else style.secondaryTextColor
        val activeAccentColor = if (isPhotoActive) 0xFFFBBF24.toInt() else style.accentColor
        val activeFrameColor = if (isPhotoActive) 0x4DFFFFFF.toInt() else style.frameColor

        // 2. Hairline Inset Sacred Border
        val frameInset = 44f
        val frameRadius = 32f
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = activeFrameColor
        }
        val frameRect = RectF(frameInset, frameInset, width - frameInset, height - frameInset)
        canvas.drawRoundRect(frameRect, frameRadius, frameRadius, framePaint)
        drawCornerOrnaments(canvas, frameRect, frameRadius, activeAccentColor)

        // Fonts
        val cormorantRegular = ResourcesCompat.getFont(context, R.font.cormorant_garamond_regular) ?: Typeface.SERIF
        val cormorantItalic = ResourcesCompat.getFont(context, R.font.cormorant_garamond_italic) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val workSansRegular = ResourcesCompat.getFont(context, R.font.worksans_regular) ?: Typeface.SANS_SERIF

        val contentMaxWidth = 860

        // 3. Category Header
        val headerText = category.uppercase()
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 17f
            color = activeAccentColor
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
        }
        val headerHeight = headerPaint.textSize

        // 4. Hero Declaration
        val cleanDeclaration = declaration.trim().removePrefix("“").removeSuffix("”")
        val declFontSize = calculateDeclarationSize(width, cleanDeclaration.length)
        val declPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantRegular
            textSize = declFontSize
            color = activePrimaryColor
            if (isPhotoActive) setShadowLayer(8f, 0f, 2f, 0xCC000000.toInt())
        }
        val declLayout = createCenteredStaticLayout("“$cleanDeclaration”", declPaint, contentMaxWidth)

        // 5. Divider
        val dividerWidth = 180f
        val dividerHeight = 1.8f

        // 6. Grounding Scripture
        val cleanScripture = scripture.trim().removePrefix("“").removeSuffix("”")
        val scriptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = 29f
            color = activeSecondaryColor
            if (isPhotoActive) setShadowLayer(6f, 0f, 2f, 0xBB000000.toInt())
        }
        val scriptLayout = if (cleanScripture.isNotBlank()) {
            createCenteredStaticLayout("“$cleanScripture”", scriptPaint, (contentMaxWidth * 0.92f).toInt())
        } else null

        // 7. Reference
        val cleanReference = reference.trim().uppercase()
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = workSansRegular
            textSize = 19f
            color = activeAccentColor
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) setShadowLayer(4f, 0f, 1f, 0x99000000.toInt())
        }
        val refHeight = refPaint.textSize

        // Vertical balancing with top 28% lockscreen clock clearance
        val spaceAfterHeader = 34f
        val spaceAfterDecl = 32f
        val spaceAfterDiv = 32f
        val spaceAfterScript = 24f

        val totalContentHeight = headerHeight + spaceAfterHeader +
                declLayout.height + spaceAfterDecl +
                dividerHeight + spaceAfterDiv +
                (scriptLayout?.height?.toFloat() ?: 0f) + spaceAfterScript +
                refHeight

        // Optical center shifted to 54% height with clock clearance
        val opticalCenter = height * 0.54f
        val startY = (opticalCenter - (totalContentHeight / 2f)).coerceAtLeast(height * 0.28f)

        var currentY = startY

        // Header
        currentY += headerHeight
        canvas.drawText(headerText, width / 2f, currentY, headerPaint)
        currentY += spaceAfterHeader

        // Declaration
        canvas.save()
        canvas.translate((width - contentMaxWidth) / 2f, currentY)
        declLayout.draw(canvas)
        canvas.restore()
        currentY += declLayout.height + spaceAfterDecl

        // Divider
        drawSacredDivider(canvas, width / 2f, currentY, dividerWidth, dividerHeight, activeAccentColor)
        currentY += dividerHeight + spaceAfterDiv

        // Scripture
        if (scriptLayout != null) {
            val scriptMaxWidth = (contentMaxWidth * 0.92f).toInt()
            canvas.save()
            canvas.translate((width - scriptMaxWidth) / 2f, currentY)
            scriptLayout.draw(canvas)
            canvas.restore()
            currentY += scriptLayout.height + spaceAfterScript
        }

        // Reference
        currentY += refHeight
        val trackedRef = cleanReference.map { "$it " }.joinToString("").trim()
        canvas.drawText("— $trackedRef —", width / 2f, currentY, refPaint)

        // Footer Wordmark
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = cormorantItalic
            textSize = 19f
            color = activePrimaryColor
            alpha = if (isPhotoActive) 140 else if (style.isDark) 90 else 80
            textAlign = Paint.Align.CENTER
            if (isPhotoActive) setShadowLayer(3f, 0f, 1f, 0x88000000.toInt())
        }
        val footerY = height - (frameInset * 1.8f)
        canvas.drawText("makarios  ·  speak truth  ·  walk blessed", width / 2f, footerY, footerPaint)

        return bitmap
    }

    /**
     * Common drawing helper for background gradient or photo center-crop + scrim.
     */
    private fun drawBackgroundAndScrim(
        canvas: Canvas,
        width: Int,
        height: Int,
        style: RenderStyle,
        photoBitmap: Bitmap?
    ) {
        if (photoBitmap != null) {
            val matrix = Matrix()
            val scale = max(width.toFloat() / photoBitmap.width, height.toFloat() / photoBitmap.height)
            val dx = (width - photoBitmap.width * scale) * 0.5f
            val dy = (height - photoBitmap.height * scale) * 0.5f
            matrix.setScale(scale, scale)
            matrix.postTranslate(dx, dy)
            val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
            canvas.drawBitmap(photoBitmap, matrix, photoPaint)

            // Multi-stop protective scrim
            val scrimShader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    0x4D000000.toInt(),
                    0x660E0B08.toInt(),
                    0x990E0B08.toInt(),
                    0xE60A0806.toInt()
                ),
                floatArrayOf(0f, 0.30f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
            val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = scrimShader }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        } else {
            // Background Gradient
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

            // Soft Ambient Radial Glow
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

    private fun createLeftStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        maxWidth: Int
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.26f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                maxWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.26f,
                0f,
                false
            )
        }
    }

    private fun drawCornerDiamonds(
        canvas: Canvas,
        rect: RectF,
        size: Float,
        accentColor: Int
    ) {
        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
            alpha = 190
        }
        val half = size / 2f
        val points = listOf(
            Pair(rect.left, rect.top),
            Pair(rect.right, rect.top),
            Pair(rect.left, rect.bottom),
            Pair(rect.right, rect.bottom)
        )
        for ((cx, cy) in points) {
            val path = Path().apply {
                moveTo(cx, cy - half)
                lineTo(cx + half, cy)
                lineTo(cx, cy + half)
                lineTo(cx - half, cy)
                close()
            }
            canvas.drawPath(path, diamondPaint)
        }
    }

    private fun drawThreeDiamondDivider(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        size: Float,
        spacing: Float,
        accentColor: Int
    ) {
        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
            alpha = 180
        }
        val offsets = listOf(-spacing, 0f, spacing)
        for (off in offsets) {
            val cx = centerX + off
            val s = if (off == 0f) size * 1.25f else size
            val half = s / 2f
            val path = Path().apply {
                moveTo(cx, centerY - half)
                lineTo(cx + half, centerY)
                lineTo(cx, centerY + half)
                lineTo(cx - half, centerY)
                close()
            }
            canvas.drawPath(path, diamondPaint)
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
