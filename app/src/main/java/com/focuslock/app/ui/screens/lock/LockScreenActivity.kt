package com.focuslock.app.ui.screens.lock

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.focuslock.app.ui.theme.FocusLockTheme

class LockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Show over keyguard / lock screen without forcing screen turn-on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        updateAodFlags()

        setContent {
            FocusLockTheme {
                LockScreenContent(
                    onExitLock = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAodFlags()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateAodFlags()
    }

    private fun updateAodFlags() {
        val aod = com.focuslock.app.FocusLockApp.instance.preferences.isAlwaysOnDisplayEnabled()
        if (aod) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Prevent back button from bypassing active lock screen
        // Move task to back or keep on screen
    }
}
