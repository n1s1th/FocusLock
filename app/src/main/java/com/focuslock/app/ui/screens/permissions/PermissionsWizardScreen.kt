package com.focuslock.app.ui.screens.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.util.PermissionUtils
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val TrackContainerBg = Color(0xFFE3E1DC)
private val LightCardBg = Color(0xFFEAE8E3)
private val DotPeachActive = AccentOrange
private val DotPeachInactive = Color(0xFFF7BAA6)

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

    // Auto-refresh when returning from System Settings
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

    // Notification launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val isStep1Granted = hasNotifications
    val isStep2Granted = hasAccessibility
    val isStep3Granted = hasOverlay
    val isStep4Granted = hasBattery
    val isStep5Granted = hasUsageStats

    fun completeOnboarding() {
        FocusLockApp.instance.preferences.setOnboardingCompleted(true)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER: LOGO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                BlockLogoView(
                    pixelSize = 4.8.dp,
                    color = CharcoalPrimary,
                    pulseColor = AccentOrange
                )
            }

            // 2. TOP WHITE CARD: PIXEL STEP DIGITS (01 - 05) & STEP DOTS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceBright),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Step Digits (e.g. 0 1, 0 2, 0 3, 0 4, 0 5)
                    val currentStepNumber = pagerState.currentPage + 1
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepHollowZero(blockSize = 18.dp, color = CharcoalPrimary)
                        StepPillarDigit(digit = currentStepNumber, blockSize = 18.dp, color = CharcoalPrimary)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5 Step Indicator Square Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 5) {
                            val isActive = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(if (isActive) DotPeachActive else DotPeachInactive)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(i)
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. MIDDLE SECTION: PAGER FOR 5 STEPS + VERTICAL SLIDER TRACK ON RIGHT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LEFT SIDE: HORIZONTAL PAGER (~78% width)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) { page ->
                    when (page) {
                        0 -> StepContentCard(
                            icon = Icons.Default.Notifications,
                            title = "NOTIFICATIONS",
                            explanation = "Grant it to alert you when a session is active and how much time is left until the block ends.",
                            footnote = "Only for the block: no sound, no vibration, no ads."
                        )
                        1 -> StepContentCard(
                            icon = Icons.Default.Security,
                            title = "ACCESSIBILITY",
                            explanation = "Grant it to strictly block unwhitelisted apps and dismiss distracting notifications during focus sessions.",
                            footnote = "Never monitors personal data, passwords, or keystrokes.",
                            sideloadCallout = "Tip: If disabled on Android 13+, open App Info → 3 dots → Allow restricted settings.",
                            onOpenAppInfo = { PermissionUtils.openAppInfoSettings(context) }
                        )
                        2 -> StepContentCard(
                            icon = Icons.Default.Layers,
                            title = "DISPLAY OVER APPS",
                            explanation = "Grant it to display the full-screen distraction barrier immediately over blacklisted applications.",
                            footnote = "Only appears when opening blocked apps during focus."
                        )
                        3 -> StepContentCard(
                            icon = Icons.Default.BatteryChargingFull,
                            title = "BATTERY OPTIMIZATION",
                            explanation = "Grant unrestricted battery usage so Android does not kill the background focus countdown timer.",
                            footnote = "Zero background battery drain; runs purely on timer."
                        )
                        4 -> StepContentCard(
                            icon = Icons.Default.QueryStats,
                            title = "USAGE ACCESS",
                            explanation = "Grant usage stats access to calculate your focus analytics and track your productivity streaks.",
                            footnote = "All statistics stay 100% private and stored on device."
                        )
                    }
                }

                // RIGHT SIDE: VERTICAL SLIDER TRACK (~22% width)
                VerticalStepSliderTrack(
                    currentPage = pagerState.currentPage,
                    pageOffsetFraction = pagerState.currentPageOffsetFraction,
                    modifier = Modifier
                        .width(66.dp)
                        .fillMaxHeight()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. BOTTOM ACTION BUTTON ("Grant" / "Continue" / "Start Focus Mode ✓")
            val currentStep = pagerState.currentPage
            val isCurrentGranted = when (currentStep) {
                0 -> isStep1Granted
                1 -> isStep2Granted
                2 -> isStep3Granted
                3 -> isStep4Granted
                4 -> isStep5Granted
                else -> false
            }

            val buttonText = when {
                currentStep == 4 && isCurrentGranted -> "Start Focus Mode ✓"
                isCurrentGranted -> "Continue"
                else -> "Grant"
            }

            Button(
                onClick = {
                    if (!isCurrentGranted) {
                        // Execute Grant Intent for current step
                        when (currentStep) {
                            0 -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    PermissionUtils.openNotificationSettings(context)
                                }
                            }
                            1 -> PermissionUtils.openAccessibilitySettings(context)
                            2 -> PermissionUtils.openOverlaySettings(context)
                            3 -> PermissionUtils.requestIgnoreBatteryOptimization(context)
                            4 -> PermissionUtils.openUsageStatsSettings(context)
                        }
                    } else {
                        // Advance to next step or complete onboarding
                        if (currentStep < 4) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentStep + 1)
                            }
                        } else {
                            completeOnboarding()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CharcoalPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = buttonText,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. FOOTER: PRIVACY POLICY · TERMS OF SERVICE
            Text(
                text = "Privacy Policy   ·   Terms of Service",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = SecondaryGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

/**
 * Left-side content card for each permission step
 */
@Composable
private fun StepContentCard(
    icon: ImageVector,
    title: String,
    explanation: String,
    footnote: String,
    sideloadCallout: String? = null,
    onOpenAppInfo: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Step Header Row: Icon + Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CharcoalPrimary,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = title,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                color = CharcoalPrimary.copy(alpha = 0.85f)
            )
        }

        // Light gray rounded container with explanation & footnote
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = explanation,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = CharcoalPrimary.copy(alpha = 0.8f)
                    )

                    if (sideloadCallout != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (onOpenAppInfo != null) Modifier.clickable { onOpenAppInfo() } else Modifier
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = sideloadCallout,
                                    fontFamily = GoogleSans,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = CharcoalPrimary
                                )
                            }
                        }
                    }
                }

                // Footnote with orange square bullet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(AccentOrange)
                    )
                    Text(
                        text = footnote,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = SecondaryGray
                    )
                }
            }
        }
    }
}

/**
 * Right-side vertical track with 3-line handle and dotted line
 */
@Composable
private fun VerticalStepSliderTrack(
    currentPage: Int,
    pageOffsetFraction: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = TrackContainerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val totalTrackHeight = maxHeight
            val handleHeight = 44.dp
            val handleWidth = 52.dp

            val density = LocalDensity.current
            val maxTravelPx = with(density) { (totalTrackHeight - handleHeight).toPx() }

            // Progress between 0f (Step 1) and 1f (Step 5)
            val currentProgress = ((currentPage + pageOffsetFraction).coerceIn(0f, 4f) / 4f)
            val animatedOffsetY = (maxTravelPx * currentProgress).roundToInt()

            // 1. Dotted vertical track & bottom stop dash
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                repeat(9) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(CharcoalPrimary.copy(alpha = 0.28f))
                    )
                }
                // Bottom stop horizontal dash
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CharcoalPrimary.copy(alpha = 0.35f))
                )
            }

            // 2. Charcoal Slider Handle with 3 horizontal grip lines
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, animatedOffsetY) }
                    .width(handleWidth)
                    .height(handleHeight)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalPrimary),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hollow block zero (3 columns x 5 rows) matching screenshot
 */
@Composable
private fun StepHollowZero(
    blockSize: Dp = 18.dp,
    color: Color = CharcoalPrimary,
    modifier: Modifier = Modifier
) {
    val width = blockSize * 3
    val height = blockSize * 5

    Canvas(modifier = modifier.size(width, height)) {
        val pxBlock = blockSize.toPx()
        // Outer border is filled, center is hollow
        for (r in 0 until 5) {
            for (c in 0 until 3) {
                val isCenterHollow = (c == 1 && (r == 1 || r == 2 || r == 3))
                if (!isCenterHollow) {
                    drawRect(
                        color = color,
                        topLeft = Offset(c * pxBlock, r * pxBlock),
                        size = Size(pxBlock, pxBlock)
                    )
                }
            }
        }
    }
}

/**
 * Step pillar digit (1 to 5)
 */
@Composable
private fun StepPillarDigit(
    digit: Int,
    blockSize: Dp = 18.dp,
    color: Color = CharcoalPrimary,
    modifier: Modifier = Modifier
) {
    val width = blockSize * 3
    val height = blockSize * 5

    // 3x5 matrices for 1 to 5
    val matrix = when (digit) {
        1 -> listOf(
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0
        )
        2 -> listOf(
            1, 1, 1,
            0, 0, 1,
            1, 1, 1,
            1, 0, 0,
            1, 1, 1
        )
        3 -> listOf(
            1, 1, 1,
            0, 0, 1,
            1, 1, 1,
            0, 0, 1,
            1, 1, 1
        )
        4 -> listOf(
            1, 0, 1,
            1, 0, 1,
            1, 1, 1,
            0, 0, 1,
            0, 0, 1
        )
        5 -> listOf(
            1, 1, 1,
            1, 0, 0,
            1, 1, 1,
            0, 0, 1,
            1, 1, 1
        )
        else -> listOf(
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0
        )
    }

    Canvas(modifier = modifier.size(width, height)) {
        val pxBlock = blockSize.toPx()
        for (r in 0 until 5) {
            for (c in 0 until 3) {
                if (matrix[r * 3 + c] == 1) {
                    drawRect(
                        color = color,
                        topLeft = Offset(c * pxBlock, r * pxBlock),
                        size = Size(pxBlock, pxBlock)
                    )
                }
            }
        }
    }
}
