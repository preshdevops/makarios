package com.makarios.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object NotificationSchedule {
    fun next(now: ZonedDateTime, time: LocalTime, days: Set<DayOfWeek>): ZonedDateTime {
        var candidate = now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
        if (!candidate.isAfter(now) || candidate.dayOfWeek !in days) candidate = candidate.plusDays(1).withHour(time.hour).withMinute(time.minute)
        while (candidate.dayOfWeek !in days) candidate = candidate.plusDays(1)
        return candidate
    }
    fun survivesTimezoneChange(now: ZonedDateTime, time: LocalTime, days: Set<DayOfWeek>): ZonedDateTime = next(now, time, days)
    fun survivesDst(now: ZonedDateTime, time: LocalTime, days: Set<DayOfWeek>): LocalDate = next(now, time, days).toLocalDate()
}
