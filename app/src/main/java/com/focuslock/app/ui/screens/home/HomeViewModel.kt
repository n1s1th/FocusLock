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

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    val isSessionActive = app.preferences.isSessionActiveFlow

    val bags: StateFlow<List<BagEntity>> = app.database.bagDao().getAllBagsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMinutes = MutableStateFlow(25)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    // Selected bag index for the Home screen bag picker
    private val _selectedBagIndex = MutableStateFlow(0)
    val selectedBagIndex: StateFlow<Int> = _selectedBagIndex.asStateFlow()

    // Live parachute count (from preferences StateFlow)
    val parachuteCount: StateFlow<Int> = app.preferences.parachutesStateFlow

    fun setMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
    }

    fun setSelectedBagIndex(index: Int) {
        _selectedBagIndex.value = index
    }

    fun startFocusSession(bag: BagEntity? = null) {
        val duration = _selectedMinutes.value
        val endTime = System.currentTimeMillis() + (duration * 60 * 1000L)
        val selectedBagId = app.preferences.getSelectedBagId()
        val targetBag = bag
            ?: bags.value.find { it.id == selectedBagId }
            ?: bags.value.firstOrNull()
            ?: BagEntity(id = selectedBagId, name = app.preferences.getActiveBagName(), isDefault = true)

        val allowed = targetBag.allowedPackages.filter { it.isNotBlank() }
        app.preferences.setActiveAllowedPackages(allowed)
        com.focuslock.app.service.FocusAccessibilityService.updateAllowedPackages(allowed.toSet())

        app.preferences.startSession(
            endTimeMillis = endTime,
            durationMinutes = duration,
            bagId = targetBag.id,
            bagName = targetBag.name,
            allowedPackages = allowed
        )

        FocusLockService.startService(app)
    }

    fun getStreak(): Int = app.preferences.getCurrentStreak()
}
