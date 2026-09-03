package com.focuslock.app.ui.screens.lock

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.focuslock.app.ui.theme.FocusLockTheme

class LockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over keyguard / lock screen and keep screen on if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        setContent {
            FocusLockTheme {
                LockScreenContent(
                    onExitLock = { finish() }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Prevent back button from bypassing active lock screen
        // Move task to back or keep on screen
    }
}
