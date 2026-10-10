package com.makarios.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NotificationScheduleTest {
    private val all = DayOfWeek.values().toSet()
    @Test fun skipsUnselectedWeekdays() { val now = ZonedDateTime.of(2026, 10, 10, 8, 0, 0, 0, ZoneId.of("Africa/Lagos")); val next = NotificationSchedule.next(now, LocalTime.of(6, 30), setOf(DayOfWeek.MONDAY)); assertEquals(DayOfWeek.MONDAY, next.dayOfWeek) }
    @Test fun timezoneChangeRecomputesLocalHour() { val now = ZonedDateTime.of(2026, 10, 10, 5, 0, 0, 0, ZoneId.of("America/New_York")); assertEquals(6, NotificationSchedule.survivesTimezoneChange(now, LocalTime.of(6, 30), all).hour) }
    @Test fun dstKeepsCalendarDay() { val now = ZonedDateTime.of(2026, 3, 7, 23, 0, 0, 0, ZoneId.of("America/New_York")); assertEquals(8, NotificationSchedule.survivesDst(now, LocalTime.of(6, 30), all).dayOfMonth) }
    @Test fun bootUsesNextEligibleOccurrence() { val now = ZonedDateTime.of(2026, 10, 11, 21, 0, 0, 0, ZoneId.of("Africa/Lagos")); assertEquals(DayOfWeek.MONDAY, NotificationSchedule.next(now, LocalTime.of(20, 0), all).dayOfWeek) }
}
