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

    val currentStreak: Int
        get() = app.preferences.getCurrentStreak()

    val totalParachutes: Int
        get() = app.preferences.getTotalParachutes()

    init {
        ensureDefaultRoutines()
    }

    private fun ensureDefaultRoutines() {
        viewModelScope.launch(Dispatchers.IO) {
            val dao = app.database.routineDao()
            val existing = dao.getAllRoutinesFlow()
            val list = dao.getActiveRoutines()
            val all = app.database.routineDao().getRoutineById(1L)
            // If fewer than 3 routines, populate Routine 1, 2, 3
            if (all == null) {
                dao.insertRoutine(
                    RoutineEntity(
                        id = 1,
                        name = "Routine 1",
                        targetBagId = 1L,
                        startHour = 0,
                        startMinute = 0,
                        durationMinutes = 60,
                        activeDaysMask = 127,
                        isEnabled = false
                    )
                )
                dao.insertRoutine(
                    RoutineEntity(
                        id = 2,
                        name = "Routine 2",
                        targetBagId = 1L,
                        startHour = 0,
                        startMinute = 0,
                        durationMinutes = 60,
                        activeDaysMask = 127,
                        isEnabled = false
                    )
                )
                dao.insertRoutine(
                    RoutineEntity(
                        id = 3,
                        name = "Routine 3",
                        targetBagId = 1L,
                        startHour = 0,
                        startMinute = 0,
                        durationMinutes = 60,
                        activeDaysMask = 127,
                        isEnabled = false
                    )
                )
            }
        }
    }

    fun updateRoutineTimes(routine: RoutineEntity, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val startTotal = startHour * 60 + startMinute
            val endTotal = endHour * 60 + endMinute
            val duration = if (endTotal >= startTotal) (endTotal - startTotal) else (24 * 60 - startTotal + endTotal)
            val updated = routine.copy(
                startHour = startHour,
                startMinute = startMinute,
                durationMinutes = maxOf(15, duration)
            )
            app.database.routineDao().updateRoutine(updated)
            if (updated.isEnabled) {
                scheduler.scheduleRoutine(updated)
            }
        }
    }

    fun updateRoutineDays(routine: RoutineEntity, daysMask: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = routine.copy(activeDaysMask = daysMask)
            app.database.routineDao().updateRoutine(updated)
            if (updated.isEnabled) {
                scheduler.scheduleRoutine(updated)
            }
        }
    }

    fun setRoutineEnabled(routine: RoutineEntity, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = routine.copy(isEnabled = enabled)
            app.database.routineDao().updateRoutine(updated)
            if (enabled) {
                scheduler.scheduleRoutine(updated)
            } else {
                scheduler.cancelRoutine(routine.id)
            }
        }
    }

    fun toggleRoutine(routine: RoutineEntity) {
        setRoutineEnabled(routine, !routine.isEnabled)
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            scheduler.cancelRoutine(routine.id)
            app.database.routineDao().deleteRoutine(routine)
        }
    }
}
