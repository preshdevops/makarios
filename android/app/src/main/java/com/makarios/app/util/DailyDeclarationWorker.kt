package com.makarios.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.makarios.app.MainActivity
import com.makarios.app.R
import com.makarios.app.data.DeclarationPicker
import com.makarios.app.data.LockScreenText
import com.makarios.app.data.ReminderSettingsStore
import com.makarios.app.data.ReminderTime
import com.makarios.app.ui.theme.Light
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * WorkManager-based daily declaration reminder.
 *
 * This worker fires the notification, then schedules itself again
 * for the next configured time (OneTimeWork rescheduled after each fire).
 * No SCHEDULE_EXACT_ALARM permission is used.
 */
class DailyDeclarationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        /** v2 — channel recreated with default sound (v1 was explicitly silent). */
        const val CHANNEL_ID = "daily_declarations_v2"
        private const val CHANNEL_ID_LEGACY = "daily_declarations"
        const val CHANNEL_NAME = "Daily declarations"
        const val NOTIFICATION_ID = 5001
        const val ACTION_SHARE = "com.makarios.app.REMINDER_SHARE"
        const val ACTION_NEXT  = "com.makarios.app.REMINDER_NEXT"
        const val EXTRA_DECL_ID = "decl_id"
        const val EXTRA_SLOT    = "slot_index" // which time slot this firing covers (0,1,2)
        private const val WORK_PREFIX = "makarios_daily_decl_"

        fun scheduleAll(context: Context) {
            val settings = ReminderSettingsStore.load(context)
            ensureChannel(context)
            settings.times.forEachIndexed { idx, time ->
                scheduleSlot(context, idx, time)
            }
        }

        fun scheduleSlot(context: Context, slotIndex: Int, time: ReminderTime) {
            val delayMs = nextDelayMillis(time.hour, time.minute)
            val nextEpoch = System.currentTimeMillis() + delayMs
            DiagnosticStore.setNextReminderFire(context, nextEpoch)

            val data = Data.Builder().putInt(EXTRA_SLOT, slotIndex).build()
            val request = OneTimeWorkRequestBuilder<DailyDeclarationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("$WORK_PREFIX$slotIndex", ExistingWorkPolicy.REPLACE, request)
        }

        fun cancelAll(context: Context) {
            repeat(3) { idx ->
                WorkManager.getInstance(context).cancelUniqueWork("$WORK_PREFIX$idx")
            }
        }

        private fun nextDelayMillis(hour: Int, minute: Int): Long {
            val now = ZonedDateTime.now()
            var target = now.toLocalDate().atTime(LocalTime.of(hour, minute)).atZone(now.zone)
            if (!target.isAfter(now)) target = target.plusDays(1)
            return Duration.between(now, target).toMillis().coerceAtLeast(1_000L)
        }

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Delete the old silent channel so it doesn't appear as a duplicate
            // in the user's notification settings. Safe to call repeatedly.
            runCatching { mgr.deleteNotificationChannel(CHANNEL_ID_LEGACY) }

            // Only create if not already present — channel settings are user-editable
            // after first creation, so we never overwrite their preferences.
            if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "One declaration each day, at your chosen time."
                // Sound and vibration use system defaults — user can customise in Settings.
            }
            mgr.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result {
        return runCatching {
            val slotIndex = inputData.getInt(EXTRA_SLOT, 0)
            val settings = ReminderSettingsStore.load(appContext)

            // Quiet hours check
            val hour = LocalTime.now().hour
            val inQuiet = if (settings.quietHoursStart != null && settings.quietHoursEnd != null) {
                if (settings.quietHoursStart <= settings.quietHoursEnd) {
                    hour in settings.quietHoursStart until settings.quietHoursEnd
                } else {
                    // wraps midnight
                    hour >= settings.quietHoursStart || hour < settings.quietHoursEnd
                }
            } else false

            // Check notification permission
            val hasPermission = if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true

            // Check system-level notification enabled
            val notifEnabled = NotificationManagerCompat.from(appContext).areNotificationsEnabled()

            if (!inQuiet && hasPermission && notifEnabled) {
                val lastId = DiagnosticStore.getLastPickedId(appContext).takeIf { it != "none" }
                val affirmation = DeclarationPicker.pickForReminder(appContext, settings, excludeId = lastId)
                DiagnosticStore.setLastPickedId(appContext, affirmation.id)

                val light = Light.forNow()
                showNotification(affirmation, settings, light)
            }

            // Reschedule for next occurrence
            val slot = settings.times.getOrNull(slotIndex)
            if (slot != null) {
                scheduleSlot(appContext, slotIndex, slot)
            }

            Result.success()
        }.getOrElse { e ->
            Log.e("DailyDeclarationWorker", "Worker failed", e)
            // Reschedule even on failure so we don't lose the reminder
            val slotIndex = inputData.getInt(EXTRA_SLOT, 0)
            val settings = runCatching { ReminderSettingsStore.load(appContext) }.getOrNull()
            settings?.times?.getOrNull(slotIndex)?.let { scheduleSlot(appContext, slotIndex, it) }
            Result.success() // Don't retry — we already rescheduled
        }
    }

    private fun showNotification(
        affirmation: com.makarios.app.data.Affirmation,
        settings: com.makarios.app.data.ReminderSettings,
        light: Light
    ) {
        ensureChannel(appContext)

        val deepLinkIntent = Intent(appContext, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("makarios://declare?id=${affirmation.id}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            appContext, NOTIFICATION_ID, deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Share" action
        val shareIntent = Intent(appContext, NotificationActionReceiver::class.java).apply {
            action = ACTION_SHARE
            putExtra(EXTRA_DECL_ID, affirmation.id)
            putExtra("decl_text", affirmation.declaration)
            putExtra("decl_ref", affirmation.reference)
            putExtra("verse_text", affirmation.scriptureText)
        }
        val sharePi = PendingIntent.getBroadcast(
            appContext, NOTIFICATION_ID + 1, shareIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Next" action — triggers next declaration
        val nextIntent = Intent(appContext, NotificationActionReceiver::class.java).apply {
            action = ACTION_NEXT
            putExtra(EXTRA_DECL_ID, affirmation.id)
        }
        val nextPi = PendingIntent.getBroadcast(
            appContext, NOTIFICATION_ID + 2, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = affirmation.declaration.take(120).let {
            if (it.length == 120) "$it…" else it
        }
        val bodyText = if (settings.includeVerse) {
            "\"${affirmation.scriptureText.take(100)}\" — ${affirmation.reference}"
        } else {
            affirmation.reference
        }

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF8A5A14.toInt())
            .setContentTitle(title)
            .setContentText(bodyText)
            .setContentIntent(contentPi)
            .setSettingsText("Reminder settings")
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bodyText)
                    .setBigContentTitle(title)
            )
            .addAction(R.drawable.ic_notification, "Share", sharePi)
            .addAction(R.drawable.ic_notification, "Next", nextPi)

        // Lock-screen visibility
        if (settings.lockScreenText == LockScreenText.HIDE) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            val publicVersion = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("A declaration for you")
                .setContentText("Unlock to read your declaration")
                .build()
            builder.setPublicVersion(publicVersion)
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        val canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (canPost) {
            runCatching {
                NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, builder.build())
            }.onFailure { Log.w("DailyDeclarationWorker", "Could not post notification", it) }
        }
    }
}
