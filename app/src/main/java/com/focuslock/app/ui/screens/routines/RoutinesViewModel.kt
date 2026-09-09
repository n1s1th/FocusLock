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

    /**
     * Fix 5: Reliably ensure 3 default routines exist by checking actual count
     * and inserting only the missing ones using known IDs.
     */
    private fun ensureDefaultRoutines() {
        viewModelScope.launch(Dispatchers.IO) {
            val dao = app.database.routineDao()

            // Check each by explicit ID to only insert truly missing ones
            val r1 = dao.getRoutineById(1L)
            val r2 = dao.getRoutineById(2L)
            val r3 = dao.getRoutineById(3L)

            if (r1 == null) {
                dao.insertRoutine(
                    RoutineEntity(
                        id = 1,
                        name = "Routine 1",
                        targetBagId = 1L,
                        startHour = 9,
                        startMinute = 0,
                        durationMinutes = 60,
                        activeDaysMask = 62, // Mon–Fri (bits 1..5)
                        isEnabled = false
                    )
                )
            }
            if (r2 == null) {
                dao.insertRoutine(
                    RoutineEntity(
                        id = 2,
                        name = "Routine 2",
                        targetBagId = 1L,
                        startHour = 14,
                        startMinute = 0,
                        durationMinutes = 60,
                        activeDaysMask = 62,
                        isEnabled = false
                    )
                )
            }
            if (r3 == null) {
                dao.insertRoutine(
                    RoutineEntity(
                        id = 3,
                        name = "Routine 3",
                        targetBagId = 1L,
                        startHour = 20,
                        startMinute = 0,
                        durationMinutes = 60,
                        activeDaysMask = 127,
                        isEnabled = false
                    )
                )
            }
        }
    }

    /**
     * Fix 12: Calculate duration without forcing a 15-min minimum distortion.
     * The minimum stored duration is 15 min, but adjust endHour/endMinute accordingly
     * so the display stays consistent with what the user set.
     */
    fun updateRoutineTimes(routine: RoutineEntity, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val startTotal = startHour * 60 + startMinute
            val endTotal = endHour * 60 + endMinute
            val rawDuration = if (endTotal >= startTotal) (endTotal - startTotal) else (24 * 60 - startTotal + endTotal)
            // Clamp to at least 15 minutes but store the actual start/end
            val duration = maxOf(15, rawDuration)
            val updated = routine.copy(
                startHour = startHour,
                startMinute = startMinute,
                durationMinutes = duration
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
