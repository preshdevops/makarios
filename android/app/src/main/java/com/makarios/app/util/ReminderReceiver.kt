package com.makarios.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.Light

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED -> {
                ReminderManager.rescheduleAll(context)
                com.makarios.app.widget.WidgetScheduling.schedule(context)
                com.makarios.app.widget.WidgetScheduling.refreshNow(context)
            }
            ReminderManager.ACTION_MORNING -> {
                val a = ReminderManager.chooseMorningAffirmation(context)
                val light = ReminderManager.deliveryLight()
                ReminderManager.showNotification(context, ReminderManager.ACTION_MORNING, a, light)
                ReminderManager.rescheduleAll(context)
            }
            ReminderManager.ACTION_EVENING -> {
                val a = ReminderManager.chooseMorningAffirmation(context)
                ReminderManager.showNotification(context, ReminderManager.ACTION_EVENING, a, Light.Night)
                ReminderManager.rescheduleAll(context)
            }
            ReminderManager.ACTION_STREAK -> {
                val a = ReminderManager.chooseMorningAffirmation(context)
                val openedToday = context.getSharedPreferences("makarios_activity_log", Context.MODE_PRIVATE).getStringSet("shown", emptySet()).orEmpty().contains(java.time.LocalDate.now().toString())
                if (!openedToday) ReminderManager.showNotification(context, ReminderManager.ACTION_STREAK, a, Light.Night)
                ReminderManager.rescheduleAll(context)
            }
        }
    }
}
