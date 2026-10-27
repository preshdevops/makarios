package com.makarios.app.widget

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.makarios.app.data.WidgetRotateEvery
import com.makarios.app.data.WidgetSettingsStore
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object WidgetScheduling {
    private const val WORK_NAME = "makarios_widget_boundary_refresh"
    @Volatile private var initialized = false
    private val initLock = Any()

    fun initialize(context: Context) = ensureInitialized(context)

    private fun ensureInitialized(context: Context) {
        if (initialized) return
        synchronized(initLock) {
            if (initialized) return
            try {
                val configuration = androidx.work.Configuration.Builder()
                    .setMinimumLoggingLevel(Log.INFO)
                    .build()
                WorkManager.initialize(context.applicationContext, configuration)
            } catch (_: IllegalStateException) {
                WorkManager.getInstance(context.applicationContext)
            }
            initialized = true
        }
    }

    /**
     * Schedules the next widget refresh based on the global WidgetSettings.rotateEvery:
     * - ON_TAP: schedule at the next Light-boundary (same as before)
     * - HOURLY: 1 hour
     * - THREE_HOUR: 3 hours
     * - DAILY: next 06:00
     */
    fun schedule(context: Context) {
        runCatching {
            ensureInitialized(context)
            val settings = WidgetSettingsStore.load(context)
            val delayMillis = when (settings.rotateEvery) {
                WidgetRotateEvery.ON_TAP -> lightBoundaryDelayMillis()
                WidgetRotateEvery.HOURLY -> Duration.ofHours(1).toMillis()
                WidgetRotateEvery.THREE_HOUR -> Duration.ofHours(3).toMillis()
                WidgetRotateEvery.DAILY -> dailyDelayMillis()
            }.coerceAtLeast(60_000L) // never less than 1 min

            val workRequest = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest)
        }.onFailure { Log.e("WidgetScheduling", "WorkManager schedule failed", it) }
    }

    fun refreshNow(context: Context) {
        runCatching {
            ensureInitialized(context)
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build())
            schedule(context)
        }.onFailure { Log.e("WidgetScheduling", "WorkManager refreshNow failed", it) }
    }

    /** Returns the epoch-millis of the next scheduled refresh (approximate). */
    fun nextRefreshEpochMillis(context: Context): Long {
        val settings = WidgetSettingsStore.load(context)
        val delayMs = when (settings.rotateEvery) {
            WidgetRotateEvery.ON_TAP -> lightBoundaryDelayMillis()
            WidgetRotateEvery.HOURLY -> Duration.ofHours(1).toMillis()
            WidgetRotateEvery.THREE_HOUR -> Duration.ofHours(3).toMillis()
            WidgetRotateEvery.DAILY -> dailyDelayMillis()
        }
        return System.currentTimeMillis() + delayMs
    }

    private fun lightBoundaryDelayMillis(): Long {
        val now = ZonedDateTime.now()
        val boundaries = listOf(
            LocalTime.of(5, 30),
            LocalTime.of(9, 0),
            LocalTime.of(16, 30),
            LocalTime.of(19, 30)
        )
        val nextBoundary = boundaries
            .map { now.toLocalDate().atTime(it).atZone(now.zone) }
            .filter { it.isAfter(now) }
            .minOrNull()
            ?: now.toLocalDate().plusDays(1).atTime(boundaries.firstOrNull() ?: java.time.LocalTime.of(6, 0)).atZone(now.zone)
        return Duration.between(now, nextBoundary).toMillis().coerceAtLeast(1000L)
    }

    private fun dailyDelayMillis(): Long {
        val now = ZonedDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(6, 0)).atZone(now.zone)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis().coerceAtLeast(1000L)
    }
}