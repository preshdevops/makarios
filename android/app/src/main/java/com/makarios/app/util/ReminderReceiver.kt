package com.makarios.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * ReminderReceiver
 *
 * Broadcast receiver triggered by AlarmManager for daily declaration reminders,
 * as well as upon device boot to reschedule active timers.
 * Dynamically resolves declarations from the user's chosen reminder pool
 * (Pinned, Custom authored, Saved, or Complete Library).
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
                    title = "Good morning · Makarios",
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
                    title = "Your midday declaration · Makarios",
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
                    title = "Your evening declaration · Makarios",
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
                        title = "Declaration reminder · Makarios",
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
