package com.makarios.app.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.glance.appwidget.updateAll
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.Light
import com.makarios.app.widget.MakariosSmallWidget
import com.makarios.app.widget.MakariosMediumWidget
import com.makarios.app.widget.MakariosLargeWidget
import com.makarios.app.widget.MakariosMediumReceiver
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
        val provider = ComponentName(context, MakariosMediumReceiver::class.java)

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
        val provider = ComponentName(context, MakariosMediumReceiver::class.java)

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
        Toast.makeText(context, "Added to Home Screen widget", Toast.LENGTH_SHORT).show()
    }

    fun shuffleWidget(context: Context): Affirmation {
        val next = AffirmationRepository.shuffleWidgetAffirmation()
        refreshGlanceWidgets(context)
        return next
    }

    fun setLockScreenAffirmation(
        context: Context,
        affirmation: Affirmation,
        light: Light,
        photoUri: android.net.Uri? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val renderer = LightCanvas(context)
                val out = java.io.File(context.cacheDir, "wallpaper_${System.currentTimeMillis()}.png")
                // Determine device resolution
                val metrics = context.resources.displayMetrics
                renderer.render(
                    declaration = affirmation.declaration,
                    verseText = affirmation.scriptureText,
                    verseReference = affirmation.reference,
                    light = light,
                    format = ExportFormat.Wallpaper,
                    deviceWidth = metrics.widthPixels,
                    deviceHeight = metrics.heightPixels,
                    outputFile = out
                )
                
                val bitmap = android.graphics.BitmapFactory.decodeFile(out.absolutePath)
                val wallpaperManager = android.app.WallpaperManager.getInstance(context)
                val success = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, android.app.WallpaperManager.FLAG_LOCK)
                        true
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                        true
                    }
                } catch(e: Exception) { false }
                
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Applied to your Lock Screen", Toast.LENGTH_SHORT).show()
                    }
                    onResult(success)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    private fun refreshGlanceWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                MakariosSmallWidget().updateAll(context)
            MakariosMediumWidget().updateAll(context)
            MakariosLargeWidget().updateAll(context)
            }
        }
    }
}



