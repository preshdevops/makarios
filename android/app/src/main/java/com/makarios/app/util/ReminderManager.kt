package com.makarios.app.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.makarios.app.MainActivity
import com.makarios.app.R
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.Light
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Calendar

object ReminderManager {
    const val MORNING_CHANNEL = "morning_declaration"
    const val EVENING_CHANNEL = "evening_verse"
    const val STREAK_CHANNEL = "streak_reminder"
    const val MORNING_ID = 4101
    const val EVENING_ID = 4102
    const val STREAK_ID = 4103
    const val ACTION_MORNING = "com.makarios.app.NOTIFY_MORNING"
    const val ACTION_EVENING = "com.makarios.app.NOTIFY_EVENING"
    const val ACTION_STREAK = "com.makarios.app.NOTIFY_STREAK"
    const val ACTION_KEEP = "com.makarios.app.NOTIFY_KEEP"
    const val ACTION_SHARE = "com.makarios.app.NOTIFY_SHARE"
    const val EXTRA_ID = "affirmation_id"
    const val EXTRA_LIGHT = "light"
    const val REQ_MORNING = 4101
    const val REQ_EVENING = 4102
    const val REQ_STREAK = 4103
    private const val LEGACY_PREFS = "makarios_reminders_prefs"

    enum class ReminderType(val action: String, val requestCode: Int, val hour: Int, val minute: Int) { DAWN(ACTION_MORNING, REQ_MORNING, 6, 30), MIDDAY(ACTION_MORNING, REQ_MORNING, 12, 0), EVENING(ACTION_EVENING, REQ_EVENING, 21, 30), HOURLY(ACTION_MORNING, REQ_MORNING, 6, 30) }
    enum class ReminderSource(val id: String, val title: String, val subtitle: String) { CUSTOM("custom", "My Created Declarations", ""), SAVED("saved", "Kept declarations", ""), ALL("all", "Daily declaration", ""), PINNED("pinned", "Selected declaration", "") }

    fun init(context: Context) { createNotificationChannels(context); rescheduleAll(context) }
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(MORNING_CHANNEL, "Morning declaration", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(EVENING_CHANNEL, "Evening verse", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(STREAK_CHANNEL, "Streak reminder", NotificationManager.IMPORTANCE_DEFAULT)
        ).forEach { channel -> channel.setSound(null, null); channel.enableVibration(false); channel.enableLights(false); manager.createNotificationChannel(channel) }
    }

    fun isDailyReminderEnabled(c: Context) = NotificationStore.morningEnabled(c)
    fun setDailyReminderEnabled(c: Context, enabled: Boolean) { NotificationStore.setMorningEnabled(c, enabled); rescheduleAll(c) }
    fun getReminderHour(c: Context) = NotificationStore.morningTime(c).first
    fun getReminderMinute(c: Context) = NotificationStore.morningTime(c).second
    fun setReminderTime(c: Context, hour: Int, minute: Int) { NotificationStore.setMorningTime(c, hour, minute); rescheduleAll(c) }
    fun getFormattedReminderTime(c: Context) = formatTime(getReminderHour(c), getReminderMinute(c))
    fun formatTime(hour: Int, minute: Int): String { val ap = if (hour < 12) "AM" else "PM"; val h = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }; return "$h:${minute.toString().padStart(2, '0')} $ap" }
    fun isDawnEnabled(c: Context) = NotificationStore.morningEnabled(c)
    fun isMiddayEnabled(c: Context) = false
    fun isEveningEnabled(c: Context) = NotificationStore.eveningEnabled(c)
    fun isHourlyEnabled(c: Context) = false
    fun setDawnEnabled(c: Context, enabled: Boolean) = setDailyReminderEnabled(c, enabled)
    fun setEveningEnabled(c: Context, enabled: Boolean) { NotificationStore.setEveningEnabled(c, enabled); rescheduleAll(c) }
    fun setStreakEnabled(c: Context, enabled: Boolean) { NotificationStore.setStreakEnabled(c, enabled); rescheduleAll(c) }
    fun setSilent(c: Context, silent: Boolean) = NotificationStore.setSilent(c, silent)
    fun setWeekdays(c: Context, days: Set<DayOfWeek>) { NotificationStore.setWeekdays(c, days); rescheduleAll(c) }
    fun getWeekdays(c: Context) = NotificationStore.weekdays(c)

    fun chooseMorningAffirmation(context: Context): Affirmation {
        val recent = NotificationStore.recentIds(context)
        val kept = AffirmationRepository.getSaved().filterNot { it.id in recent }
        val pool = if (kept.isNotEmpty()) kept else AffirmationRepository.getAll().filterNot { it.id in recent }
        return (pool.ifEmpty { AffirmationRepository.getAll() }).firstOrNull() ?: AffirmationRepository.affirmationOfTheDay
    }
    fun deliveryLight(): Light = Light.forNow()
    fun resolveAffirmationForReminder(c: Context) = chooseMorningAffirmation(c)

    fun rescheduleAll(context: Context) {
        cancel(context, REQ_MORNING); cancel(context, REQ_EVENING); cancel(context, REQ_STREAK)
        if (NotificationStore.morningEnabled(context)) schedule(context, ACTION_MORNING, REQ_MORNING, NotificationStore.morningTime(context).first, NotificationStore.morningTime(context).second)
        if (NotificationStore.eveningEnabled(context)) schedule(context, ACTION_EVENING, REQ_EVENING, NotificationStore.eveningTime(context).first, NotificationStore.eveningTime(context).second)
        if (NotificationStore.streakEnabled(context)) schedule(context, ACTION_STREAK, REQ_STREAK, 20, 0)
        val morning = NotificationStore.morningTime(context)
        val renderAt = nextDateTime(context, morning.first, morning.second).minusHours(1)
        val rawDelay = Duration.between(ZonedDateTime.now(), renderAt)
        val delay = if (rawDelay.isNegative) Duration.ZERO else rawDelay
        WorkManager.getInstance(context).enqueueUniqueWork("notification-artwork", ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<NotificationArtworkWorker>().setInitialDelay(delay.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS).build())
    }
    fun scheduleDaily(c: Context, type: ReminderType = ReminderType.DAWN, customHour: Int? = null, customMinute: Int? = null) = rescheduleAll(c)
    fun scheduleHourly(c: Context) = rescheduleAll(c)
    fun cancelReminder(c: Context, type: ReminderType) = cancel(c, type.requestCode)

    private fun schedule(context: Context, action: String, requestCode: Int, hour: Int, minute: Int) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val target = nextDateTime(context, hour, minute)
        val intent = Intent(context, ReminderReceiver::class.java).setAction(action)
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarm.canScheduleExactAlarms()) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.toInstant().toEpochMilli(), pi)
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.toInstant().toEpochMilli(), pi)
            else alarm.set(AlarmManager.RTC_WAKEUP, target.toInstant().toEpochMilli(), pi)
        } catch (_: SecurityException) { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.toInstant().toEpochMilli(), pi) }
    }
    private fun nextDateTime(c: Context, hour: Int, minute: Int): ZonedDateTime {
        var next = ZonedDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(ZonedDateTime.now())) next = next.plusDays(1)
        val allowed = NotificationStore.weekdays(c)
        while (next.dayOfWeek !in allowed) next = next.plusDays(1)
        return next
    }
    private fun cancel(context: Context, requestCode: Int) { val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager; val pi = PendingIntent.getBroadcast(context, requestCode, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); alarm.cancel(pi) }

    fun showNotification(context: Context, kind: String, affirmation: Affirmation, light: Light, kept: Boolean = false) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        createNotificationChannels(context)
        val id = when (kind) { ACTION_EVENING -> EVENING_ID; ACTION_STREAK -> STREAK_ID; else -> MORNING_ID }
        val channel = when (kind) { ACTION_EVENING -> EVENING_CHANNEL; ACTION_STREAK -> STREAK_CHANNEL; else -> MORNING_CHANNEL }
        val title = when (kind) { ACTION_EVENING -> "Before you sleep"; ACTION_STREAK -> "${ProfileActivityLogCompat.streak(context)} days of declaring"; else -> when (light) { Light.Dawn, Light.Midday -> "Good morning"; Light.Dusk -> "Good evening"; else -> "Good afternoon" } }
        val text = if (kind == ACTION_STREAK) "Today's declaration is still waiting." else affirmation.declaration
        val content = PendingIntent.getActivity(context, id, Intent(context, MainActivity::class.java).apply { data = Uri.parse("makarios://today/${affirmation.id}"); putExtra(EXTRA_ID, affirmation.id); flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, channel).setSmallIcon(R.drawable.ic_notification).setColor(0xFF8A5A14.toInt()).setContentTitle(if (kept) "Kept" else title).setContentText(text).setContentIntent(content).setAutoCancel(true).setOnlyAlertOnce(true).setSilent(NotificationStore.silent(context))
        if (kind == ACTION_MORNING) {
            val file = NotificationArtwork.cacheFile(context, affirmation, light); val picture = if (file.exists()) android.graphics.BitmapFactory.decodeFile(file.absolutePath) else NotificationArtwork.bigPicture(context, affirmation, light)
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(picture).setSummaryText(affirmation.reference)).addAction(R.drawable.ic_notification, "Share", action(context, ACTION_SHARE, affirmation, light, id)).addAction(R.drawable.ic_notification, "Keep", action(context, ACTION_KEEP, affirmation, light, id))
        } else builder.setLargeIcon(NotificationArtwork.tile(context, light))
        try { NotificationManagerCompat.from(context).notify(id, builder.build()) } catch (_: SecurityException) { }
        if (kind != ACTION_STREAK) NotificationStore.addHistory(context, affirmation.id)
    }
    private fun action(c: Context, action: String, a: Affirmation, light: Light, id: Int): PendingIntent = PendingIntent.getBroadcast(c, id + action.hashCode(), Intent(c, NotificationActionReceiver::class.java).setAction(action).putExtra(EXTRA_ID, a.id).putExtra(EXTRA_LIGHT, light.name), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun sendTestNotification(c: Context, isHourly: Boolean = false) = showNotification(c, ACTION_MORNING, chooseMorningAffirmation(c), deliveryLight())
    fun createNotificationChannel(c: Context) = createNotificationChannels(c)
    fun setPinnedAffirmation(c: Context, a: Affirmation) = showNotification(c, ACTION_MORNING, a, deliveryLight())
    fun scheduleAffirmationReminder(c: Context, a: Affirmation, hour: Int? = null, minute: Int? = null) { if (hour != null && minute != null) setReminderTime(c, hour, minute); setPinnedAffirmation(c, a) }
    fun getPinnedAffirmation(c: Context): Affirmation? = null
    fun getReminderSource(c: Context) = ReminderSource.ALL
    fun setReminderSource(c: Context, source: ReminderSource) = Unit
    fun getUserName(c: Context) = "Friend"
    fun setUserName(c: Context, name: String) = Unit
}

private object ProfileActivityLogCompat { fun streak(context: Context): Int { val dates = context.getSharedPreferences("makarios_activity_log", Context.MODE_PRIVATE).getStringSet("action", emptySet()).orEmpty(); var day = LocalDate.now(); var count = 0; while (day.toString() in dates) { count++; day = day.minusDays(1) }; return count } }
