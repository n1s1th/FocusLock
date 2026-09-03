package com.focuslock.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.focuslock.app.FocusLockApp
import com.focuslock.app.data.database.entities.RoutineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class RoutineScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleAllActiveRoutines() {
        val app = FocusLockApp.instance
        app.applicationScope.launch(Dispatchers.IO) {
            val routines = app.database.routineDao().getActiveRoutines()
            routines.forEach { routine ->
                scheduleRoutine(routine)
            }
        }
    }

    fun scheduleRoutine(routine: RoutineEntity) {
        if (!routine.isEnabled || alarmManager == null) return

        val triggerTimeMillis = calculateNextTriggerTime(routine.startHour, routine.startMinute, routine.activeDaysMask)
        val intent = Intent(context, RoutineAlarmReceiver::class.java).apply {
            putExtra("routine_id", routine.id)
            putExtra("routine_name", routine.name)
            putExtra("bag_id", routine.targetBagId)
            putExtra("duration_minutes", routine.durationMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routine.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        }
    }

    fun cancelRoutine(routineId: Long) {
        if (alarmManager == null) return
        val intent = Intent(context, RoutineAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int, daysMask: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Check next 7 days for matching day of week in bitmask
        for (i in 0..7) {
            if (i > 0 || target.after(now)) {
                val dayOfWeek = target.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon... 7=Sat
                val dayBit = 1 shl ((dayOfWeek + 5) % 7) // Convert to Mon(0)..Sun(6)
                if ((daysMask and dayBit) != 0) {
                    return target.timeInMillis
                }
            }
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis
    }
}
