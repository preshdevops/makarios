package com.makarios.app.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.makarios.app.data.Affirmation
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.data.local.toAffirmation
import com.makarios.app.ui.theme.Light
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DailyDeclarationWorker.ACTION_SHARE -> {
                val declaration = intent.getStringExtra("decl_text")
                val id = intent.getStringExtra(DailyDeclarationWorker.EXTRA_DECL_ID).orEmpty()
                val affirmation = if (!declaration.isNullOrBlank()) Affirmation(
                    id = id,
                    declaration = declaration,
                    scriptureText = intent.getStringExtra("verse_text").orEmpty(),
                    reference = intent.getStringExtra("decl_ref").orEmpty()
                ) else AffirmationRepository.getById(id)
                if (affirmation != null) ShareHelper.shareGeneric(context, affirmation, Light.forNow())
            }
            DailyDeclarationWorker.ACTION_NEXT -> {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(DailyDeclarationWorker.NOTIFICATION_ID)
                WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<DailyDeclarationWorker>().build())
            }
            ReminderManager.ACTION_KEEP -> {
                val pending = goAsync()
                val id = intent.getStringExtra(ReminderManager.EXTRA_ID).orEmpty()
                val light = intent.getStringExtra(ReminderManager.EXTRA_LIGHT)?.let { runCatching { Light.valueOf(it) }.getOrNull() } ?: Light.forNow()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                                                val existing = KeptRepository.getById(id)
                        if (existing == null) {
                            val affirmation = AffirmationRepository.getById(id)
                            if (affirmation != null) KeptRepository.save(KeptEntity(id, affirmation.declaration, affirmation.scriptureText, affirmation.reference, light.name, "", sourceType = "bank"))
                        }
                        val affirmation = existing?.toAffirmation() ?: AffirmationRepository.getById(id)
                        if (affirmation != null) ReminderManager.showNotification(context, ReminderManager.ACTION_MORNING, affirmation, light, kept = true)
                    } finally {
                        pending.finish()
                    }
                }
            }
            ReminderManager.ACTION_SHARE -> {
                val id = intent.getStringExtra(ReminderManager.EXTRA_ID) ?: return
                val light = intent.getStringExtra(ReminderManager.EXTRA_LIGHT)?.let { runCatching { Light.valueOf(it) }.getOrNull() } ?: Light.forNow()
                val affirmation = AffirmationRepository.getById(id) ?: return
                ShareHelper.shareGeneric(context, affirmation, light)
            }
        }
    }
}
