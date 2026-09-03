package com.focuslock.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.focuslock.app.FocusLockApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val app = FocusLockApp.instance

        val routineId = intent.getLongExtra("routine_id", -1L)
        val routineName = intent.getStringExtra("routine_name") ?: "Scheduled Routine"
        val bagId = intent.getLongExtra("bag_id", 1L)
        val durationMinutes = intent.getIntExtra("duration_minutes", 30)

        Log.d("RoutineReceiver", "Automated routine triggered: $routineName ($durationMinutes mins)")

        // If session is already active, ignore or extend
        if (!app.preferences.isSessionActive()) {
            val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)

            app.applicationScope.launch(Dispatchers.IO) {
                val bag = app.database.bagDao().getBagById(bagId)
                val bagName = bag?.name ?: "Scheduled Bag"

                app.preferences.startSession(endTime, durationMinutes, bagId, bagName)
                FocusLockService.startService(context)
            }
        }

        // Reschedule for next occurrence
        app.applicationScope.launch(Dispatchers.IO) {
            val routine = app.database.routineDao().getRoutineById(routineId)
            if (routine != null && routine.isEnabled) {
                RoutineScheduler(context).scheduleRoutine(routine)
            }
        }
    }
}
