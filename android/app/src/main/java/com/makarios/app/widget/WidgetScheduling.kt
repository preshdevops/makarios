package com.makarios.app.widget

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object WidgetScheduling {
    private const val WORK_NAME = "makarios_widget_boundary_refresh"

    fun schedule(context: Context) {
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
            ?: now.toLocalDate().plusDays(1).atTime(boundaries.first()).atZone(now.zone)

        val delayMillis = Duration.between(now, nextBoundary).toMillis().coerceAtLeast(1000L)

        val workRequest = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun refreshNow(context: Context) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
        )
        schedule(context)
    }
}
