package com.focuslock.app.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focuslock.app.ui.screens.bags.BagsScreen
import com.focuslock.app.ui.screens.home.HomeScreen
import com.focuslock.app.ui.screens.lock.LockScreenActivity
import com.focuslock.app.ui.screens.routines.RoutinesScreen
import com.focuslock.app.ui.screens.settings.SettingsScreen
import com.focuslock.app.ui.screens.stats.StatsScreen
import com.focuslock.app.ui.theme.BackgroundDark
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SurfaceDark
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Home,
        Screen.Bags,
        Screen.Routines,
        Screen.Stats,
        Screen.Settings
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark
            ) {
                items.forEach { screen ->
                    val isSelected = currentDestination?.route == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryIndigo,
                            selectedTextColor = PrimaryIndigo,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToLock = {
                        val lockIntent = Intent(context, LockScreenActivity::class.java)
                        context.startActivity(lockIntent)
                    }
                )
            }
            composable(Screen.Bags.route) {
                BagsScreen()
            }
            composable(Screen.Routines.route) {
                RoutinesScreen()
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
