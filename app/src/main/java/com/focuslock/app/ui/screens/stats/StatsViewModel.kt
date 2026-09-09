package com.focuslock.app.ui.screens.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.FocusLockApp
import com.focuslock.app.data.database.entities.SessionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp

    val today: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val totalLifetimeMinutes: StateFlow<Int?> = app.database.sessionDao().getTotalLifetimeFocusMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCompletedSessions: StateFlow<Int> = app.database.sessionDao().getTotalCompletedSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayMinutes: StateFlow<Int?> = app.database.sessionDao().getTotalFocusMinutesForDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentSessions: StateFlow<List<SessionEntity>> = app.database.sessionDao().getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parachutesCount: StateFlow<Int> = app.preferences.parachutesStateFlow

    fun getStreak(): Int = app.preferences.getCurrentStreak()

    /** Returns display string for a given Calendar month/year, e.g. "SEPTEMBER 2026" */
    fun formatMonthYear(cal: Calendar): String {
        return SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time).uppercase(Locale.US)
    }

    /** Format date as "FRIDAY 4 · X MIN" */
    fun formatDayLabel(sessions: List<SessionEntity>): String {
        val dayFmt = SimpleDateFormat("EEEE d", Locale.US)
        val label = dayFmt.format(Date()).uppercase(Locale.US)
        val todayMins = sessions
            .filter { it.dateString == today }
            .sumOf { it.actualDurationMinutes }
        return "$label  ·  $todayMins MIN"
    }

    /**
     * Returns daily focus minute data for the last 7 calendar days, newest first.
     * Used to drive the bar chart. Returns normalized fractions 0..1 relative to max day.
     */
    fun getWeeklyBarData(sessions: List<SessionEntity>): List<Float> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val result = mutableListOf<Float>()
        val minutesList = mutableListOf<Int>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = fmt.format(dayCal.time)
            val mins = sessions.filter { it.dateString == dateStr }.sumOf { it.actualDurationMinutes }
            minutesList.add(mins)
        }
        val maxMins = minutesList.maxOrNull()?.takeIf { it > 0 } ?: 1
        return minutesList.map { it.toFloat() / maxMins }
    }
}
