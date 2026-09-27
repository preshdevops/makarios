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
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import java.util.Calendar

/**
 * ReminderManager
 *
 * Scheduler and notification manager for Makarios.
 * Handles daily declaration reminders and lets users set any specific affirmation
 * as their active notification, or choose a delivery pool (Pinned, Custom, Saved, All).
 */
object ReminderManager {

    const val CHANNEL_ID = "makarios_reminders"
    const val CHANNEL_NAME = "Daily Declarations"
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
        DAWN(ACTION_DAWN, REQ_DAWN, 8, 30),
        MIDDAY(ACTION_MIDDAY, REQ_MIDDAY, 12, 30),
        EVENING(ACTION_EVENING, REQ_EVENING, 20, 30),
        HOURLY(ACTION_HOURLY, REQ_HOURLY, -1, -1)
    }

    enum class ReminderSource(val id: String, val title: String, val subtitle: String) {
        CUSTOM("custom", "My Created Declarations", "Affirmations you personally authored in Create Studio"),
        SAVED("saved", "My Saved Favorites", "Your bookmarked collection of biblical promises"),
        ALL("all", "Daily Scripture Discovery", "A fresh biblical declaration rotated from the full library each day"),
        PINNED("pinned", "Specific Pinned Declaration", "Keep meditating on a single declaration of your choice")
    }

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
                description = "Daily declaration reminders from Makarios"
                enableLights(true)
                lightColor = 0xFFD97706.toInt()
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // ── Preference Accessors ──────────────────────────────────────

    fun isDailyReminderEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("daily_reminder_enabled", true)

    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("daily_reminder_enabled", enabled).apply()
        if (enabled) {
            scheduleDaily(context, ReminderType.DAWN)
        } else {
            cancelReminder(context, ReminderType.DAWN)
            cancelReminder(context, ReminderType.MIDDAY)
            cancelReminder(context, ReminderType.EVENING)
            cancelReminder(context, ReminderType.HOURLY)
        }
    }

    fun getReminderHour(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("reminder_hour", 8)

    fun getReminderMinute(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("reminder_minute", 30)

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt("reminder_hour", hour)
            .putInt("reminder_minute", minute)
            .apply()
        if (isDailyReminderEnabled(context)) {
            scheduleDaily(context, ReminderType.DAWN, customHour = hour, customMinute = minute)
        }
    }

    fun getFormattedReminderTime(context: Context): String {
        val hour = getReminderHour(context)
        val minute = getReminderMinute(context)
        return formatTime(hour, minute)
    }

    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val displayMinute = if (minute < 10) "0$minute" else "$minute"
        return "$displayHour:$displayMinute $amPm"
    }

    fun isDawnEnabled(context: Context): Boolean = isDailyReminderEnabled(context)
    fun isMiddayEnabled(context: Context): Boolean = false
    fun isEveningEnabled(context: Context): Boolean = false
    fun isHourlyEnabled(context: Context): Boolean = false

    fun setDawnEnabled(context: Context, enabled: Boolean) {
        setDailyReminderEnabled(context, enabled)
    }

    // ── Specific / Pinned Affirmation Delivery ────────────────────

    fun setPinnedAffirmation(context: Context, affirmation: Affirmation) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("pinned_affirmation_id", affirmation.id)
            .putString("reminder_source", ReminderSource.PINNED.id)
            .putBoolean("daily_reminder_enabled", true)
            .apply()

        scheduleDaily(context, ReminderType.DAWN)

        showSacredNotification(
            context = context,
            notificationId = 1001,
            title = "Your declaration · Makarios",
            declaration = affirmation.declaration,
            scripture = affirmation.scriptureText,
            reference = affirmation.reference
        )
    }

    /**
     * Schedules a custom reminder specifically for an affirmation (e.g. newly created declaration).
     * Saves the affirmation, configures the requested time, enables daily reminders, and schedules the alarm.
     */
    fun scheduleAffirmationReminder(
        context: Context,
        affirmation: Affirmation,
        hour: Int? = null,
        minute: Int? = null
    ) {
        if (hour != null && minute != null) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putInt("reminder_hour", hour)
                .putInt("reminder_minute", minute)
                .putString("pinned_affirmation_id", affirmation.id)
                .putString("reminder_source", ReminderSource.PINNED.id)
                .putBoolean("daily_reminder_enabled", true)
                .apply()
            scheduleDaily(context, ReminderType.DAWN, customHour = hour, customMinute = minute)
        } else {
            setPinnedAffirmation(context, affirmation)
        }

        val formattedTime = if (hour != null && minute != null) formatTime(hour, minute) else getFormattedReminderTime(context)
        showSacredNotification(
            context = context,
            notificationId = 1001,
            title = "Daily declaration set for $formattedTime",
            declaration = affirmation.declaration,
            scripture = affirmation.scriptureText,
            reference = affirmation.reference
        )
    }

    fun getPinnedAffirmation(context: Context): Affirmation? {
        val id = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("pinned_affirmation_id", null) ?: return null
        return AffirmationRepository.getById(id)
    }

    // ── Reminder Content Source ───────────────────────────────────

    fun getReminderSource(context: Context): ReminderSource {
        val key = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("reminder_source", ReminderSource.ALL.id) ?: ReminderSource.ALL.id
        return ReminderSource.values().find { it.id == key } ?: ReminderSource.ALL
    }

    fun setReminderSource(context: Context, source: ReminderSource) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("reminder_source", source.id).apply()
    }

    fun getUserName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("user_name", "Friend") ?: "Friend"
    }

    fun setUserName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("user_name", name.trim().ifEmpty { "Friend" }).apply()
    }

    /**
     * Resolves the exact affirmation to deliver based on user preference:
     * - PINNED: Specific declaration chosen by user
     * - CUSTOM: User-authored personal declarations
     * - SAVED: User-bookmarked declarations
     * - ALL: Any biblical declaration from the complete library
     */
    fun resolveAffirmationForReminder(context: Context): Affirmation {
        val source = getReminderSource(context)

        return when (source) {
            ReminderSource.PINNED -> {
                getPinnedAffirmation(context) ?: AffirmationRepository.affirmationOfTheDay
            }
            ReminderSource.CUSTOM -> {
                val personal = AffirmationRepository.personalAffirmations
                if (personal.isNotEmpty()) {
                    personal.random()
                } else {
                    val saved = AffirmationRepository.getSaved()
                    if (saved.isNotEmpty()) saved.random() else AffirmationRepository.affirmationOfTheDay
                }
            }
            ReminderSource.SAVED -> {
                val saved = AffirmationRepository.getSaved()
                if (saved.isNotEmpty()) {
                    saved.random()
                } else {
                    AffirmationRepository.affirmationOfTheDay
                }
            }
            ReminderSource.ALL -> {
                AffirmationRepository.getAll().randomOrNull() ?: AffirmationRepository.affirmationOfTheDay
            }
        }
    }

    // ── Scheduling Engine ─────────────────────────────────────────

    fun scheduleDaily(
        context: Context,
        type: ReminderType = ReminderType.DAWN,
        customHour: Int? = null,
        customMinute: Int? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val hour = customHour ?: if (type == ReminderType.DAWN) getReminderHour(context) else type.hour
        val minute = customMinute ?: if (type == ReminderType.DAWN) getReminderMinute(context) else type.minute

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
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
        scheduleDaily(context, ReminderType.DAWN)
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
        if (isDailyReminderEnabled(context)) {
            scheduleDaily(context, ReminderType.DAWN)
        }
    }

    /**
     * Instantly triggers a notification preview according to user's selected source or pinned affirmation.
     */
    fun sendTestNotification(context: Context, isHourly: Boolean = false) {
        createNotificationChannel(context)

        val affirmation = resolveAffirmationForReminder(context)
        val source = getReminderSource(context)
        val title = when (source) {
            ReminderSource.PINNED -> "Your declaration · Makarios"
            ReminderSource.CUSTOM -> "Your declaration · Makarios"
            ReminderSource.SAVED -> "Your declaration · Makarios"
            else -> "Makarios"
        }

        showSacredNotification(
            context = context,
            notificationId = 1001,
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
