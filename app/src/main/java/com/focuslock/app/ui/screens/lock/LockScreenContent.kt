package com.focuslock.app.ui.screens.lock

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import com.focuslock.app.service.FocusAccessibilityService
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.focuslock.app.FocusLockApp
import com.focuslock.app.R
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.service.FocusLockService
import com.focuslock.app.ui.screens.home.components.BlockLogoView
import com.focuslock.app.ui.screens.home.components.DigitalTimerDisplay
import com.focuslock.app.ui.screens.home.components.SquareDigit
import com.focuslock.app.ui.screens.lock.components.DoneScreenContent
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.GoogleSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val CardBackgroundDark = Color(0xFF191817)
private val CardMutedText = Color(0xFF8E8D8A)
private val CardAccentHandle = Color(0xFF454341)

@Composable
fun LockScreenContent(
    onExitLock: () -> Unit
) {
    val context = LocalContext.current
    val app = FocusLockApp.instance

    var remainingMillis by remember { mutableLongStateOf(0L) }
    var bagEntity by remember { mutableStateOf<BagEntity?>(null) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showNoParachuteDialog by remember { mutableStateOf(false) }
    var isSessionCompleted by remember { mutableStateOf(false) }

    // Initial session info preserved for Done screen
    val plannedDurationMinutes = remember {
        app.preferences.getSessionTotalDurationMinutes().coerceAtLeast(1)
    }
    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    val todayMinutesFlow = remember {
        app.database.sessionDao().getTotalFocusMinutesForDate(todayDateString)
    }
    val todayMinutesState by todayMinutesFlow.collectAsState(initial = 0)
    val todayFocusMinutes = (todayMinutesState ?: 0).coerceAtLeast(plannedDurationMinutes)
    val currentStreak = remember { app.preferences.getCurrentStreak().coerceAtLeast(1) }

    val totalParachutes by app.preferences.parachutesStateFlow.collectAsState()

    // Screensaver / Ambient Floating Mode State
    var isAmbientMode by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var screenTurnedOffByInactivity by remember { mutableStateOf(false) }

    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reset inactivity timer and restore brightness whenever the screen turns on / activity resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                lastInteractionTime = System.currentTimeMillis()
                screenTurnedOffByInactivity = false
                if (!app.preferences.isAlwaysOnDisplayEnabled()) {
                    activity?.window?.let { w ->
                        val lp = w.attributes
                        if (lp.screenBrightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                            w.attributes = lp
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun registerUserInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        screenTurnedOffByInactivity = false
        if (isAmbientMode) {
            isAmbientMode = false
        }
        // Restore screen brightness if dimmed due to 30s timeout
        if (!app.preferences.isAlwaysOnDisplayEnabled()) {
            activity?.window?.let { w ->
                val lp = w.attributes
                if (lp.screenBrightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                    lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    w.attributes = lp
                }
            }
        }
    }

    // Inactivity detection & 30-second screen timeout
    LaunchedEffect(isAmbientMode, isSessionCompleted) {
        if (isSessionCompleted) return@LaunchedEffect
        while (true) {
            delay(1000L)
            val elapsed = System.currentTimeMillis() - lastInteractionTime
            if (!isAmbientMode && elapsed >= 10000L) {
                isAmbientMode = true
            }
            // If Always On Display is disabled, physically turn off the display after 30 seconds of inactivity
            if (!app.preferences.isAlwaysOnDisplayEnabled() && elapsed >= 30000L && !screenTurnedOffByInactivity) {
                screenTurnedOffByInactivity = true
                val turnedOff = FocusAccessibilityService.turnScreenOff()
                if (!turnedOff) {
                    // Fallback dimming if accessibility service is unavailable
                    activity?.window?.let { w ->
                        val lp = w.attributes
                        if (lp.screenBrightness != 0.0f) {
                            lp.screenBrightness = 0.0f
                            w.attributes = lp
                        }
                    }
                }
            }
        }
    }

    // Live countdown loop
    LaunchedEffect(Unit) {
        val bagId = app.preferences.getActiveBagId()
        val loadedBag = app.database.bagDao().getBagById(bagId)
        bagEntity = loadedBag
        val nonBlank = (loadedBag?.allowedPackages ?: emptyList()).filter { it.isNotBlank() }
        app.preferences.setActiveAllowedPackages(nonBlank)
        FocusAccessibilityService.updateAllowedPackages(nonBlank.toSet())

        while (true) {
            val endMillis = app.preferences.getSessionEndTimeMillis()
            val remaining = endMillis - System.currentTimeMillis()
            if (remaining <= 0) {
                // Focus session successfully completed! Show DONE screen
                FocusLockService.stopService(context)
                isSessionCompleted = true
                break
            }
            remainingMillis = remaining
            delay(250L)
        }
    }

    // If session has finished successfully, show the DONE Completion Screen
    if (isSessionCompleted) {
        DoneScreenContent(
            sessionMinutes = plannedDurationMinutes,
            todayMinutes = todayFocusMinutes,
            streak = currentStreak,
            onDoneClick = onExitLock
        )
        return
    }

    val totalSeconds = (remainingMillis / 1000).toInt()
    val totalMinutes = totalSeconds / 60
    val displayHours = totalMinutes / 60
    val displayMinutes = totalMinutes % 60
    val displaySeconds = totalSeconds % 60

    // Formatted Expected End Time
    val endTimeMillis = app.preferences.getSessionEndTimeMillis()
    val formattedEndTime = remember(endTimeMillis) {
        if (endTimeMillis > 0) {
            SimpleDateFormat("h:mm a", Locale.US).format(Date(endTimeMillis))
        } else {
            "--:--"
        }
    }

    // Infinite live floating animations for remaining time displays in ambient mode (Lock Screen 2)
    val infiniteTransition = rememberInfiniteTransition(label = "liveTimerFloating")

    // Main timer floating with increased radius (visible orbital drift exclusively on Lock Screen 2)
    val ambientTimerFloatY by infiniteTransition.animateFloat(
        initialValue = -28f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTimerFloatY"
    )
    val ambientTimerFloatX by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTimerFloatX"
    )

    // Seconds card live counter-phase float for organic fluidity with increased radius
    val ambientSecFloatY by infiniteTransition.animateFloat(
        initialValue = 22f,
        targetValue = -22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientSecFloatY"
    )
    val ambientSecFloatX by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = -16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientSecFloatX"
    )

    // ONLY active on Lock Screen 2 (ambient mode). On Lock Screen 1 (active mode with all icons), multiplier is 0 (completely stationary)
    val ambientDriftMultiplier by animateFloatAsState(
        targetValue = if (isAmbientMode) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "ambientDriftMultiplier"
    )

    val mainTimerFloatX = ambientTimerFloatX * ambientDriftMultiplier
    val mainTimerFloatY = ambientTimerFloatY * ambientDriftMultiplier
    val secTimerFloatX = ambientSecFloatX * ambientDriftMultiplier
    val secTimerFloatY = ambientSecFloatY * ambientDriftMultiplier

    // Smooth fade for non-timing UI elements in ambient mode
    val ambientAlpha by animateFloatAsState(
        targetValue = if (isAmbientMode) 0f else 1f,
        animationSpec = tween(450),
        label = "ambientAlpha"
    )

    // Animated container background and digit colors for timing cards
    val timerCardBg by animateColorAsState(
        targetValue = if (isAmbientMode) Color.Black else CardBackgroundDark,
        animationSpec = tween(450),
        label = "timerCardBg"
    )
    val timerDigitColor by animateColorAsState(
        targetValue = if (isAmbientMode) Color(0xFF6E6A66) else Color.White,
        animationSpec = tween(450),
        label = "timerDigitColor"
    )
    val timerColonColor by animateColorAsState(
        targetValue = if (isAmbientMode) Color(0xFF4A4744) else CardMutedText,
        animationSpec = tween(450),
        label = "timerColonColor"
    )
    val timerBorder = if (isAmbientMode) BorderStroke(1.dp, Color(0xFF262422)) else null

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) {
                            registerUserInteraction()
                        }
                    }
                }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Ambient Wake-Up Interceptor Overlay
        // When in ambient mode, this full-screen top-layer overlay consumes the blind touch
        // completely, waking the screen without allowing ANY touch event to pass through
        // to underlying allowed apps, dialer, parachute, or slide-to-exit.
        if (isAmbientMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(999f)
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                                registerUserInteraction()
                            }
                        }
                    }
            )
        }

        val totalH = maxHeight
        val clockCardHeight = (totalH * 0.245f).coerceIn(175.dp, 245.dp)
        val clockBlockSize = (clockCardHeight * 0.098f).coerceIn(17.dp, 24.dp)

        val middleSectionHeight = (totalH * 0.44f).coerceIn(290.dp, 430.dp)
        val rowGap = 8.dp
        val pillHeight = (middleSectionHeight * 0.14f).coerceIn(42.dp, 54.dp)
        val rainCardHeight = (pillHeight * 2) + rowGap
        val secCardHeight = middleSectionHeight - (pillHeight * 2) - (rowGap * 2)
        val secBlockSize = (secCardHeight * 0.095f).coerceIn(15.dp, 21.dp)

        val sliderHeight = (totalH * 0.075f).coerceIn(58.dp, 66.dp)
        val dockHeight = (totalH * 0.065f).coerceIn(48.dp, 54.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP LOGO (With comfortable top margin)
            BlockLogoView(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 4.dp)
                    .graphicsLayer { alpha = ambientAlpha },
                pixelSize = 4.2.dp,
                color = Color.White,
                pulseColor = AccentOrange
            )

            // 2. BIG DIGITAL TIMER CARD (HH:MM / MM:SS)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(clockCardHeight)
                    .graphicsLayer {
                        translationX = mainTimerFloatX
                        translationY = mainTimerFloatY
                    },
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = timerCardBg),
                border = timerBorder,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DigitalTimerDisplay(
                        hours = displayHours,
                        minutes = displayMinutes,
                        blockSize = clockBlockSize,
                        digitColor = timerDigitColor,
                        colonColor = timerColonColor
                    )
                }
            }

            // 3. MIDDLE 2-COLUMN SECTION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(middleSectionHeight),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LEFT COLUMN (Seconds Tile + Ends Pill + Parachutes Pill)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(rowGap)
                ) {
                    // Seconds Tile - Floating in ambient mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(secCardHeight)
                            .graphicsLayer {
                                translationX = secTimerFloatX
                                translationY = secTimerFloatY
                            },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = timerCardBg),
                        border = timerBorder,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val s1 = displaySeconds / 10
                                val s2 = displaySeconds % 10
                                SquareDigit(digit = s1, blockSize = secBlockSize, color = timerDigitColor)
                                SquareDigit(digit = s2, blockSize = secBlockSize, color = timerDigitColor)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "SEC",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isAmbientMode) Color(0xFF4A4744) else CardMutedText,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    // Ends Time Pill
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(pillHeight)
                            .graphicsLayer { alpha = ambientAlpha },
                        shape = RoundedCornerShape(pillHeight / 2),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ENDS",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = CardMutedText,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedEndTime,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Parachutes Count Pill
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(pillHeight)
                            .graphicsLayer { alpha = ambientAlpha },
                        shape = RoundedCornerShape(pillHeight / 2),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(CardAccentHandle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_parachute),
                                    contentDescription = "Parachutes",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(11.dp)
                                        .rotate(-25f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$totalParachutes",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // RIGHT COLUMN (+15 min card + RAIN focus sound card)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer { alpha = ambientAlpha },
                    verticalArrangement = Arrangement.spacedBy(rowGap)
                ) {
                    // +15 min Extension Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(secCardHeight)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable {
                                if (!isAmbientMode) {
                                    val currentEnd = app.preferences.getSessionEndTimeMillis()
                                    val newEnd = currentEnd + (15 * 60 * 1000L)
                                    val totalMin = app.preferences.getSessionTotalDurationMinutes() + 15
                                    val currentPkgs = (bagEntity?.allowedPackages ?: emptyList()).filter { it.isNotBlank() }
                                    app.preferences.startSession(
                                        endTimeMillis = newEnd,
                                        durationMinutes = totalMin,
                                        bagId = app.preferences.getActiveBagId(),
                                        bagName = app.preferences.getActiveBagName(),
                                        allowedPackages = currentPkgs
                                    )
                                }
                            },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "+15",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color.White
                            )
                            Text(
                                text = "min",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = CardMutedText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(CardAccentHandle)
                                    )
                                }
                            }
                        }
                    }

                    // Ambient Rain / Focus Sound Tile
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rainCardHeight),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.5.dp)
                                            .background(CardMutedText)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "RAIN",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = CardMutedText,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.5.dp)
                                            .background(CardMutedText)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. SLIDE TO EXIT (Smooth, directly below middle section)
            SlideToExitTrack(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sliderHeight)
                    .graphicsLayer { alpha = ambientAlpha },
                enabled = !isAmbientMode,
                onTriggerExit = {
                    if (totalParachutes > 0) {
                        showEmergencyDialog = true
                    } else {
                        showNoParachuteDialog = true
                    }
                }
            )

            // 5. BOTTOM ALLOWED APPS DOCK & EMERGENCY DIALER (Centered side-by-side)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = ambientAlpha }
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val allowedPackages = (bagEntity?.allowedPackages ?: emptyList()).filter { it.isNotBlank() }
                val maxSlots = if (allowedPackages.size <= 3) 3 else allowedPackages.size.coerceAtMost(5)

                Row(
                    modifier = Modifier
                        .height(dockHeight)
                        .clip(RoundedCornerShape(dockHeight / 2))
                        .background(CardBackgroundDark)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until maxSlots) {
                        val pkg = allowedPackages.getOrNull(i)
                        if (!pkg.isNullOrBlank()) {
                            AllowedAppCircle(
                                packageName = pkg,
                                onClick = {
                                    if (!isAmbientMode) {
                                        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                                        if (intent != null) {
                                            launchAllowedApp(context, intent, pkg)
                                        }
                                    }
                                }
                            )
                        } else {
                            EmptyAllowedSlotCircle()
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // White Emergency Phone Button
                Box(
                    modifier = Modifier
                        .size(dockHeight)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            if (!isAmbientMode) {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
                                val dialerPkg = context.packageManager.resolveActivity(dialIntent, 0)?.activityInfo?.packageName ?: "com.google.android.dialer"
                                launchAllowedApp(context, dialIntent, dialerPkg)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_phone),
                        contentDescription = "Emergency Phone",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Parachute Emergency Confirmation Dialog
        if (showEmergencyDialog) {
            AlertDialog(
                onDismissRequest = { showEmergencyDialog = false },
                containerColor = CardBackgroundDark,
                title = {
                    Text(
                        text = "Use 1 Parachute?",
                        fontFamily = GoogleSans,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "You have $totalParachutes parachute(s) available. Using 1 will end this focus session immediately without penalty.",
                        fontFamily = GoogleSans,
                        color = CardMutedText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEmergencyDialog = false
                            val success = app.preferences.useParachute()
                            if (success) {
                                FocusLockService.stopService(context)
                                onExitLock()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Text(
                            text = "Use Parachute",
                            fontFamily = GoogleSans,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmergencyDialog = false }) {
                        Text(
                            text = "Keep Focusing",
                            fontFamily = GoogleSans,
                            color = Color.White
                        )
                    }
                }
            )
        }

        // No Parachutes Available Dialog
        if (showNoParachuteDialog) {
            AlertDialog(
                onDismissRequest = { showNoParachuteDialog = false },
                containerColor = CardBackgroundDark,
                title = {
                    Text(
                        text = "No Parachutes Available",
                        fontFamily = GoogleSans,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "You don't have any parachutes available right now. Wait for your weekly free parachute or request one on the Bags screen.",
                        fontFamily = GoogleSans,
                        color = CardMutedText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showNoParachuteDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CardAccentHandle)
                    ) {
                        Text(
                            text = "Got It",
                            fontFamily = GoogleSans,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}

// Previous version SlideToExitTrack: Handle on left, slides left-to-right
@Composable
fun SlideToExitTrack(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTriggerExit: () -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val onTriggerExitState by rememberUpdatedState(onTriggerExit)

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    var hasTriggeredThresholdHaptic by remember { mutableStateOf(false) }

    val containerHeight = 64.dp
    val handleWidth = 62.dp
    val handleHeight = 48.dp
    val trackPadding = 8.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .clip(RoundedCornerShape(32.dp))
            .background(CardBackgroundDark)
            .padding(trackPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        val maxDragPx = with(density) {
            (maxWidth - handleWidth - trackPadding * 2).toPx().coerceAtLeast(1f)
        }

        val currentOffset = if (isDragging) dragOffsetX else animOffsetX.value
        val progress = (currentOffset / maxDragPx).coerceIn(0f, 1f)

        // Background label (fades as handle slides over)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (1f - progress * 1.5f).coerceIn(0f, 1f) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SLIDE TO EXIT",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CardMutedText,
                letterSpacing = 2.sp
            )
        }

        // Draggable parachute knob on the left, sliding rightwards
        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffset.roundToInt(), 0) }
                .width(handleWidth)
                .height(handleHeight)
                .clip(RoundedCornerShape(24.dp))
                .background(CardAccentHandle)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffsetX = animOffsetX.value
                            hasTriggeredThresholdHaptic = false
                        },
                        onDragEnd = {
                            isDragging = false
                            scope.launch {
                                if (dragOffsetX >= maxDragPx * 0.75f) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    animOffsetX.snapTo(dragOffsetX)
                                    animOffsetX.animateTo(maxDragPx, tween(120))
                                    onTriggerExitState()
                                    animOffsetX.snapTo(0f)
                                    dragOffsetX = 0f
                                } else {
                                    animOffsetX.snapTo(dragOffsetX)
                                    animOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                    dragOffsetX = 0f
                                }
                                hasTriggeredThresholdHaptic = false
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch {
                                animOffsetX.snapTo(dragOffsetX)
                                animOffsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                                dragOffsetX = 0f
                                hasTriggeredThresholdHaptic = false
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = (dragOffsetX + dragAmount).coerceIn(0f, maxDragPx)
                            dragOffsetX = next

                            if (next >= maxDragPx * 0.75f) {
                                if (!hasTriggeredThresholdHaptic) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    hasTriggeredThresholdHaptic = true
                                }
                            } else {
                                hasTriggeredThresholdHaptic = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_parachute),
                contentDescription = "Parachute Exit",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(-25f)
            )
        }
    }
}

// Previous version AllowedAppCircle: size 34dp
@Composable
fun AllowedAppCircle(
    packageName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    var appIcon by remember { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
    var appLabel by remember { mutableStateOf("") }

    LaunchedEffect(packageName) {
        try {
            val info = pm.getApplicationInfo(packageName, 0)
            appIcon = pm.getApplicationIcon(info)
            appLabel = pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            appLabel = packageName.take(2).uppercase()
        }
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CardAccentHandle)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val bitmap = remember(appIcon) {
            try {
                appIcon?.toBitmap(80, 80)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = appLabel,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = appLabel.take(1).uppercase(),
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

// Previous version EmptyAllowedSlotCircle: size 32dp
@Composable
fun EmptyAllowedSlotCircle() {
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 1.2.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw subtle dashed/dotted circle
        val dotCount = 12
        for (i in 0 until dotCount) {
            val angle = (i * 360f / dotCount) * (Math.PI / 180f).toFloat()
            val dx = center.x + radius * kotlin.math.cos(angle)
            val dy = center.y + radius * kotlin.math.sin(angle)
            drawCircle(
                color = CardMutedText.copy(alpha = 0.6f),
                radius = 1.dp.toPx(),
                center = Offset(dx, dy)
            )
        }

        // Draw small + inside
        val plusSize = 3.5.dp.toPx()
        drawLine(
            color = CardMutedText.copy(alpha = 0.6f),
            start = Offset(center.x - plusSize, center.y),
            end = Offset(center.x + plusSize, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = CardMutedText.copy(alpha = 0.6f),
            start = Offset(center.x, center.y - plusSize),
            end = Offset(center.x, center.y + plusSize),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun launchAllowedApp(context: Context, intent: Intent, packageName: String) {
    FocusAccessibilityService.notifyAppLaunching(packageName)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    val activity = context.findActivity()
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null && keyguardManager?.isKeyguardLocked == true) {
        keyguardManager.requestDismissKeyguard(activity, object : KeyguardManager.KeyguardDismissCallback() {
            override fun onDismissSucceeded() {
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("LockScreen", "Failed to launch allowed app: $packageName", e)
                }
            }
            override fun onDismissError() {
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("LockScreen", "Failed to launch allowed app: $packageName", e)
                }
            }
            override fun onDismissCancelled() {
                Log.d("LockScreen", "Keyguard dismiss cancelled by user")
            }
        })
    } else {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("LockScreen", "Failed to launch allowed app: $packageName", e)
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

