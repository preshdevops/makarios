package com.makarios.app.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.random.Random

/**
 * Singleton cache for the grain texture tile.
 * Generates ONE small (128x128) tiled bitmap once, rather than generating noise per render.
 */
object GrainTileProvider {
    val androidBitmap: Bitmap by lazy {
        val b = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(128 * 128)
        val rng = Random(0x4D414B)
        for (i in pixels.indices) {
            val n = rng.nextInt(256)
            pixels[i] = Color.argb(n, n, n, n)
        }
        b.setPixels(pixels, 0, 128, 0, 0, 128, 128)
        b
    }

    val imageBitmap: ImageBitmap by lazy {
        androidBitmap.asImageBitmap()
    }
}
