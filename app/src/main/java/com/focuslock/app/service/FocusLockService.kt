package com.focuslock.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focuslock.app.FocusLockApp
import com.focuslock.app.data.database.entities.SessionEntity
import com.focuslock.app.ui.screens.lock.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FocusLockService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var countdownJob: Job? = null
    private var finishing = false

    private val screenReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON) {
                val pm = context?.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                if (pm?.isInteractive == true && FocusLockApp.instance.preferences.isSessionActive()) {
                    val timeSinceLaunch = android.os.SystemClock.uptimeMillis() - FocusAccessibilityService.lastAllowedLaunchTime
                    if (timeSinceLaunch > 3000L) {
                        val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                            )
                        }
                        context?.startActivity(lockIntent)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = android.content.IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenReceiver, filter)
    }

    companion object {
        const val ACTION_START_LOCK = "ACTION_START_LOCK"
        const val ACTION_STOP_LOCK = "ACTION_STOP_LOCK"
        const val ACTION_COMPLETE_LOCK = "ACTION_COMPLETE_LOCK"
        const val NOTIFICATION_ID = 1001

        fun completeService(context: Context) {
            context.startService(Intent(context, FocusLockService::class.java).setAction(ACTION_COMPLETE_LOCK))
        }

        fun startService(context: Context) {
            val intent = Intent(context, FocusLockService::class.java).apply {
                action = ACTION_START_LOCK
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FocusLockService::class.java).apply {
                action = ACTION_STOP_LOCK
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!FocusLockApp.instance.preferences.hasStoredSession()) {
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent?.action) {
            ACTION_STOP_LOCK -> {
                val prefs = FocusLockApp.instance.preferences
                if (prefs.getRemainingFocusMillis() <= 0) finishSession(completed = true)
                else if (!finishing && prefs.useParachute()) finishSession(completed = false)
            }
            ACTION_COMPLETE_LOCK -> {
                if (FocusLockApp.instance.preferences.getRemainingFocusMillis() <= 0) finishSession(completed = true)
            }
            else -> {
                startForegroundWithNotification()
                startCountdown()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildProgressNotification(getRemainingSeconds(), getTotalSeconds())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = serviceScope.launch {
            while (isActive) {
                val app = FocusLockApp.instance
                val remainingMillis = app.preferences.getRemainingFocusMillis()

                if (remainingMillis <= 0) {
                    finishSession(completed = true)
                    break
                }

                val remainingSeconds = (remainingMillis / 1000).toInt()
                val totalSeconds = app.preferences.getSessionTotalDurationMinutes() * 60

                updateNotification(remainingSeconds, totalSeconds)
                delay(1000L)
            }
        }
    }

    private fun updateNotification(remainingSecs: Int, totalSecs: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildProgressNotification(remainingSecs, totalSecs))
    }

    private fun buildProgressNotification(remainingSecs: Int, totalSecs: Int): Notification {
        val app = FocusLockApp.instance
        val minutes = remainingSecs / 60
        val seconds = remainingSecs % 60
        val timeString = String.format(Locale.US, "%02d:%02d", minutes, seconds)
        val bagName = app.preferences.getActiveBagName()

        val lockIntent = Intent(this, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            lockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FocusLockApp.FOCUS_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("BlockIT Active • $bagName")
            .setContentText(if (app.preferences.getResetRemainingMillis() > 0) "Brief reset · focus resumes shortly" else "Time remaining: $timeString")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(totalSecs.coerceAtLeast(1), (totalSecs - remainingSecs).coerceAtLeast(0), false)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun finishSession(completed: Boolean) {
        val app = FocusLockApp.instance
        val prefs = app.preferences
        if (finishing || !prefs.hasStoredSession()) return
        finishing = true
        countdownJob?.cancel()
        val key = prefs.getSessionKey()
        val now = if (completed) prefs.getSessionEndTimeMillis() else System.currentTimeMillis()
        val total = prefs.getSessionTotalDurationMinutes()
        val focused = if (completed) total * 60_000L else
            com.focuslock.app.data.model.FocusProgress.focusedMillis(total, prefs.getRemainingFocusMillis())
        val session = SessionEntity(
            startTime = prefs.getSessionStartMillis(), endTime = now,
            plannedDurationMinutes = total, actualDurationMinutes = (focused / 60_000).toInt(),
            bagId = prefs.getActiveBagId(), bagName = prefs.getActiveBagName(),
            isCompleted = completed, distractionsBlockedCount = prefs.getDistractionsBlockedCount(),
            dateString = java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString(), sessionKey = key,
            task = prefs.getActiveTask(), copingPlan = prefs.getActivePlan(), actualFocusMillis = focused
        )
        // Persist first, then publish the outcome. The unique key prevents duplicate records on recovery.
        serviceScope.launch {
            try {
                kotlinx.coroutines.withContext(Dispatchers.IO) {
                    app.database.sessionDao().insertSession(session)
                    prefs.refreshProgress(app.database.sessionDao().getSessions())
                }
                prefs.finishSessionState(completed, key)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                android.util.Log.e("FocusLock", "Could not save session", error)
                finishing = false
                startCountdown()
            }
        }
    }

    private fun getRemainingSeconds(): Int {
        val app = FocusLockApp.instance
        val remainingMillis = (app.preferences.getRemainingFocusMillis()).coerceAtLeast(0)
        return (remainingMillis / 1000).toInt()
    }

    private fun getTotalSeconds(): Int {
        return FocusLockApp.instance.preferences.getSessionTotalDurationMinutes() * 60
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}
        countdownJob?.cancel()
        serviceScope.coroutineContext[Job]?.cancel()
        // Schedule watchdog alarm if session was stopped unexpectedly
        if (FocusLockApp.instance.preferences.hasStoredSession()) {
            LockWatchdogReceiver.scheduleWatchdog(this, 5000L)
        }
    }
}
