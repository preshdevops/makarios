package com.makarios.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp

/**
 * One photographic system for the whole app.
 *
 * - [WarmPhotoGrade]: pass as `colorFilter` to every AsyncImage/Image that shows a photo,
 *   so Home, Library, Saved and Create share the same warm, slightly desaturated grade.
 * - [PhotoTextScrim]: bottom-weighted scrim. Text placed in the lower 45% of a photo sits
 *   on at least ~60% black, which keeps white text ≥ 4.5:1 even over a pure-white sky.
 * - [ScrimPill]: solid dark pill for labels/buttons over photos. Replaces frosted/glass pills.
 */

/** Saturation 0.88, gentle red lift, blue pulled down. Offsets are on the 0–255 scale. */
val WarmPhotoGrade: ColorFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            0.960f, 0.091f, 0.009f, 0f, 6f,
            0.026f, 0.966f, 0.009f, 0f, 2f,
            0.023f, 0.077f, 0.800f, 0f, -4f,
            0f,     0f,     0f,     1f, 0f
        )
    )
)

val PhotoTextScrim: Brush = Brush.verticalGradient(
    0.00f to Color.Transparent,
    0.30f to Color.Black.copy(alpha = 0.15f),
    0.55f to Color.Black.copy(alpha = 0.60f),
    1.00f to Color.Black.copy(alpha = 0.78f)
)

/** Solid (no blur) scrim colour. 60% black keeps white text ≥ 4.5:1 over any photo. */
val ScrimPillColor: Color = Color.Black.copy(alpha = 0.60f)

@Composable
fun ScrimPill(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ScrimPillColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        content()
    }
}
