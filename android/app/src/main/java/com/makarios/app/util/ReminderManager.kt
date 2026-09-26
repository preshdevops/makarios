package com.makarios.app.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.makarios.app.MainActivity
import com.makarios.app.R
import com.makarios.app.data.AffirmationRepository
import java.util.Calendar

/**
 * ReminderManager
 *
 * Full-fidelity scheduler and notification manager for Makarios.
 * Powers daily sacred reminders (Dawn, Midday, Evening) and hourly declarations.
 */
object ReminderManager {

    const val CHANNEL_ID = "makarios_sacred_reminders"
    const val CHANNEL_NAME = "Sacred Declarations & Truth"
    private const val PREFS_NAME = "makarios_reminders_prefs"

    const val ACTION_DAWN = "com.makarios.app.ACTION_DAWN_REMINDER"
    const val ACTION_MIDDAY = "com.makarios.app.ACTION_MIDDAY_REMINDER"
    const val ACTION_EVENING = "com.makarios.app.ACTION_EVENING_REMINDER"
    const val ACTION_HOURLY = "com.makarios.app.ACTION_HOURLY_REMINDER"

    const val REQ_DAWN = 1001
    const val REQ_MIDDAY = 1002
    const val REQ_EVENING = 1003
    const val REQ_HOURLY = 1004

    enum class ReminderType(val action: String, val requestCode: Int, val hour: Int, val minute: Int) {
        DAWN(ACTION_DAWN, REQ_DAWN, 6, 30),
        MIDDAY(ACTION_MIDDAY, REQ_MIDDAY, 12, 30),
        EVENING(ACTION_EVENING, REQ_EVENING, 20, 30),
        HOURLY(ACTION_HOURLY, REQ_HOURLY, -1, -1)
    }

    /**
     * Initializes notification channels and reschedules active reminders.
     */
    fun init(context: Context) {
        createNotificationChannel(context)
        rescheduleAll(context)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Quiet reminders to anchor your spirit in God's promises"
                enableLights(true)
                lightColor = 0xFFD97706.toInt()
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // ── Preference Accessors ──────────────────────────────────────

    fun isDawnEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("dawn_enabled", true)

    fun isMiddayEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("midday_enabled", false)

    fun isEveningEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("evening_enabled", true)

    fun isHourlyEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("hourly_enabled", false)

    fun setDawnEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("dawn_enabled", enabled).apply()
        if (enabled) scheduleDaily(context, ReminderType.DAWN) else cancelReminder(context, ReminderType.DAWN)
    }

    fun setMiddayEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("midday_enabled", enabled).apply()
        if (enabled) scheduleDaily(context, ReminderType.MIDDAY) else cancelReminder(context, ReminderType.MIDDAY)
    }

    fun setEveningEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("evening_enabled", enabled).apply()
        if (enabled) scheduleDaily(context, ReminderType.EVENING) else cancelReminder(context, ReminderType.EVENING)
    }

    fun setHourlyEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("hourly_enabled", enabled).apply()
        if (enabled) scheduleHourly(context) else cancelReminder(context, ReminderType.HOURLY)
    }

    // ── Scheduling Engine ─────────────────────────────────────────

    fun scheduleDaily(context: Context, type: ReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, type.hour)
            set(Calendar.MINUTE, type.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = type.action
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, type.requestCode, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
        }
    }

    fun scheduleHourly(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_HOURLY
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQ_HOURLY, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, type: ReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = type.action
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, type.requestCode, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAll(context: Context) {
        if (isDawnEnabled(context)) scheduleDaily(context, ReminderType.DAWN)
        if (isMiddayEnabled(context)) scheduleDaily(context, ReminderType.MIDDAY)
        if (isEveningEnabled(context)) scheduleDaily(context, ReminderType.EVENING)
        if (isHourlyEnabled(context)) scheduleHourly(context)
    }

    /**
     * Instantly triggers a live notification so the user can verify notifications working on their phone.
     */
    fun sendTestNotification(context: Context, isHourly: Boolean = false) {
        createNotificationChannel(context)

        val affirmation = AffirmationRepository.featuredAffirmation
        val title = if (isHourly) "Hourly Truth · Makarios" else "Sacred Revelation · Makarios"

        showSacredNotification(
            context = context,
            notificationId = if (isHourly) 2002 else 1001,
            title = title,
            declaration = affirmation.declaration,
            scripture = affirmation.scriptureText,
            reference = affirmation.reference
        )
    }

    fun showSacredNotification(
        context: Context,
        notificationId: Int,
        title: String,
        declaration: String,
        scripture: String,
        reference: String
    ) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, notificationId, openAppIntent, flags)

        val bigText = "“$declaration”\n\n“$scripture”\n— $reference"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("“$declaration”")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFFD97706.toInt()) // Sacred amber gold
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
