package com.focuslock.app.ui.screens.routines

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.FocusLockApp
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.data.database.entities.RoutineEntity
import com.focuslock.app.service.RoutineScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    private val scheduler = RoutineScheduler(app)

    val routines: StateFlow<List<RoutineEntity>> = app.database.routineDao().getAllRoutinesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bags: StateFlow<List<BagEntity>> = app.database.bagDao().getAllBagsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createRoutine(
        name: String,
        targetBagId: Long,
        startHour: Int,
        startMinute: Int,
        durationMinutes: Int,
        daysMask: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = RoutineEntity(
                name = name,
                targetBagId = targetBagId,
                startHour = startHour,
                startMinute = startMinute,
                durationMinutes = durationMinutes,
                activeDaysMask = daysMask,
                isEnabled = true
            )
            val id = app.database.routineDao().insertRoutine(entity)
            scheduler.scheduleRoutine(entity.copy(id = id))
        }
    }

    fun toggleRoutine(routine: RoutineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val newEnabled = !routine.isEnabled
            app.database.routineDao().toggleRoutine(routine.id, newEnabled)
            if (newEnabled) {
                scheduler.scheduleRoutine(routine.copy(isEnabled = true))
            } else {
                scheduler.cancelRoutine(routine.id)
            }
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            scheduler.cancelRoutine(routine.id)
            app.database.routineDao().deleteRoutine(routine)
        }
    }
}
