package com.makarios.app.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.util.Log

enum class WallpaperScreenTarget {
    LOCK,
    HOME,
    BOTH
}

object WallpaperHelper {
    private const val TAG = "WallpaperHelper"

    @Volatile
    private var lastSetTarget: WallpaperScreenTarget? = null

    data class WallpaperMetrics(
        val renderWidth: Int,
        val renderHeight: Int,
        val displayWidth: Int,
        val displayHeight: Int,
        val isWiderCanvas: Boolean
    )

    fun getMetrics(context: Context, target: WallpaperScreenTarget): WallpaperMetrics {
        val wm = WallpaperManager.getInstance(context)
        val displayMetrics = context.resources.displayMetrics
        val dispW = displayMetrics.widthPixels
        val dispH = displayMetrics.heightPixels

        // Prompt 21 Part 7: ALWAYS exact display size for lock and home (no wider scrolling canvas)
        return WallpaperMetrics(dispW, dispH, dispW, dispH, false)
    }

    /**
     * Centralized wallpaper setting function.
     * Sets bitmap with an explicit non-null crop hint Rect(0, 0, width, height).
     */
    fun applyWallpaper(
        context: Context,
        bitmap: Bitmap,
        target: WallpaperScreenTarget
    ): Result<String> {
        return runCatching {
            val wm = WallpaperManager.getInstance(context)
            val cropHint = Rect(0, 0, bitmap.width, bitmap.height)
            wm.suggestDesiredDimensions(bitmap.width, bitmap.height)
            Log.d(TAG, "wallpaper render ${bitmap.width}x${bitmap.height} set ${bitmap.width}x${bitmap.height}")
            val flags = when (target) {
                WallpaperScreenTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperScreenTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperScreenTarget.BOTH -> WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bitmap, cropHint, true, flags)
            } else {
                wm.setBitmap(bitmap)
            }

            lastSetTarget = target
            when (target) {
                WallpaperScreenTarget.LOCK -> "Set on lock screen"
                WallpaperScreenTarget.HOME -> "Set on home screen"
                WallpaperScreenTarget.BOTH -> "Set on lock and home screens"
            }
        }.onFailure {
            Log.e(TAG, "Failed to apply wallpaper: ${it.message}", it)
        }
    }

    /**
     * Clears or restores previous wallpaper if possible.
     */
    fun clearOrResetWallpaper(context: Context): Boolean {
        return runCatching {
            val wm = WallpaperManager.getInstance(context)
            wm.clear()
            true
        }.getOrDefault(false)
    }
}
