package com.makarios.app.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.glance.appwidget.updateAll
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.widget.MakariosGlanceWidget
import com.makarios.app.widget.MakariosGlanceWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object WidgetHelper {

    private const val PREFS_NAME = "makarios_widget_prefs"
    private const val KEY_WIDGET_THEME = "widget_theme"

    fun getWidgetTheme(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WIDGET_THEME, "alabaster") ?: "alabaster"
    }

    fun setWidgetTheme(context: Context, theme: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_WIDGET_THEME, theme)
            .apply()
        refreshGlanceWidgets(context)
    }

    fun pinWidgetToHomeScreen(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, MakariosGlanceWidgetReceiver::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
        } else {
            Toast.makeText(
                context,
                "To add: Long-press your home screen, tap 'Widgets', and select Makarios.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun pinWidgetToLockScreen(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, MakariosGlanceWidgetReceiver::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
            Toast.makeText(
                context,
                "Widget pinned! Customize your lock screen to display Makarios.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "To add to lock screen: Customize your lock screen in device settings and choose Makarios.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setWidgetAffirmation(context: Context, affirmation: Affirmation) {
        AffirmationRepository.updateActiveWidgetAffirmation(affirmation)
        refreshGlanceWidgets(context)
        Toast.makeText(context, "Added to Home Screen widget ✓", Toast.LENGTH_SHORT).show()
    }

    /**
     * Renders full-resolution lockscreen artwork with safe clearance and sets it as the lock screen wallpaper.
     * Guarantees a breathtaking typographic lock screen experience on all Android devices.
     */
    fun setLockScreenAffirmation(
        context: Context,
        affirmation: Affirmation,
        styleIndex: Int = 4,
        photoUrl: String? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val photoBmp = photoUrl?.let { WallpaperRenderer.fetchBitmapFromUrl(context, it) }
                val bitmap = WallpaperRenderer.renderBitmap(
                    context = context,
                    declaration = affirmation.declaration,
                    scripture = affirmation.scriptureText,
                    reference = affirmation.reference,
                    category = affirmation.category,
                    style = WallpaperRenderer.getStyle(styleIndex),
                    format = WallpaperRenderer.OutputFormat.WALLPAPER,
                    photoBitmap = photoBmp
                )
                val success = WallpaperRenderer.setAsSystemWallpaper(
                    context = context,
                    bitmap = bitmap,
                    target = WallpaperRenderer.WallpaperTarget.LOCK_SCREEN
                )
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Applied to your Lock Screen ✓", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Couldn't set lock screen directly", Toast.LENGTH_SHORT).show()
                    }
                    onResult(success)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error setting lock screen: ${e.message}", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }
            }
        }
    }

    private fun refreshGlanceWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                MakariosGlanceWidget().updateAll(context)
            }
        }
    }
}
