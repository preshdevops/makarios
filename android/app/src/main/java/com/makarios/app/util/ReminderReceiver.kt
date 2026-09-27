package com.makarios.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * ReminderReceiver
 *
 * Broadcast receiver triggered by AlarmManager for Daily and Hourly declarations,
 * as well as upon device boot to reschedule active timers.
 * Now dynamically resolves declarations from the user's chosen reminder pool
 * (Custom authored, Saved, Spiritual Focus, or Complete Library).
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Reschedule all active reminders on system boot
                ReminderManager.rescheduleAll(context)
            }

            ReminderManager.ACTION_DAWN -> {
                val affirmation = ReminderManager.resolveAffirmationForReminder(context)

                ReminderManager.showSacredNotification(
                    context = context,
                    notificationId = ReminderManager.REQ_DAWN,
                    title = "Dawn Revelation · Makarios",
                    declaration = affirmation.declaration,
                    scripture = affirmation.scriptureText,
                    reference = affirmation.reference
                )
                // Schedule for tomorrow
                ReminderManager.scheduleDaily(context, ReminderManager.ReminderType.DAWN)
            }

            ReminderManager.ACTION_MIDDAY -> {
                val affirmation = ReminderManager.resolveAffirmationForReminder(context)

                ReminderManager.showSacredNotification(
                    context = context,
                    notificationId = ReminderManager.REQ_MIDDAY,
                    title = "Midday Stillness · Makarios",
                    declaration = affirmation.declaration,
                    scripture = affirmation.scriptureText,
                    reference = affirmation.reference
                )
                // Schedule for tomorrow
                ReminderManager.scheduleDaily(context, ReminderManager.ReminderType.MIDDAY)
            }

            ReminderManager.ACTION_EVENING -> {
                val affirmation = ReminderManager.resolveAffirmationForReminder(context)

                ReminderManager.showSacredNotification(
                    context = context,
                    notificationId = ReminderManager.REQ_EVENING,
                    title = "Evening Examen · Makarios",
                    declaration = affirmation.declaration,
                    scripture = affirmation.scriptureText,
                    reference = affirmation.reference
                )
                // Schedule for tomorrow
                ReminderManager.scheduleDaily(context, ReminderManager.ReminderType.EVENING)
            }

            ReminderManager.ACTION_HOURLY -> {
                if (ReminderManager.isHourlyEnabled(context)) {
                    val affirmation = ReminderManager.resolveAffirmationForReminder(context)

                    ReminderManager.showSacredNotification(
                        context = context,
                        notificationId = ReminderManager.REQ_HOURLY,
                        title = "Hourly Stillness · Makarios",
                        declaration = affirmation.declaration,
                        scripture = affirmation.scriptureText,
                        reference = affirmation.reference
                    )
                    // Schedule next hour
                    ReminderManager.scheduleHourly(context)
                }
            }
        }
    }
}
