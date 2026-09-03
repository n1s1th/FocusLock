package com.focuslock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.focuslock.app.ui.navigation.AppNavigation
import com.focuslock.app.ui.theme.FocusLockTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FocusLockTheme {
                AppNavigation()
            }
        }
    }
}
