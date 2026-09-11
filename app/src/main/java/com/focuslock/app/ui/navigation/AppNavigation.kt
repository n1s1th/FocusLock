package com.focuslock.app.ui.navigation

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focuslock.app.FocusLockApp
import com.focuslock.app.ui.screens.bags.BagsScreen
import com.focuslock.app.ui.screens.home.HomeScreen
import com.focuslock.app.ui.screens.lock.LockScreenActivity
import com.focuslock.app.ui.screens.permissions.PermissionsWizardScreen
import com.focuslock.app.ui.screens.routines.RoutinesScreen
import com.focuslock.app.ui.screens.settings.SettingsScreen
import com.focuslock.app.ui.screens.stats.StatsScreen
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SurfaceBright
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isOnboardingDone = FocusLockApp.instance.preferences.isOnboardingCompleted()
    val startDest = if (isOnboardingDone) "main_pager" else Screen.PermissionsWizard.route

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Screen.PermissionsWizard.route) {
            PermissionsWizardScreen(
                onFinished = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("main_pager") {
                            popUpTo(Screen.PermissionsWizard.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("main_pager") {
            MainPagerScreen(
                onNavigateToLock = {
                    val lockIntent = Intent(context, LockScreenActivity::class.java)
                    context.startActivity(lockIntent)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPermissionsWizard = {
                    navController.navigate(Screen.PermissionsWizard.route)
                }
            )
        }
    }
}

@Composable
fun MainPagerScreen(
    onNavigateToLock: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val navItems = listOf(
        Screen.Home,
        Screen.Bags,
        Screen.Routines,
        Screen.Stats
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { navItems.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 10.dp, bottom = 12.dp),
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
                    navItems.forEachIndexed { index, screen ->
                        val isSelected = pagerState.currentPage == index

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) SurfaceBright else CharcoalPrimary,
                            animationSpec = tween(durationMillis = 200),
                            label = "navIconColor"
                        )
                        val pillBgColor by animateColorAsState(
                            targetValue = if (isSelected) CharcoalPrimary else Color.Transparent,
                            animationSpec = tween(durationMillis = 200),
                            label = "navPillBgColor"
                        )

                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(pillBgColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = index,
                                            animationSpec = tween(
                                                durationMillis = 280,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            beyondViewportPageCount = 3, // Cache adjacent screens for instantaneous 120 FPS swiping
            key = { page -> navItems[page].route }
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    onNavigateToLock = onNavigateToLock,
                    onNavigateToSettings = onNavigateToSettings
                )
                1 -> BagsScreen(
                    onNavigateToSettings = onNavigateToSettings
                )
                2 -> RoutinesScreen(
                    onNavigateToSettings = onNavigateToSettings
                )
                3 -> StatsScreen(
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}
