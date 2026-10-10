package com.makarios.app.ui.wallpaper

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize
import com.makarios.app.ui.theme.*

/**
 * Backward-compatible bridge to the unified Style system.
 * The 12 styles produce wallpapers (device resolution) and share images (Story, Square, Portrait, X).
 */
fun renderWallpaper(context: Context, spec: WallpaperSpec, widthPx: Int, heightPx: Int): Bitmap {
    StyleTypefaces.init(context)
    val style = spec.style
    return style.render(spec.asStyleSpec(), spec.light, IntSize(widthPx, heightPx))
}

fun renderStyleBitmap(context: Context, style: Style, light: Light, spec: StyleSpec, widthPx: Int, heightPx: Int): Bitmap {
    StyleTypefaces.init(context)
    return style.render(spec, light, IntSize(widthPx, heightPx))
}
