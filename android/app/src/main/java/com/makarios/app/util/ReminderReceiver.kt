package com.makarios.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.makarios.app.data.AffirmationRepository

/**
 * ReminderReceiver
 *
 * Broadcast receiver triggered by AlarmManager for Daily and Hourly declarations,
 * as well as upon device boot to reschedule active timers.
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
                val affirmation = AffirmationRepository.curatedAffirmations
                    .find { it.category.equals("Peace", ignoreCase = true) || it.category.equals("Identity", ignoreCase = true) }
                    ?: AffirmationRepository.featuredAffirmation

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
                val affirmation = AffirmationRepository.curatedAffirmations
                    .find { it.category.equals("Strength", ignoreCase = true) || it.category.equals("Courage", ignoreCase = true) }
                    ?: AffirmationRepository.featuredAffirmation

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
                val affirmation = AffirmationRepository.curatedAffirmations
                    .find { it.category.equals("Peace", ignoreCase = true) || it.category.equals("Joy", ignoreCase = true) }
                    ?: AffirmationRepository.featuredAffirmation

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
                    // Pick rotating affirmation
                    val all = AffirmationRepository.curatedAffirmations
                    val index = (System.currentTimeMillis() / (1000 * 60 * 60) % all.size).toInt().coerceIn(0, all.size - 1)
                    val affirmation = all.getOrElse(index) { AffirmationRepository.featuredAffirmation }

                    ReminderManager.showSacredNotification(
                        context = context,
                        notificationId = ReminderManager.REQ_HOURLY,
                        title = "Hourly Truth · Makarios",
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
