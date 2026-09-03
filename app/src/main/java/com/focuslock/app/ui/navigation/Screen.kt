package com.focuslock.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Focus", Icons.Default.HourglassTop)
    object Bags : Screen("bags", "Bags", Icons.Default.Workspaces)
    object Routines : Screen("routines", "Routines", Icons.Default.Schedule)
    object Stats : Screen("stats", "Analytics", Icons.Default.Analytics)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
