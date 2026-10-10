package com.makarios.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.theme.Light
import com.makarios.app.ui.theme.NewsreaderFontFamily

/**
 * Makarios Logo Mark:
 * An open doorway (arch) with a half sun sitting on the ground line.
 * One shape, three parts: arch stroke, ground line, half sun.
 *
 * ViewBox: 100 x 100
 * Arch: M27 82V46a23 23 0 0 1 46 0v36, stroke 4.5, round caps/joins.
 * Ground: M18 82h64, stroke 4.5, round caps.
 * Sun: M32 82a18 18 0 0 1 36 0Z, filled.
 */
@Composable
fun MakariosMark(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    archColor: Color = Color(0xFF2A1B14),
    groundColor: Color = Color(0xFF2A1B14),
    sunColor: Color = Color(0xFFB5532B)
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension / 100f
        // Thicken strokes below 40px (roughly 30dp)
        val strokeWidth = if (this.size.minDimension < 40f) 6f * s else 4.5f * s

        // 1. Sun: M32 82 a18 18 0 0 1 36 0 Z (half sun)
        val sunPath = Path().apply {
            moveTo(32f * s, 82f * s)
            // Arc of radius 18 from (32, 82) to (68, 82)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = 32f * s,
                    top = 64f * s,
                    right = 68f * s,
                    bottom = 100f * s
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(sunPath, color = sunColor, style = Fill)

        // 2. Arch: M27 82 V46 a23 23 0 0 1 46 0 v36
        val archPath = Path().apply {
            moveTo(27f * s, 82f * s)
            lineTo(27f * s, 46f * s)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = 27f * s,
                    top = 23f * s,
                    right = 73f * s,
                    bottom = 69f * s
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(73f * s, 82f * s)
        }
        drawPath(
            archPath,
            color = archColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Ground line: M18 82 h64
        val groundPath = Path().apply {
            moveTo(18f * s, 82f * s)
            lineTo(82f * s, 82f * s)
        }
        drawPath(
            groundPath,
            color = groundColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Wordmark: The mark at 22dp plus "makarios" in Newsreader Italic 500, letter-spacing -0.01em.
 */
@Composable
fun MakariosWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 22.dp,
    light: Light? = null,
    textColor: Color = Color.Unspecified
) {
    val isDark = light?.isDark ?: false
    val effectiveTextColor = if (textColor != Color.Unspecified) {
        textColor
    } else if (light != null) {
        light.text
    } else {
        Color(0xFF2A1B14)
    }

    val archColor = if (isDark) Color(0xFFFFF4E4) else Color(0xFF2A1B14)
    val sunColor = if (isDark) Color(0xFFE8A860) else Color(0xFFB5532B)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MakariosMark(
            size = markSize,
            archColor = if (textColor != Color.Unspecified) textColor else archColor,
            groundColor = if (textColor != Color.Unspecified) textColor else archColor,
            sunColor = if (textColor != Color.Unspecified) textColor.copy(alpha = 0.85f) else sunColor
        )
        Text(
            text = "makarios",
            fontFamily = NewsreaderFontFamily,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            fontSize = 22.sp,
            letterSpacing = (-0.01).sp,
            color = effectiveTextColor
        )
    }
}
