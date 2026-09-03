package com.focuslock.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.focuslock.app.data.database.FocusDatabase
import com.focuslock.app.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FocusLockApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val database by lazy { FocusDatabase.getDatabase(this, applicationScope) }
    val preferences by lazy { AppPreferences(this) }

    companion object {
        const val FOCUS_NOTIFICATION_CHANNEL_ID = "focus_lock_channel"
        const val ROUTINE_NOTIFICATION_CHANNEL_ID = "routine_channel"
        lateinit var instance: FocusLockApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Live Focus Session Channel
            val focusChannel = NotificationChannel(
                FOCUS_NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            // Routine Alerts Channel
            val routineChannel = NotificationChannel(
                ROUTINE_NOTIFICATION_CHANNEL_ID,
                getString(R.string.routine_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.routine_notification_channel_desc)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(focusChannel)
            notificationManager.createNotificationChannel(routineChannel)
        }
    }
}
