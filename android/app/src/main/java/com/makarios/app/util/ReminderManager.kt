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
 * Full-fidelity scheduler and notification manager for Makarios.
 * Powers daily sacred reminders (Dawn, Midday, Evening) and hourly declarations,
 * supporting targeted delivery from Custom authored, Saved, Spiritual Focus, or Complete Library.
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

    enum class ReminderSource(val id: String, val title: String, val subtitle: String) {
        CUSTOM("custom", "Personal Declarations", "Delivers affirmations you authored"),
        SAVED("saved", "Saved Declarations", "Delivers your bookmarked promises"),
        FOCUS("focus", "Spiritual Focus", "Anchored to your active season"),
        ALL("all", "Complete Library", "Any biblical declaration across themes")
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

    // ── Reminder Content Source ───────────────────────────────────

    fun getReminderSource(context: Context): ReminderSource {
        val key = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("reminder_source", ReminderSource.FOCUS.id) ?: ReminderSource.FOCUS.id
        return ReminderSource.values().find { it.id == key } ?: ReminderSource.FOCUS
    }

    fun setReminderSource(context: Context, source: ReminderSource) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("reminder_source", source.id).apply()
    }

    fun getActiveSeason(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("active_season", "Peace over Anxiety") ?: "Peace over Anxiety"
    }

    fun setActiveSeason(context: Context, season: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("active_season", season).apply()
    }

    fun getUserName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("user_name", "Precious") ?: "Precious"
    }

    fun setUserName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("user_name", name.trim().ifEmpty { "Precious" }).apply()
    }

    /**
     * Resolves the exact affirmation to deliver based on user preference:
     * - CUSTOM: User-authored personal declarations (falls back to saved/featured if none yet)
     * - SAVED: User-bookmarked declarations (falls back to featured if none)
     * - FOCUS: Matched to user's active spiritual focus theme
     * - ALL: Any biblical declaration from the complete library
     */
    fun resolveAffirmationForReminder(context: Context): Affirmation {
        val source = getReminderSource(context)
        val season = getActiveSeason(context)

        return when (source) {
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
            ReminderSource.FOCUS -> {
                val categoryKeyword = when {
                    season.contains("Peace", ignoreCase = true) -> "Peace"
                    season.contains("Confidence", ignoreCase = true) || season.contains("Calling", ignoreCase = true) -> "Confidence"
                    season.contains("Rest", ignoreCase = true) || season.contains("Renewal", ignoreCase = true) -> "Peace"
                    season.contains("Provision", ignoreCase = true) -> "Provision"
                    season.contains("Strength", ignoreCase = true) -> "Strength"
                    season.contains("Joy", ignoreCase = true) -> "Joy"
                    season.contains("Identity", ignoreCase = true) -> "Identity"
                    else -> "Peace"
                }
                val matches = AffirmationRepository.getAll().filter {
                    it.category.equals(categoryKeyword, ignoreCase = true)
                }
                if (matches.isNotEmpty()) {
                    matches.random()
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
     * Instantly triggers a live notification according to the user's chosen reminder pool.
     */
    fun sendTestNotification(context: Context, isHourly: Boolean = false) {
        createNotificationChannel(context)

        val affirmation = resolveAffirmationForReminder(context)
        val source = getReminderSource(context)
        val title = when {
            isHourly -> "Hourly Stillness · Makarios"
            source == ReminderSource.CUSTOM -> "Personal Declaration · Makarios"
            source == ReminderSource.SAVED -> "Saved Promise · Makarios"
            source == ReminderSource.FOCUS -> "Spiritual Focus · Makarios"
            else -> "Sacred Revelation · Makarios"
        }

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
