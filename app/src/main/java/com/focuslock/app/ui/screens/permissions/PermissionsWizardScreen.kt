package com.focuslock.app.ui.screens.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.focuslock.app.FocusLockApp
import com.focuslock.app.ui.screens.home.components.BlockLogoView
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SecondaryMuted
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.util.PermissionUtils
import kotlinx.coroutines.launch

@Composable
fun PermissionsWizardScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })

    // Live permission states
    var hasNotifications by remember { mutableStateOf(PermissionUtils.hasNotificationPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionUtils.hasOverlayPermission(context)) }
    var hasBattery by remember { mutableStateOf(PermissionUtils.isBatteryOptimizationIgnored(context)) }
    var hasUsageStats by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }

    fun refreshPermissions() {
        hasNotifications = PermissionUtils.hasNotificationPermission(context)
        hasAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
        hasOverlay = PermissionUtils.hasOverlayPermission(context)
        hasBattery = PermissionUtils.isBatteryOptimizationIgnored(context)
        hasUsageStats = PermissionUtils.hasUsageStatsPermission(context)
    }

    // Auto-refresh when returning to app from Android settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifications = isGranted
        if (isGranted && pagerState.currentPage == 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    val allGranted = hasNotifications && hasAccessibility && hasOverlay && hasBattery && hasUsageStats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Branding Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BlockLogoView()

            // Skip / Exit button
            TextButton(
                onClick = {
                    FocusLockApp.instance.preferences.setOnboardingCompleted(true)
                    onFinished()
                }
            ) {
                Text(
                    text = if (allGranted) "Done" else "Skip",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SecondaryGray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step Indicator Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 5) {
                val isSelected = pagerState.currentPage == i
                val isStepGranted = when (i) {
                    0 -> hasNotifications
                    1 -> hasAccessibility
                    2 -> hasOverlay
                    3 -> hasBattery
                    4 -> hasUsageStats
                    else -> false
                }

                val pillColor by animateColorAsState(
                    targetValue = when {
                        isStepGranted -> Color(0xFF2E7D32)
                        isSelected -> AccentOrange
                        else -> Color.LightGray.copy(alpha = 0.5f)
                    },
                    animationSpec = tween(300),
                    label = "pillColor"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (isSelected) 36.dp else 14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(pillColor)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(i)
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SETUP STEP ${pagerState.currentPage + 1} OF 5",
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = SecondaryMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5-Interface Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> PermissionStepCard(
                    stepNumber = "Interface 1",
                    title = "Allow Notifications",
                    tagline = "Keep focus timers & alerts visible",
                    description = "BlockIT uses status bar notifications to show your live focus countdown and prevent Android from terminating the focus barrier service.",
                    icon = Icons.Default.Notifications,
                    isGranted = hasNotifications,
                    grantedLabel = "Notifications Enabled",
                    actionLabel = "Grant Notification Permission",
                    onActionClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            PermissionUtils.openNotificationSettings(context)
                        }
                    },
                    onNext = {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                1 -> PermissionStepCard(
                    stepNumber = "Interface 2",
                    title = "Accessibility Service",
                    tagline = "Core distraction blocker & gesture lock",
                    description = "Detects when unallowed apps are opened, enforces your active bag whitelist, and suppresses notification shade pull-downs during active sessions.",
                    icon = Icons.Default.Security,
                    isGranted = hasAccessibility,
                    grantedLabel = "Accessibility Connected",
                    actionLabel = "Open Accessibility Settings",
                    specialNotice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        "⚠️ If setting is greyed out (Restricted Setting):\n1. Long-press BlockIT icon → App Info (ℹ️)\n2. Tap 3 dots (⋮) top-right → 'Allow restricted settings'\n3. Return here & turn Accessibility ON."
                    } else null,
                    secondaryActionLabel = "Open App Info",
                    onSecondaryActionClick = {
                        PermissionUtils.openAppInfoSettings(context)
                    },
                    onActionClick = {
                        PermissionUtils.openAccessibilitySettings(context)
                    },
                    onNext = {
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
                2 -> PermissionStepCard(
                    stepNumber = "Interface 3",
                    title = "Display Over Other Apps",
                    tagline = "Full-screen lock overlay & ambient HUD",
                    description = "Allows BlockIT to display the full-screen digital timer lock HUD and floating ambient clock directly over distracting applications.",
                    icon = Icons.Default.Layers,
                    isGranted = hasOverlay,
                    grantedLabel = "Overlay Permission Granted",
                    actionLabel = "Enable Overlay Permission",
                    onActionClick = {
                        PermissionUtils.openOverlaySettings(context)
                    },
                    onNext = {
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                    }
                )
                3 -> PermissionStepCard(
                    stepNumber = "Interface 4",
                    title = "Battery Optimization",
                    tagline = "Unrestricted background execution",
                    description = "Prevents aggressive Android battery savers (Samsung, Xiaomi, Pixel) from silently killing your active focus timer and watchdog.",
                    icon = Icons.Default.BatteryChargingFull,
                    isGranted = hasBattery,
                    grantedLabel = "Set to Unrestricted",
                    actionLabel = "Disable Battery Optimization",
                    onActionClick = {
                        PermissionUtils.requestIgnoreBatteryOptimization(context)
                    },
                    onNext = {
                        coroutineScope.launch { pagerState.animateScrollToPage(4) }
                    }
                )
                4 -> PermissionStepCard(
                    stepNumber = "Interface 5",
                    title = "Usage Access",
                    tagline = "Screen time & statistics tracking",
                    description = "Enables the Stats screen to calculate your total daily focus hours, blocked distractions count, and app usage breakdown.",
                    icon = Icons.Default.QueryStats,
                    isGranted = hasUsageStats,
                    grantedLabel = "Usage Access Enabled",
                    actionLabel = "Grant Usage Access",
                    onActionClick = {
                        PermissionUtils.openUsageStatsSettings(context)
                    },
                    onNext = {
                        FocusLockApp.instance.preferences.setOnboardingCompleted(true)
                        onFinished()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Controls: Back, Next/Finish
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalPrimary),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Back", fontFamily = GoogleSans, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.width(50.dp))
            }

            if (pagerState.currentPage < 4) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalPrimary),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(text = "Next Step", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = {
                        FocusLockApp.instance.preferences.setOnboardingCompleted(true)
                        onFinished()
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(text = "Start Focus Mode", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Finish",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStepCard(
    stepNumber: String,
    title: String,
    tagline: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    grantedLabel: String,
    actionLabel: String,
    specialNotice: String? = null,
    secondaryActionLabel: String? = null,
    onSecondaryActionClick: (() -> Unit)? = null,
    onActionClick: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBright),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Icon + Status Pill
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) Color(0xFFE8F5E9) else CharcoalPrimary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isGranted) Color(0xFF2E7D32) else CharcoalPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGranted) Color(0xFFE8F5E9) else AccentOrange.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF2E7D32) else AccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGranted) grantedLabel else "Action Required",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isGranted) Color(0xFF2E7D32) else AccentOrange
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = CharcoalPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tagline,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = SecondaryGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = description,
                    fontFamily = GoogleSans,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center
                )

                // Optional Sideload tip box (for Accessibility)
                if (specialNotice != null && !isGranted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = specialNotice,
                            fontFamily = GoogleSans,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFB75E00)
                        )
                    }
                }
            }

            // Bottom Buttons inside the card
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isGranted) {
                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Permission Granted • Continue",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    if (secondaryActionLabel != null && onSecondaryActionClick != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onSecondaryActionClick,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = CharcoalPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = secondaryActionLabel,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
