package com.focuslock.app.ui.navigation

import androidx.annotation.DrawableRes
import com.focuslock.app.R

sealed class Screen(val route: String, val title: String, @DrawableRes val iconRes: Int) {
    object Home : Screen("home", "Focus", R.drawable.ic_navigation_session)
    object Bags : Screen("bags", "Bags", R.drawable.ic_navigation_bag)
    object Routines : Screen("routines", "Routines", R.drawable.ic_navigation_control)
    object Stats : Screen("stats", "Analytics", R.drawable.ic_navigation_stats)
    object Settings : Screen("settings", "Settings", R.drawable.ic_account)
    object PermissionsWizard : Screen("permissions_wizard", "Permissions", R.drawable.ic_account)
}
