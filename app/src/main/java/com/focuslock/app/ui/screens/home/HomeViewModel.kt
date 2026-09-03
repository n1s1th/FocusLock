package com.focuslock.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.FocusLockApp
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.service.FocusLockService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    val isSessionActive = app.preferences.isSessionActiveFlow

    val bags: StateFlow<List<BagEntity>> = app.database.bagDao().getAllBagsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMinutes = MutableStateFlow(25)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    private val _selectedBagId = MutableStateFlow(1L)
    val selectedBagId: StateFlow<Int> = MutableStateFlow(0) // index for UI selection

    fun setMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
    }

    fun startFocusSession(bag: BagEntity) {
        val duration = _selectedMinutes.value
        val endTime = System.currentTimeMillis() + (duration * 60 * 1000L)

        app.preferences.startSession(
            endTimeMillis = endTime,
            durationMinutes = duration,
            bagId = bag.id,
            bagName = bag.name
        )

        FocusLockService.startService(app)
    }

    fun getStreak(): Int = app.preferences.getCurrentStreak()
}
