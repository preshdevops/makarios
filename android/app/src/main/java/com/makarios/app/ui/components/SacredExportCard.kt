package com.makarios.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.theme.DisplayFontFamily
import com.makarios.app.ui.theme.BodyFontFamily
import com.makarios.app.util.ImageOutputFormat
import com.makarios.app.util.WallpaperRenderer

/**
 * SacredExportCard
 *
 * Dedicated composable crafted specifically for high-resolution offscreen export.
 * Rendered at exact fixed DP sizes (e.g. 360x640 dp under LocalDensity 3.0 = 1080x1920 px).
 * Never captures the visible on-screen card.
 *
 * Specifications:
 * - Bottom-weighted black scrim: transparent at top to ~65% black at bottom
 * - Soft text shadows: 45% black, 6px blur, 2px offset
 * - 8% horizontal safe margins
 * - Top & bottom 14% clearance for Story UI overlays (Instagram/Snapchat bars)
 * - Cormorant Garamond display serif typography with Work Sans grounding
 */
@Composable
fun SacredExportCard(
    affirmation: Affirmation,
    format: ImageOutputFormat,
    photoBitmap: Bitmap?,
    style: WallpaperRenderer.RenderStyle = WallpaperRenderer.STYLES[0],
    modifier: Modifier = Modifier
) {
    val isPhoto = photoBitmap != null

    // Palette & soft shadow
    val primaryTextColor = if (isPhoto) Color.White else Color(style.primaryTextColor)
    val secondaryTextColor = if (isPhoto) Color.White.copy(alpha = 0.92f) else Color(style.secondaryTextColor)
    val accentColor = if (isPhoto) Color(0xFFFBBF24) else Color(style.accentColor)

    val softTextShadow = if (isPhoto) {
        Shadow(
            color = Color.Black.copy(alpha = 0.45f),
            offset = Offset(0f, 2f),
            blurRadius = 6f
        )
    } else null

    // Safe margins: 8% horizontal, 14% top/bottom for Story
    val horizontalMargin = format.targetDpWidth * format.horizontalSafeRatio
    val topMargin = format.targetDpHeight * format.topSafeRatio
    val bottomMargin = format.targetDpHeight * format.bottomSafeRatio

    Box(
        modifier = modifier
            .size(format.targetDpWidth, format.targetDpHeight)
            .background(Color(style.backgroundColors[0]))
    ) {
        // 1. Background layer
        if (photoBitmap != null) {
            Image(
                bitmap = photoBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom-weighted black scrim: transparent at top down to about 65% black
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.00f to Color.Transparent,
                            0.35f to Color.Black.copy(alpha = 0.15f),
                            0.65f to Color.Black.copy(alpha = 0.45f),
                            1.00f to Color.Black.copy(alpha = 0.65f)
                        )
                    )
            )
        } else {
            // Radiant gradient ground
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = style.backgroundColors.map { Color(it) }
                        )
                    )
            )
        }

        // 2. Sacred content hierarchy within strict safe margins
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = horizontalMargin,
                    end = horizontalMargin,
                    top = topMargin,
                    bottom = bottomMargin
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Category badge (tracked all-caps allowed for category tag only)
            Text(
                text = affirmation.category.uppercase(),
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
                letterSpacing = 1.4.sp,
                color = accentColor,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = softTextShadow)
            )

            // Center declaration & scripture block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Declaration
                val declarationFontSize = when (format) {
                    ImageOutputFormat.SQUARE -> 22.sp
                    else -> 25.sp
                }
                Text(
                    text = "“${affirmation.declaration}”",
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = declarationFontSize,
                    lineHeight = (declarationFontSize.value * 1.36f).sp,
                    color = primaryTextColor,
                    textAlign = TextAlign.Center,
                    style = TextStyle(shadow = softTextShadow)
                )

                // Sacred subtle divider
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .background(accentColor.copy(alpha = 0.5f))
                )

                // Grounding Scripture
                if (affirmation.scriptureText.isNotBlank()) {
                    Text(
                        text = "“${affirmation.scriptureText}”",
                        fontFamily = DisplayFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center,
                        style = TextStyle(shadow = softTextShadow)
                    )
                }

                // Reference (Sentence case, bold Work Sans)
                Text(
                    text = "— ${affirmation.reference}",
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = accentColor,
                    textAlign = TextAlign.Center,
                    style = TextStyle(shadow = softTextShadow)
                )
            }

            // Wordmark signature above bottom safe zone
            Text(
                text = "makarios",
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = primaryTextColor.copy(alpha = if (isPhoto) 0.65f else 0.45f),
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = softTextShadow)
            )
        }
    }
}

/**
 * Dedicated offscreen host composable.
 * Renders [SacredExportCard] at the fixed target size (e.g. 360x640 dp @ 3.0 density = 1080x1920 px)
 * in an offscreen 0-size clipped container.
 * Records the layout via [GraphicsLayer] for export to an [android.graphics.Bitmap].
 */
@Composable
fun OffscreenExportHost(
    affirmation: Affirmation,
    format: ImageOutputFormat,
    photoBitmap: Bitmap?,
    style: WallpaperRenderer.RenderStyle,
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(0.dp)
            .clipToBounds()
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(density = format.density, fontScale = 1.0f)
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(format.targetDpWidth, format.targetDpHeight)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                SacredExportCard(
                    affirmation = affirmation,
                    format = format,
                    photoBitmap = photoBitmap,
                    style = style
                )
            }
        }
    }
}
