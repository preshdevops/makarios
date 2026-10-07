package com.makarios.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ImageEngineTest
 *
 * Unit tests verifying:
 * 1. All exported formats have width >= 1080 (and exactly 1080).
 * 2. Exact dimensions for the three outputs: Story (1080x1920), Square (1080x1080), Portrait Post (1080x1350).
 * 3. Safe margins: 8% horizontal, 14% top/bottom for Story UI overlays.
 * 4. Fixed DP layout coordinates under LocalDensity 3.0.
 * 5. High-resolution URL upgrade stripping thumbnails and applying w=1920.
 * 6. JPEG quality clamping (>= 92 default, never below 85).
 * 7. WallpaperRenderer formats compatibility with width >= 1080.
 */
class ImageEngineTest {

    @Test
    fun exportDimensions_allFormatsHaveWidthAtLeast1080() {
        for (format in ImageOutputFormat.values()) {
            assertTrue(
                "Export format ${format.name} width (${format.width}) must be at least 1080px",
                format.width >= 1080
            )
            assertEquals(
                "All primary output formats must be fixed at exactly 1080px width",
                1080,
                format.width
            )
        }
    }

    @Test
    fun exactDimensions_storySquareAndPortraitPost() {
        // 1. Story & Wallpaper: 1080 x 1920 (9:16)
        assertEquals(1080, ImageOutputFormat.STORY.width)
        assertEquals(1920, ImageOutputFormat.STORY.height)
        assertEquals(ImageOutputFormat.STORY, ImageOutputFormat.WALLPAPER)

        // 2. Square: 1080 x 1080 (1:1)
        assertEquals(1080, ImageOutputFormat.SQUARE.width)
        assertEquals(1080, ImageOutputFormat.SQUARE.height)

        // 3. Portrait Post: 1080 x 1350 (4:5)
        assertEquals(1080, ImageOutputFormat.PORTRAIT_POST.width)
        assertEquals(1350, ImageOutputFormat.PORTRAIT_POST.height)
    }

    @Test
    fun fixedDpSizesUnderDensity3_equalPixelDimensions() {
        for (format in ImageOutputFormat.values()) {
            val computedPixelWidth = (format.targetDpWidth.value * format.density).toInt()
            val computedPixelHeight = (format.targetDpHeight.value * format.density).toInt()

            assertEquals(
                "Format ${format.name} DP width * density (${format.density}) must equal target pixel width",
                format.width,
                computedPixelWidth
            )
            assertEquals(
                "Format ${format.name} DP height * density (${format.density}) must equal target pixel height",
                format.height,
                computedPixelHeight
            )
        }
    }

    @Test
    fun safeMargins_adhereToLegibilityGuidelines() {
        for (format in ImageOutputFormat.values()) {
            // 8% horizontal safe margins
            assertEquals(0.08f, format.horizontalSafeRatio, 0.001f)
            val horizontalMarginPx = format.width * format.horizontalSafeRatio
            assertEquals(86.4f, horizontalMarginPx, 0.01f)
        }

        // Story requires 14% top & bottom clearance for Instagram/Snapchat UI overlays
        assertEquals(0.14f, ImageOutputFormat.STORY.topSafeRatio, 0.001f)
        assertEquals(0.14f, ImageOutputFormat.STORY.bottomSafeRatio, 0.001f)
        val storyTopClearancePx = ImageOutputFormat.STORY.height * ImageOutputFormat.STORY.topSafeRatio
        assertEquals(268.8f, storyTopClearancePx, 0.01f)
    }

    @Test
    fun wallpaperRendererFormats_allWidthsAtLeast1080() {
        for (format in WallpaperRenderer.OutputFormat.values()) {
            assertTrue(
                "WallpaperRenderer format ${format.name} width (${format.width}) must be >= 1080",
                format.width >= 1080
            )
            assertEquals(1080, format.width)
        }

        // Verify WallpaperRenderer format exact resolutions
        assertEquals(1080, WallpaperRenderer.OutputFormat.STORY.width)
        assertEquals(1920, WallpaperRenderer.OutputFormat.STORY.height)

        assertEquals(1080, WallpaperRenderer.OutputFormat.SQUARE.width)
        assertEquals(1080, WallpaperRenderer.OutputFormat.SQUARE.height)

        assertEquals(1080, WallpaperRenderer.OutputFormat.SNAPCHAT.width)
        assertEquals(1920, WallpaperRenderer.OutputFormat.SNAPCHAT.height)

        assertEquals(1080, WallpaperRenderer.OutputFormat.X_CARD.width)
        assertEquals(1350, WallpaperRenderer.OutputFormat.X_CARD.height)

        assertEquals(1080, WallpaperRenderer.OutputFormat.STATUS.width)
        assertEquals(1350, WallpaperRenderer.OutputFormat.STATUS.height)

        assertEquals(1080, WallpaperRenderer.OutputFormat.WALLPAPER.width)
        assertEquals(1920, WallpaperRenderer.OutputFormat.WALLPAPER.height)
    }

    @Test
    fun photoUrlUpgrade_replacesThumbnailsWithHighRes() {
        val thumbnailUrl = "https://images.unsplash.com/photo-1507652313519-d4e9174996dd?w=320&q=80"
        val upgradedUrl = ImageEngine.upgradePhotoUrlForExport(thumbnailUrl)

        assertTrue("Upgraded URL must contain w=1920", upgradedUrl.contains("w=1920"))
        assertTrue("Upgraded URL must contain q=90", upgradedUrl.contains("q=90"))
        assertTrue("Upgraded URL must not contain thumbnail w=320", !upgradedUrl.contains("w=320"))

        // Blank or non-unsplash URLs should remain intact
        assertEquals("", ImageEngine.upgradePhotoUrlForExport(""))
        val customUrl = "https://cdn.example.com/sacred.jpg"
        assertEquals(customUrl, ImageEngine.upgradePhotoUrlForExport(customUrl))
    }

    @Test
    fun jpegQualityClamping_neverBelow85() {
        // Values below 85 must be clamped to 85
        assertEquals(85, ImageEngine.clampJpegQuality(50))
        assertEquals(85, ImageEngine.clampJpegQuality(84))
        assertEquals(85, ImageEngine.clampJpegQuality(85))

        // Default quality 92 is maintained
        assertEquals(92, ImageEngine.clampJpegQuality(ImageEngine.DEFAULT_JPEG_QUALITY))

        // Higher qualities up to 100 are permitted
        assertEquals(95, ImageEngine.clampJpegQuality(95))
        assertEquals(100, ImageEngine.clampJpegQuality(100))
        assertEquals(100, ImageEngine.clampJpegQuality(120))
    }

    @Test
    fun platformMapping_routesToAppropriateOutputs() {
        assertEquals(
            ImageOutputFormat.STORY,
            ImageOutputFormat.fromSocialPlatform(ShareHelper.SocialPlatform.INSTAGRAM_STORY)
        )
        assertEquals(
            ImageOutputFormat.STORY,
            ImageOutputFormat.fromSocialPlatform(ShareHelper.SocialPlatform.SNAPCHAT)
        )
        assertEquals(
            ImageOutputFormat.SQUARE,
            ImageOutputFormat.fromSocialPlatform(ShareHelper.SocialPlatform.INSTAGRAM_POST)
        )
        assertEquals(
            ImageOutputFormat.PORTRAIT_POST,
            ImageOutputFormat.fromSocialPlatform(ShareHelper.SocialPlatform.X_TWITTER)
        )
        assertEquals(
            ImageOutputFormat.PORTRAIT_POST,
            ImageOutputFormat.fromSocialPlatform(ShareHelper.SocialPlatform.WHATSAPP)
        )
    }
}
