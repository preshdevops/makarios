package com.makarios.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.makarios.app.ui.theme.Light
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> {
                DailyDeclarationWorker.scheduleAll(context)
                ReminderManager.rescheduleAll(context)
                com.makarios.app.widget.WidgetScheduling.schedule(context)
                com.makarios.app.widget.WidgetScheduling.refreshNow(context)
            }
            ReminderManager.ACTION_MORNING,
            ReminderManager.ACTION_EVENING,
            ReminderManager.ACTION_STREAK -> {
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        // KeptRepository is now lazy — no init() needed.
                        val affirmation = ReminderManager.chooseMorningAffirmation(context)
                        when (intent.action) {
                            ReminderManager.ACTION_MORNING -> ReminderManager.showNotification(context, ReminderManager.ACTION_MORNING, affirmation, ReminderManager.deliveryLight())
                            ReminderManager.ACTION_EVENING -> ReminderManager.showNotification(context, ReminderManager.ACTION_EVENING, affirmation, Light.Night)
                            ReminderManager.ACTION_STREAK -> {
                                val openedToday = context.getSharedPreferences("makarios_activity_log", Context.MODE_PRIVATE)
                                    .getStringSet("shown", emptySet()).orEmpty()
                                    .contains(java.time.LocalDate.now().toString())
                                if (!openedToday) ReminderManager.showNotification(context, ReminderManager.ACTION_STREAK, affirmation, Light.Night)
                            }
                        }
                        ReminderManager.rescheduleAll(context)
                    } finally {
                        pending.finish()
                    }
                }
            }
            DailyDeclarationWorker.ACTION_NEXT -> DailyDeclarationWorker.scheduleAll(context)
        }
    }
}
