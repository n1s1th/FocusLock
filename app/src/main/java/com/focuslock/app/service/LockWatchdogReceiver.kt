package com.focuslock.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.focuslock.app.FocusLockApp

class LockWatchdogReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val app = FocusLockApp.instance

        if (app.preferences.isSessionActive()) {
            Log.d("LockWatchdog", "Active focus session detected! Reviving FocusLockService...")
            FocusLockService.startService(context)
        }
    }

    companion object {
        fun scheduleWatchdog(context: Context, delayMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LockWatchdogReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = System.currentTimeMillis() + delayMillis
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }
}
