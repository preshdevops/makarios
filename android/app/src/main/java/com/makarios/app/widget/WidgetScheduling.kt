package com.makarios.app.widget

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.time.Duration
import java.time.ZonedDateTime

object WidgetScheduling {
 private const val NAME="makarios-widget-daily-refresh"
 fun schedule(context:Context){val now=ZonedDateTime.now();var next=now.withHour(com.makarios.app.util.ReminderManager.getReminderHour(context)).withMinute(com.makarios.app.util.ReminderManager.getReminderMinute(context)).withSecond(0).withNano(0);if(!next.isAfter(now))next=next.plusDays(1);val request=PeriodicWorkRequestBuilder<WidgetRefreshWorker>(1,TimeUnit.DAYS).setInitialDelay(Duration.between(now,next).toMillis(),TimeUnit.MILLISECONDS).build();WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME,ExistingPeriodicWorkPolicy.UPDATE,request)}
 fun refreshNow(context:Context){WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build())}
}

