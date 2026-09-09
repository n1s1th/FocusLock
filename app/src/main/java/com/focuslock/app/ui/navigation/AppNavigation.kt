package com.focuslock.app.ui.navigation

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SurfaceBright

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navItems = listOf(
        Screen.Home,
        Screen.Bags,
        Screen.Routines,
        Screen.Stats
    )

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Floating Pill Navigation Bar
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(SurfaceBright)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentDestination?.route == screen.route

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) SurfaceBright else CharcoalPrimary,
                            label = "navIconColor"
                        )

                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) CharcoalPrimary else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = screen.iconRes),
                                    contentDescription = screen.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )

                                // Orange accent badge dot on top-right of the Bags icon
                                if (screen == Screen.Bags) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(5.dp)
                                            .background(AccentOrange)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToLock = {
                        val lockIntent = Intent(context, LockScreenActivity::class.java)
                        context.startActivity(lockIntent)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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
