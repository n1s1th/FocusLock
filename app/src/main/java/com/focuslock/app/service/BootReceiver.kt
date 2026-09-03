package com.focuslock.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.focuslock.app.FocusLockApp

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val app = FocusLockApp.instance

        Log.d("BootReceiver", "Device boot or time reset received: ${intent?.action}")

        // 1. If a session was active during reboot, resume it immediately
        if (app.preferences.isSessionActive()) {
            FocusLockService.startService(context)
        }

        // 2. Reschedule all enabled routines
        RoutineScheduler(context).scheduleAllActiveRoutines()
    }
}
