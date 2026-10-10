package com.makarios.app.util

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate

object NotificationStore {
    const val PREFS = "makarios_notifications"
    private const val MORNING_ENABLED = "morning_enabled"
    private const val MORNING_HOUR = "morning_hour"
    private const val MORNING_MINUTE = "morning_minute"
    private const val EVENING_ENABLED = "evening_enabled"
    private const val EVENING_HOUR = "evening_hour"
    private const val EVENING_MINUTE = "evening_minute"
    private const val STREAK_ENABLED = "streak_enabled"
    private const val SILENT = "silent"
    private const val DAYS = "days"
    private const val HISTORY = "history"
    private const val PERMISSION_ASKS = "permission_asks"
    private const val ASK_SHOWN = "ask_shown"
    private fun p(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun morningEnabled(c: Context) = p(c).getBoolean(MORNING_ENABLED, true)
    fun setMorningEnabled(c: Context, v: Boolean) = p(c).edit().putBoolean(MORNING_ENABLED, v).apply()
    fun morningTime(c: Context) = p(c).getInt(MORNING_HOUR, 6) to p(c).getInt(MORNING_MINUTE, 30)
    fun setMorningTime(c: Context, h: Int, m: Int) = p(c).edit().putInt(MORNING_HOUR, h).putInt(MORNING_MINUTE, m).apply()
    fun eveningEnabled(c: Context) = p(c).getBoolean(EVENING_ENABLED, false)
    fun setEveningEnabled(c: Context, v: Boolean) = p(c).edit().putBoolean(EVENING_ENABLED, v).apply()
    fun eveningTime(c: Context) = p(c).getInt(EVENING_HOUR, 21) to p(c).getInt(EVENING_MINUTE, 30)
    fun setEveningTime(c: Context, h: Int, m: Int) = p(c).edit().putInt(EVENING_HOUR, h).putInt(EVENING_MINUTE, m).apply()
    fun streakEnabled(c: Context) = p(c).getBoolean(STREAK_ENABLED, false)
    fun setStreakEnabled(c: Context, v: Boolean) = p(c).edit().putBoolean(STREAK_ENABLED, v).apply()
    fun silent(c: Context) = p(c).getBoolean(SILENT, true)
    fun setSilent(c: Context, v: Boolean) = p(c).edit().putBoolean(SILENT, v).apply()
    fun weekdays(c: Context): Set<DayOfWeek> = p(c).getStringSet(DAYS, DayOfWeek.values().map { it.value.toString() }.toSet()).orEmpty().mapNotNull { it.toIntOrNull()?.let(DayOfWeek::of) }.toSet()
    fun setWeekdays(c: Context, days: Set<DayOfWeek>) = p(c).edit().putStringSet(DAYS, days.map { it.value.toString() }.toSet()).apply()
    fun markAsked(c: Context) = p(c).edit().putInt(PERMISSION_ASKS, permissionAsks(c) + 1).putBoolean(ASK_SHOWN, true).apply()
    fun permissionAsks(c: Context) = p(c).getInt(PERMISSION_ASKS, 0)
    fun askShown(c: Context) = p(c).getBoolean(ASK_SHOWN, false)
    fun addHistory(c: Context, id: String, date: LocalDate = LocalDate.now()) { val old = p(c).getStringSet(HISTORY, emptySet()).orEmpty().toMutableSet(); old.removeAll { it.substringAfter('|', "") < date.minusDays(14).toString() }; old.add("$id|$date"); p(c).edit().putStringSet(HISTORY, old).apply() }
    fun recentIds(c: Context, date: LocalDate = LocalDate.now()): Set<String> = p(c).getStringSet(HISTORY, emptySet()).orEmpty().filter { it.substringAfter('|', "") >= date.minusDays(14).toString() }.map { it.substringBefore('|') }.toSet()
}
