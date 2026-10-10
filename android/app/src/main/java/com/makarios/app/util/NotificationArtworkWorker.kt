package com.makarios.app.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File

class NotificationArtworkWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val affirmation = ReminderManager.chooseMorningAffirmation(applicationContext)
        val light = ReminderManager.deliveryLight()
        return runCatching { File(applicationContext.cacheDir, "notification_${affirmation.id}_${light.name}.png").outputStream().use { output -> NotificationArtwork.bigPicture(applicationContext, affirmation, light).compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output) }; Result.success() }.getOrElse { Result.retry() }
    }
}