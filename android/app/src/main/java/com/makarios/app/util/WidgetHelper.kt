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

object WidgetHelper {

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

    fun setWidgetAffirmation(context: Context, affirmation: Affirmation) {
        AffirmationRepository.setWidgetAffirmation(affirmation)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                MakariosGlanceWidget().updateAll(context)
            }
        }
        Toast.makeText(context, "Added to Home Screen widget", Toast.LENGTH_SHORT).show()
    }
}
