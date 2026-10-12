package com.focuslock.app.data.model

import java.time.DayOfWeek
import java.time.LocalDate

/** Calendar rules shared by home, completion and stats. Bit zero is Sunday. */
object FocusProgress {
    fun isPlanned(date: LocalDate, daysMask: Int): Boolean =
        daysMask and (1 shl (date.dayOfWeek.value % 7)) != 0

    fun streak(completedDates: Set<LocalDate>, today: LocalDate, daysMask: Int): Int {
        if (daysMask == 0 || completedDates.isEmpty()) return 0
        var cursor = today
        // Today's opportunity remains open until the day ends.
        if (cursor !in completedDates) cursor = cursor.minusDays(1)
        while (!isPlanned(cursor, daysMask)) cursor = cursor.minusDays(1)
        var count = 0
        while (cursor in completedDates) {
            count++
            do { cursor = cursor.minusDays(1) } while (!isPlanned(cursor, daysMask))
        }
        return count
    }

    fun weekStart(today: LocalDate): LocalDate =
        today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

    fun remainingFocusMillis(end: Long, restEnd: Long, now: Long): Long =
        (end - now - (restEnd - now).coerceAtLeast(0)).coerceAtLeast(0)

    fun focusedMillis(plannedMinutes: Int, remaining: Long): Long =
        (plannedMinutes * 60_000L - remaining).coerceIn(0, plannedMinutes * 60_000L)
}
