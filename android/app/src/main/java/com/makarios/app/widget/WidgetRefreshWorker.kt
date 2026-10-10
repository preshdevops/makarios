package com.makarios.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetRefreshWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){
 override suspend fun doWork():Result=runCatching{
  MakariosSmallWidget().updateAll(applicationContext);MakariosMediumWidget().updateAll(applicationContext);MakariosLargeWidget().updateAll(applicationContext)
 }.fold({Result.success()},{Result.retry()})
}
