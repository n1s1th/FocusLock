package com.focuslock.app.ui.screens.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.FocusLockApp
import com.focuslock.app.ui.screens.home.components.WeeklyFocusCard
import com.focuslock.app.ui.screens.home.components.HabitGoalDialog
import com.focuslock.app.ui.screens.home.components.SessionPlanDialog
import com.focuslock.app.ui.screens.home.components.ExitReasonDialog
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.TextButton
import com.focuslock.app.R
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.service.FocusAccessibilityService
import com.focuslock.app.ui.screens.home.components.BlockLogoView
import com.focuslock.app.ui.screens.home.components.DigitalTimerDisplay
import com.focuslock.app.ui.screens.home.components.MainModeSwitch
import com.focuslock.app.ui.screens.reels.ReelBlockDashboardScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.focuslock.app.ui.screens.home.components.SlideToStart
import com.focuslock.app.ui.screens.home.components.TopHeaderBar
import com.focuslock.app.ui.screens.home.components.VerticalRulerPicker
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SecondaryMuted
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.ui.theme.SurfaceVariant

@Composable
fun HomeScreen(
    onNavigateToLock: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppConfig: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    val parachuteCount by viewModel.parachuteCount.collectAsState()

    val preferences = FocusLockApp.instance.preferences
    val sessions by viewModel.sessions.collectAsState()
    val bags by viewModel.bags.collectAsState()
    val revision by viewModel.habitRevision.collectAsState()
    var showPlan by remember { mutableStateOf(false) }
    var showGoal by remember { mutableStateOf(false) }
    var isAccessibilityOn by remember { mutableStateOf(FocusAccessibilityService.isServiceRunning) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) isAccessibilityOn = FocusAccessibilityService.isServiceRunning
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    if (showPlan) SessionPlanDialog(selectedMinutes, bags, preferences.getSelectedBagId(), preferences,
        onDismiss = { showPlan = false }, canStart = isAccessibilityOn, onStart = { bag, task, plan ->
            if (viewModel.startFocusSession(bag, task, plan)) {
                preferences.setSelectedBagId(bag.id)
                showPlan = false
            }
        })
    if (showGoal) HabitGoalDialog(preferences) { showGoal = false }
    val pending = sessions.find { it.sessionKey == preferences.getPendingReasonKey() && !it.isCompleted }
    if (pending != null && !isSessionActive) ExitReasonDialog(pending) { viewModel.recordExitReason(pending.sessionKey, it) }
    var isReelMode by remember { mutableStateOf(false) }

    // Fix 4: Use LaunchedEffect to avoid side-effects in composition body
    LaunchedEffect(isSessionActive) {
        if (isSessionActive) {
            onNavigateToLock()
        }
    }

    val hours = selectedMinutes / 60
    val minutes = selectedMinutes % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HEADER BAR
        TopHeaderBar(
            streakCount = viewModel.getStreak(),
            parachuteCount = parachuteCount,
            onProfileClick = onNavigateToSettings
        )

        MainModeSwitch(
            isReelMode = isReelMode,
            onModeChange = { isReelMode = it }
        )

        if (isReelMode) {
            ReelBlockDashboardScreen(onAppClick = onNavigateToAppConfig)
        } else {

        WeeklyFocusCard(sessions, preferences) { showGoal = true }
        val last = sessions.firstOrNull()
        if (last != null && !last.isCompleted && last.exitReason in listOf("too_long", "distraction")) {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceBright), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("One interruption does not erase progress.", fontFamily = GoogleSans, color = CharcoalPrimary)
                    TextButton(onClick = {
                        viewModel.setMinutes(minOf(10, maxOf(1, last.plannedDurationMinutes / 2)))
                        showPlan = true
                    }) { Text("Choose a shorter session") }
                }
            }
        } else if (last?.exitReason == "blocked_app") {
            Text("Before your next session, add the tool you needed on the Bags screen.", color = SecondaryGray)
        }

        // Accessibility Permission Notice (compact if disabled)
        if (!isAccessibilityOn) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceBright),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = AccentOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Permission Required",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Enable Accessibility for screen blocking.",
                            fontFamily = GoogleSans,
                            color = SecondaryGray,
                            fontSize = 10.sp
                        )
                    }
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Enable", color = SurfaceBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. MAIN TIMER CARD (Fixed proportion, fits cleanly in viewport)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                DigitalTimerDisplay(
                    hours = hours,
                    minutes = minutes,
                    blockSize = 16.5.dp,
                    digitColor = CharcoalPrimary,
                    colonColor = SecondaryGray.copy(alpha = 0.7f)
                )
            }
        }

        // 3. PRESETS GRID & VERTICAL SELECTOR (Balanced 2x2 grid matching ruler)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column: 2x2 Preset Cards
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: "15 Minutes" & "45 Minutes"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCard(
                        value = "15",
                        label = "Minutes",
                        isSelected = selectedMinutes == 15,
                        onClick = { viewModel.setMinutes(15) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                    PresetCard(
                        value = "45",
                        label = "Minutes",
                        isSelected = selectedMinutes == 45,
                        onClick = { viewModel.setMinutes(45) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }

                // Row 2: "3 Hours" & "12 Hours"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCard(
                        value = "3",
                        label = "Hours",
                        isSelected = selectedMinutes == 180,
                        onClick = { viewModel.setMinutes(180) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                    PresetCard(
                        value = "12",
                        label = "Hours",
                        isSelected = selectedMinutes == 720,
                        onClick = { viewModel.setMinutes(720) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }
            }

            // Right Column: Vertical Time Ruler Picker & Chevrons (matches presets height)
            VerticalRulerPicker(
                selectedMinutes = selectedMinutes,
                onMinutesChanged = { viewModel.setMinutes(it) },
                modifier = Modifier.fillMaxHeight()
            )
        }

        // 4. SLIDE TO START
        SlideToStart(
            onSlideComplete = {
                showPlan = true
            },
            text = "SLIDE TO START"
        )
        }
    }
}

@Composable
fun PresetCard(
    value: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CharcoalPrimary else SurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = if (isSelected) SurfaceBright else CharcoalPrimary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = label,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isSelected) SurfaceBright.copy(alpha = 0.7f) else SecondaryGray
            )
        }
    }
}
