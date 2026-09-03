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
import java.util.Date
import java.util.Locale

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val totalLifetimeMinutes: StateFlow<Int?> = app.database.sessionDao().getTotalLifetimeFocusMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCompletedSessions: StateFlow<Int> = app.database.sessionDao().getTotalCompletedSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayMinutes: StateFlow<Int?> = app.database.sessionDao().getTotalFocusMinutesForDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentSessions: StateFlow<List<SessionEntity>> = app.database.sessionDao().getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getStreak(): Int = app.preferences.getCurrentStreak()
}
