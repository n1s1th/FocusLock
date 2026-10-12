package com.focuslock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.focuslock.app.ui.navigation.AppNavigation
import com.focuslock.app.ui.theme.FocusLockTheme

class MainActivity : ComponentActivity() {

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readRoutineRequest(intent)
    }

    private fun readRoutineRequest(intent: android.content.Intent?) {
        val id = intent?.getLongExtra("routine_id", -1L) ?: -1L
        if (id >= 0) {
            FocusLockApp.instance.preferences.routineRequest.value = id
            intent?.removeExtra("routine_id")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readRoutineRequest(intent)
        if (FocusLockApp.instance.preferences.hasStoredSession()) {
            com.focuslock.app.service.FocusLockService.startService(this)
        }

        setContent {
            FocusLockTheme {
                AppNavigation()
            }
        }
    }
}
