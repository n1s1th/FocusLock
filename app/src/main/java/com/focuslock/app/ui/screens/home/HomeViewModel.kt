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
import kotlinx.coroutines.Dispatchers
import com.focuslock.app.data.database.entities.SessionEntity

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    val isSessionActive = app.preferences.isSessionActiveFlow

    val bags: StateFlow<List<BagEntity>> = app.database.bagDao().getAllBagsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMinutes = MutableStateFlow(app.preferences.getSavedMinutes())
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    // Selected bag index for the Home screen bag picker
    private val _selectedBagIndex = MutableStateFlow(0)
    val selectedBagIndex: StateFlow<Int> = _selectedBagIndex.asStateFlow()

    // Live parachute count (from preferences StateFlow)
    val parachuteCount: StateFlow<Int> = app.preferences.parachutesStateFlow

    val sessions: StateFlow<List<SessionEntity>> = app.database.sessionDao().getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val habitRevision = app.preferences.habitRevision
    fun recordExitReason(key: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.sessionDao().setExitReason(key, reason)
            app.preferences.dismissPendingReason()
        }
    }

    init {
        viewModelScope.launch {
            app.preferences.routineRequest.collect { id ->
                if (id != null) {
                    val routine = app.database.routineDao().getRoutineById(id)
                    if (routine != null && !app.preferences.hasStoredSession()) {
                        app.preferences.setSelectedBagId(routine.targetBagId)
                        _selectedMinutes.value = routine.durationMinutes
                    }
                    app.preferences.routineRequest.value = null
                }
            }
        }
    }

    fun setMinutes(minutes: Int) {
        _selectedMinutes.value = minutes.coerceIn(1, 720)
    }

    fun setSelectedBagIndex(index: Int) {
        _selectedBagIndex.value = index
    }

    fun startFocusSession(bag: BagEntity? = null, task: String = "", plan: String = ""): Boolean {
        if (app.preferences.hasStoredSession() || !com.focuslock.app.service.FocusAccessibilityService.isServiceRunning) return false
        val duration = _selectedMinutes.value
        val endTime = System.currentTimeMillis() + (duration * 60 * 1000L)
        val selectedBagId = app.preferences.getSelectedBagId()
        val targetBag = bag
            ?: bags.value.find { it.id == selectedBagId }
            ?: bags.value.firstOrNull()
            ?: BagEntity(id = selectedBagId, name = app.preferences.getActiveBagName(), isDefault = true)

        val allowed = targetBag.allowedPackages.filter { it.isNotBlank() }
        val started = app.preferences.startSession(
            endTimeMillis = endTime,
            durationMinutes = duration,
            bagId = targetBag.id,
            bagName = targetBag.name,
            allowedPackages = allowed, task = task.trim().take(120), copingPlan = plan.trim().take(160)
        )

        if (!started) return false
        app.preferences.setSelectedBagId(targetBag.id)
        app.preferences.savePreset(duration, task, plan)
        FocusLockService.startService(app)
        return true
    }

    fun getStreak(): Int = app.preferences.getCurrentStreak()
}
