package com.makarios.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.ui.theme.Light
import com.makarios.app.util.ShareHelper

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(ReminderManager.EXTRA_ID) ?: return
        val affirmation = AffirmationRepository.getById(id) ?: return
        val light = intent.getStringExtra(ReminderManager.EXTRA_LIGHT)?.let { runCatching { Light.valueOf(it) }.getOrNull() } ?: Light.forNow()
        when (intent.action) {
            ReminderManager.ACTION_KEEP -> {
                AffirmationRepository.save(id)
                ReminderManager.showNotification(context, ReminderManager.ACTION_MORNING, affirmation, light, kept = true)
            }
            ReminderManager.ACTION_SHARE -> ShareHelper.shareGeneric(context, affirmation, light)
        }
    }
}
