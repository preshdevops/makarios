package com.makarios.app.ui.theme

import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.imageResource
import com.makarios.app.R

fun Modifier.lightBackground(light: Light): Modifier = composed {
    val grainImage = ImageBitmap.imageResource(id = R.drawable.grain_tile)
    
    this.then(
        Modifier.drawBehind {
            // 1. Draw the gradient
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(light.top, light.bottom),
                startY = 0f,
                endY = size.height
            )
            drawRect(brush = gradientBrush, size = size)
            
            // 2. Draw the grain tile
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    shader = ImageShader(
                        image = grainImage,
                        tileModeX = TileMode.Repeated,
                        tileModeY = TileMode.Repeated
                    )
                }
                canvas.drawRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    paint = paint
                )
            }
        }
    )
}
