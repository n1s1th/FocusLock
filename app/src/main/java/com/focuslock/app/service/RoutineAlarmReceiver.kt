package com.focuslock.app.service

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.focuslock.app.FocusLockApp
import com.focuslock.app.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val pending = goAsync()
        val app = FocusLockApp.instance
        app.applicationScope.launch(Dispatchers.IO) {
            try {
                val id = intent.getLongExtra("routine_id", -1)
                val routine = app.database.routineDao().getRoutineById(id) ?: return@launch
                if (!routine.isEnabled || routine.activeDaysMask == 0) return@launch
                if (!app.preferences.hasStoredSession()) {
                    val bag = app.database.bagDao().getBagById(routine.targetBagId)
                    if (routine.reminderOnly) {
                        val open = PendingIntent.getActivity(context, id.toInt(),
                            Intent(context, MainActivity::class.java).putExtra("routine_id", id),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        val notification = NotificationCompat.Builder(context, FocusLockApp.ROUTINE_NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                            .setContentTitle(if (routine.cue.isBlank()) "Time for your focus routine" else routine.cue)
                            .setContentText("${routine.durationMinutes} minutes · ${bag?.name ?: routine.name}")
                            .setContentIntent(open).setAutoCancel(true).build()
                        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context,
                                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            NotificationManagerCompat.from(context).notify(3000 + id.toInt(), notification)
                        }
                    } else if (bag != null) {
                        app.preferences.startSession(System.currentTimeMillis() + routine.durationMinutes * 60_000L,
                            routine.durationMinutes, bag.id, bag.name, bag.allowedPackages)
                        FocusLockService.startService(context)
                    }
                }
                RoutineScheduler(context).scheduleRoutine(routine)
            } finally { pending.finish() }
        }
    }
}
