package com.makarios.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.makarios.app.util.DiagnosticStore

class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        MakariosGlanceWidget().updateAll(applicationContext)
        WidgetScheduling.schedule(applicationContext)
        // Record next scheduled refresh time in DiagnosticStore
        DiagnosticStore.setNextWidgetRefresh(applicationContext, WidgetScheduling.nextRefreshEpochMillis(applicationContext))
        Result.success()
    }.getOrElse {
        Result.retry()
    }
}
