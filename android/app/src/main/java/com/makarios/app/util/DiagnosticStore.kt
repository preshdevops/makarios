package com.makarios.app.util

import android.content.Context
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Lightweight SharedPreferences store that records scheduling diagnostics.
 * Written from background threads; read on the crash / diagnostic screen.
 */
object DiagnosticStore {
    private const val PREFS = "makarios_diagnostics"
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    fun setNextWidgetRefresh(context: Context, epochMillis: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("next_widget_refresh", epochMillis).apply()
    }

    fun setNextReminderFire(context: Context, epochMillis: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("next_reminder_fire", epochMillis).apply()
    }

    fun setLastPickedId(context: Context, id: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("last_picked_id", id).apply()
    }

    fun getNextWidgetRefreshLabel(context: Context): String {
        val ms = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("next_widget_refresh", 0L)
        return if (ms == 0L) "not scheduled" else fmt.format(Instant.ofEpochMilli(ms))
    }

    fun getNextReminderFireLabel(context: Context): String {
        val ms = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("next_reminder_fire", 0L)
        return if (ms == 0L) "not scheduled" else fmt.format(Instant.ofEpochMilli(ms))
    }

    fun getLastPickedId(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("last_picked_id", "none") ?: "none"

    /** Single-line summary for the crash / diagnostic screen. */
    fun reportLine(context: Context): String =
        "Widget next: ${getNextWidgetRefreshLabel(context)} | Reminder next: ${getNextReminderFireLabel(context)} | Last picked: ${getLastPickedId(context)}"
}
