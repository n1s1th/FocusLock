package com.focuslock.app.ui.screens.routines

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.R
import com.focuslock.app.ui.screens.home.components.DigitalTimerDisplay
import com.focuslock.app.ui.screens.home.components.TopHeaderBar
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
fun RoutinesScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: RoutinesViewModel = viewModel()
) {
    var selectedRoutineIndex by remember { mutableIntStateOf(0) }
    var startHour by remember { mutableIntStateOf(22) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(5) }
    var endMinute by remember { mutableIntStateOf(10) }

    val daysActive = remember { mutableStateListOf(true, true, true, true, true, true, true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // 1. TOP HEADER
        TopHeaderBar(
            streakCount = 10,
            parachuteCount = 0,
            onProfileClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. TOP WHITE CARD (Dual Digital Clocks + Timeline Progress Bar)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            shape = RoundedCornerShape(38.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Clocks Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DigitalTimerDisplay(
                        hours = startHour,
                        minutes = startMinute,
                        blockSize = 13.dp,
                        digitColor = CharcoalPrimary,
                        colonColor = SecondaryGray.copy(alpha = 0.7f)
                    )

                    DigitalTimerDisplay(
                        hours = endHour,
                        minutes = endMinute,
                        blockSize = 13.dp,
                        digitColor = CharcoalPrimary,
                        colonColor = SecondaryGray.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Dotted Timeline Bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    val dotCount = 38
                    val spacing = size.width / dotCount
                    val dotSize = 4.5.dp.toPx()

                    for (i in 0 until dotCount) {
                        val isOrange = (i in 0..7) || (i in 32..37)
                        val color = if (isOrange) AccentOrange else Color(0xFFD0D0D0)
                        drawRect(
                            color = color,
                            topLeft = Offset(i * spacing, (size.height - dotSize) / 2f),
                            size = Size(dotSize, dotSize)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. MIDDLE SECTION (Routine Tab Cards + Lock/Action Controls)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Routine 1: NIGHT
            RoutineTabCard(
                label = "NIGHT",
                time = "22:00",
                isSelected = selectedRoutineIndex == 0,
                onClick = { selectedRoutineIndex = 0 },
                modifier = Modifier.weight(1f)
            )

            // Routine 2
            RoutineTabCard(
                label = "ROUTINE 2",
                time = "00:00",
                isSelected = selectedRoutineIndex == 1,
                onClick = { selectedRoutineIndex = 1 },
                modifier = Modifier.weight(1f)
            )

            // Routine 3
            RoutineTabCard(
                label = "ROUTINE 3",
                time = "00:00",
                isSelected = selectedRoutineIndex == 2,
                onClick = { selectedRoutineIndex = 2 },
                modifier = Modifier.weight(1f)
            )

            // Right Column Actions
            Column(
                modifier = Modifier.width(62.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Lock Button
                Card(
                    modifier = Modifier
                        .size(62.dp, 56.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Lock",
                            tint = CharcoalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Charcoal Solid Action Button
                Box(
                    modifier = Modifier
                        .size(62.dp, 56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CharcoalPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. BOTTOM ROUTINE SCHEDULER CARD (Days of Week + Rulers)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Days Row: M T W T F S S
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CharcoalPrimary
                            )

                            // Vertical Slider Toggle Pill
                            val isActive = daysActive[index]
                            Box(
                                modifier = Modifier
                                    .size(width = 30.dp, height = 52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ScreenBackground)
                                    .clickable { daysActive[index] = !daysActive[index] }
                                    .padding(3.dp),
                                contentAlignment = if (isActive) Alignment.TopCenter else Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp, 20.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isActive) AccentOrange else SecondaryGray.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Subtitle: Off · 7h 10m
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Off",
                        fontFamily = GoogleSans,
                        fontSize = 13.sp,
                        color = SecondaryGray
                    )
                    Text(
                        text = " · ",
                        fontFamily = GoogleSans,
                        fontSize = 13.sp,
                        color = SecondaryGray
                    )
                    Text(
                        text = "7h 10m",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentOrange
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dual Time Wheels: START and END
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoutineTimeWheel(
                        label = "START",
                        value = startHour * 60 + startMinute,
                        onValueChanged = {
                            startHour = it / 60
                            startMinute = it % 60
                        },
                        modifier = Modifier.weight(1f)
                    )

                    RoutineTimeWheel(
                        label = "END",
                        value = endHour * 60 + endMinute,
                        onValueChanged = {
                            endHour = it / 60
                            endMinute = it % 60
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RoutineTabCard(
    label: String,
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = SecondaryGray
            )

            Text(
                text = time,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = if (isSelected) CharcoalPrimary else SecondaryMuted
            )

            // Bottom horizontal indicator line
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSelected) CharcoalPrimary.copy(alpha = 0.3f) else Color.Transparent)
            )
        }
    }
}

@Composable
fun RoutineTimeWheel(
    label: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, OutlineSubtle, RoundedCornerShape(16.dp))
                .background(Color.Transparent)
                .pointerInput(value) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            dragAccumulator += dragAmount
                            val step = 10f
                            if (kotlin.math.abs(dragAccumulator) >= step) {
                                val delta = (dragAccumulator / step).toInt() * 5
                                val next = (value - delta).coerceIn(0, 1439)
                                if (next != value) {
                                    onValueChanged(next)
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                                dragAccumulator %= step
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val tickSpacing = 8.dp.toPx()
                val totalTicks = (size.width / tickSpacing).toInt() + 4
                val offsetFraction = (value % 60) / 60f
                val scrollOffset = offsetFraction * tickSpacing * 4

                for (i in -totalTicks..totalTicks) {
                    val x = (size.width / 2f) + (i * tickSpacing) - (scrollOffset % tickSpacing)
                    if (x in 6.dp.toPx()..(size.width - 6.dp.toPx())) {
                        drawLine(
                            color = SecondaryGray.copy(alpha = 0.25f),
                            start = Offset(x, size.height * 0.2f),
                            end = Offset(x, size.height * 0.8f),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Orange vertical center tick indicator
                val centerX = size.width / 2f
                drawLine(
                    color = AccentOrange,
                    start = Offset(centerX, size.height * 0.1f),
                    end = Offset(centerX, size.height * 0.9f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = SecondaryGray
        )
    }
}
