package com.focuslock.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FocusProgressTest {
    private fun date(value: String) = LocalDate.parse(value)

    @Test fun todayCanStillBeCompleted() {
        assertEquals(2, FocusProgress.streak(setOf(date("2026-10-09"), date("2026-10-10")), date("2026-10-11"), 127))
    }

    @Test fun missedPlannedDayBreaksStreak() {
        assertEquals(0, FocusProgress.streak(setOf(date("2026-10-09")), date("2026-10-11"), 127))
    }

    @Test fun weekendsDoNotBreakWeekdayStreak() {
        val dates = setOf(date("2026-10-08"), date("2026-10-09"), date("2026-10-12"))
        assertEquals(2, FocusProgress.streak(dates, date("2026-10-11"), 62))
        assertEquals(3, FocusProgress.streak(dates, date("2026-10-12"), 62))
    }

    @Test fun completingRestDayDoesNotReplaceMissedPlannedDay() {
        assertEquals(0, FocusProgress.streak(setOf(date("2026-10-11")), date("2026-10-11"), 62))
    }

    @Test fun gapsAndCalendarBoundariesAreHandled() {
        assertEquals(3, FocusProgress.streak(setOf(date("2025-12-31"), date("2026-01-01"), date("2026-01-02")), date("2026-01-02"), 127))
        assertEquals(2, FocusProgress.streak(setOf(date("2024-02-28"), date("2024-02-29")), date("2024-03-01"), 127))
    }

    @Test fun emptyScheduleOrHistoryHasNoStreak() {
        assertEquals(0, FocusProgress.streak(emptySet(), date("2026-10-11"), 127))
        assertEquals(0, FocusProgress.streak(setOf(date("2026-10-11")), date("2026-10-11"), 0))
    }

    @Test fun sundayIsBitZero() {
        assertTrue(FocusProgress.isPlanned(date("2026-10-11"), 1))
        assertFalse(FocusProgress.isPlanned(date("2026-10-12"), 1))
    }

    @Test fun weekStartsMondayAcrossYearBoundary() {
        assertEquals(date("2025-12-29"), FocusProgress.weekStart(date("2026-01-04")))
    }

    @Test fun resetDoesNotAccrueFocus() {
        // Eight focus minutes remain, then a one-minute reset is added to the deadline.
        assertEquals(480_000L, FocusProgress.remainingFocusMillis(540_000L, 60_000L, 0))
        assertEquals(480_000L, FocusProgress.remainingFocusMillis(540_000L, 60_000L, 30_000L))
        assertEquals(480_000L, FocusProgress.remainingFocusMillis(540_000L, 60_000L, 60_000L))
        assertEquals(450_000L, FocusProgress.remainingFocusMillis(540_000L, 60_000L, 90_000L))
    }

    @Test fun endingDuringResetKeepsOnlyFocusedTime() {
        val remaining = FocusProgress.remainingFocusMillis(540_000L, 60_000L, 30_000L)
        assertEquals(420_000L, FocusProgress.focusedMillis(15, remaining))
    }

    @Test fun subMinuteInterruptionsNeverEarnAnInventedMinute() {
        assertEquals(5_000L, FocusProgress.focusedMillis(15, 895_000L))
        assertEquals(0L, FocusProgress.focusedMillis(15, 1_000_000L))
    }

    @Test fun expiredSessionHasZeroRemainingTime() {
        assertEquals(0L, FocusProgress.remainingFocusMillis(1000, 0, 2000))
        assertEquals(900_000L, FocusProgress.focusedMillis(15, 0))
    }
}
