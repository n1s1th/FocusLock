package com.focuslock.app.ui.screens.routines

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.data.database.entities.RoutineEntity
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
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RoutinesScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: RoutinesViewModel = viewModel()
) {
    val routines by viewModel.routines.collectAsState()
    var selectedRoutineIndex by remember { mutableIntStateOf(0) }

    // Fix 6: Only use real DB entities — never mix fallback IDs with live data.
    // If loading, show zero-state defaults (id=-1 so no ViewModel write will match a real row).
    val emptyRoutine = RoutineEntity(id = -1L, name = "Loading", targetBagId = 1L, startHour = 9, startMinute = 0, durationMinutes = 60, activeDaysMask = 62, isEnabled = false)
    val routine1 = routines.getOrNull(0) ?: emptyRoutine.copy(name = "Routine 1")
    val routine2 = routines.getOrNull(1) ?: emptyRoutine.copy(name = "Routine 2")
    val routine3 = routines.getOrNull(2) ?: emptyRoutine.copy(name = "Routine 3")
    val routineList = listOf(routine1, routine2, routine3)

    // Use the currently selected real routine (or first if index out of range)
    val currentRoutine = routineList.getOrElse(selectedRoutineIndex) { routine1 }
    val isDataLoaded = routines.isNotEmpty()

    val startHour = currentRoutine.startHour
    val startMinute = currentRoutine.startMinute
    val durationMinutes = currentRoutine.durationMinutes

    val endTotalMinutes = (startHour * 60 + startMinute + durationMinutes) % (24 * 60)
    val endHour = endTotalMinutes / 60
    val endMinute = endTotalMinutes % 60

    val isEnabled = currentRoutine.isEnabled
    val daysMask = currentRoutine.activeDaysMask

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HEADER (Live Streaks + Parachute Count)
        TopHeaderBar(
            streakCount = viewModel.currentStreak,
            parachuteCount = viewModel.totalParachutes,
            onProfileClick = onNavigateToSettings
        )

        // 2. TOP WHITE CARD (Dual Digital Clocks + 24h Timeline Progress Bar)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Clocks Row: Start Time [—] End Time (Responsive sizing so it never truncates)
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val availableWidth = maxWidth
                    val block = (availableWidth / 33f).coerceIn(7.dp, 9.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DigitalTimerDisplay(
                            hours = startHour,
                            minutes = startMinute,
                            blockSize = block,
                            digitSpacing = block * 0.5f,
                            digitColor = CharcoalPrimary,
                            colonColor = SecondaryGray.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.width(block * 1.2f))

                        // Middle separator dash
                        Box(
                            modifier = Modifier
                                .size(width = block * 1.5f, height = block * 0.5f)
                                .clip(RoundedCornerShape(block * 0.25f))
                                .background(CharcoalPrimary.copy(alpha = 0.6f))
                        )

                        Spacer(modifier = Modifier.width(block * 1.2f))

                        DigitalTimerDisplay(
                            hours = endHour,
                            minutes = endMinute,
                            blockSize = block,
                            digitSpacing = block * 0.5f,
                            digitColor = CharcoalPrimary,
                            colonColor = SecondaryGray.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 24-Hour Dotted Timeline Bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    val dotCount = 48 // 48 dots = 30-min increments
                    val spacing = size.width / dotCount
                    val dotSize = 4.5.dp.toPx()

                    val startSlot = ((startHour * 60 + startMinute) / 30).coerceIn(0, dotCount - 1)
                    val endSlot = (endTotalMinutes / 30).coerceIn(0, dotCount - 1)

                    for (i in 0 until dotCount) {
                        val inRange = if (startSlot <= endSlot) {
                            i in startSlot..endSlot
                        } else {
                            // Overnight wrap
                            i >= startSlot || i <= endSlot
                        }
                        val color = if (inRange && isEnabled) AccentOrange
                        else if (inRange) CharcoalPrimary.copy(alpha = 0.5f)
                        else Color(0xFFD6D6D6)

                        drawRect(
                            color = color,
                            topLeft = Offset(i * spacing, (size.height - dotSize) / 2f),
                            size = Size(dotSize, dotSize)
                        )
                    }
                }
            }
        }

        // 3. MIDDLE SECTION (Routine 1, Routine 2, Routine 3 Cards + Vertical Drag ON/OFF Switch)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Routine 1
            RoutineTabCard(
                label = "ROUTINE 1",
                time = String.format(Locale.US, "%02d:%02d", routine1.startHour, routine1.startMinute),
                isSelected = selectedRoutineIndex == 0,
                onClick = { selectedRoutineIndex = 0 },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Routine 2
            RoutineTabCard(
                label = "ROUTINE 2",
                time = String.format(Locale.US, "%02d:%02d", routine2.startHour, routine2.startMinute),
                isSelected = selectedRoutineIndex == 1,
                onClick = { selectedRoutineIndex = 1 },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Routine 3 (100% Unlocked, Zero Locks!)
            RoutineTabCard(
                label = "ROUTINE 3",
                time = String.format(Locale.US, "%02d:%02d", routine3.startHour, routine3.startMinute),
                isSelected = selectedRoutineIndex == 2,
                onClick = { selectedRoutineIndex = 2 },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Vertical Drag ON/OFF Switch (Drag UP = ON, Drag DOWN = OFF)
            VerticalOnOffSwitch(
                isEnabled = isEnabled,
                onToggle = { newEnabled ->
                    // Fix 6: Only update real entities
                    if (isDataLoaded) {
                        viewModel.setRoutineEnabled(currentRoutine, newEnabled)
                    }
                },
                modifier = Modifier
                    .width(62.dp)
                    .fillMaxHeight()
            )
        }

        // 4. BOTTOM ROUTINE SCHEDULER CARD (Days of Week + Rulers)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Days Row: S M T W T F S (Sun=0..Sat=6)
                // Dragging UP = Applied / Selected, Dragging DOWN = Unapplied
                val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { dayIndex, label ->
                        val isDayActive = (daysMask and (1 shl dayIndex)) != 0
                        DraggableDaySlot(
                            label = label,
                            isActive = isDayActive,
                            onStateChanged = { makeActive ->
                                // Fix 6: Only update when real DB data is loaded
                                if (isDataLoaded) {
                                    val newMask = if (makeActive) {
                                        daysMask or (1 shl dayIndex)
                                    } else {
                                        daysMask and (1 shl dayIndex).inv()
                                    }
                                    viewModel.updateRoutineDays(currentRoutine, newMask)
                                }
                            }
                        )
                    }
                }

                // Duration & Status Subtitle (e.g. "Off · 1h" or "On · 7h 10m")
                val durHours = durationMinutes / 60
                val durMins = durationMinutes % 60
                val durationFormatted = when {
                    durHours > 0 && durMins > 0 -> "${durHours}h ${durMins}m"
                    durHours > 0 -> "${durHours}h"
                    else -> "${durMins}m"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isEnabled) "On" else "Off",
                        fontFamily = GoogleSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) AccentOrange else SecondaryGray
                    )
                    Text(
                        text = " · ",
                        fontFamily = GoogleSans,
                        fontSize = 12.sp,
                        color = SecondaryGray
                    )
                    Text(
                        text = durationFormatted,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isEnabled) AccentOrange else AccentOrange.copy(alpha = 0.7f)
                    )
                }

                // Dual Scrollable Time Rulers: START and END
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // START RULER — only respond when real data is loaded
                    RoutineScrollableRuler(
                        label = "START",
                        totalMinutes = startHour * 60 + startMinute,
                        onMinutesChanged = { newStartMinutes ->
                            if (isDataLoaded) {
                                val sH = newStartMinutes / 60
                                val sM = newStartMinutes % 60
                                viewModel.updateRoutineTimes(currentRoutine, sH, sM, endHour, endMinute)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // END RULER — only respond when real data is loaded
                    RoutineScrollableRuler(
                        label = "END",
                        totalMinutes = endTotalMinutes,
                        onMinutesChanged = { newEndMinutes ->
                            if (isDataLoaded) {
                                val eH = newEndMinutes / 60
                                val eM = newEndMinutes % 60
                                viewModel.updateRoutineTimes(currentRoutine, startHour, startMinute, eH, eM)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
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
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = if (isSelected) CharcoalPrimary else SecondaryGray
            )

            Text(
                text = time,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isSelected) CharcoalPrimary else SecondaryMuted
            )

            // Bottom horizontal indicator underline
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSelected) CharcoalPrimary else Color.Transparent)
            )
        }
    }
}

/**
 * Vertical ON/OFF Switch (Drag UP = ON, Drag DOWN = OFF)
 */
@Composable
fun VerticalOnOffSwitch(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val onToggleState by rememberUpdatedState(onToggle)
    val isEnabledState by rememberUpdatedState(isEnabled)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -6f && !isEnabledState) {
                                onToggleState(true)
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            } else if (dragAmount > 6f && isEnabledState) {
                                onToggleState(false)
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                        }
                    )
                },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Slot: ON (AccentOrange when active)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(39.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isEnabled) AccentOrange else Color.Transparent)
                    .clickable {
                        if (!isEnabled) {
                            onToggleState(true)
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ON",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isEnabled) Color.White else SecondaryGray
                )
            }

            // Bottom Slot: OFF (CharcoalPrimary when inactive)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(39.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (!isEnabled) CharcoalPrimary else Color.Transparent)
                    .clickable {
                        if (isEnabled) {
                            onToggleState(false)
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OFF",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (!isEnabled) SurfaceBright else SecondaryGray
                )
            }
        }
    }
}

/**
 * Draggable Day Slot:
 * Position UP = Applied (Orange), Position DOWN = Unapplied (Inactive)
 */
@Composable
fun DraggableDaySlot(
    label: String,
    isActive: Boolean,
    onStateChanged: (Boolean) -> Unit
) {
    val view = LocalView.current
    val onStateChangedState by rememberUpdatedState(onStateChanged)
    val isActiveState by rememberUpdatedState(isActive)

    // Smooth animated vertical offset for thumb
    val thumbOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isActive) 0.dp else 22.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "daySlotThumbOffset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = CharcoalPrimary
        )

        // Vertical Pill Track (width 28.dp, height 48.dp)
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ScreenBackground)
                .padding(2.5.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -5f && !isActiveState) {
                                onStateChangedState(true)
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            } else if (dragAmount > 5f && isActiveState) {
                                onStateChangedState(false)
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                        }
                    )
                }
                .clickable {
                    onStateChangedState(!isActive)
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .offset(y = thumbOffset)
                    .size(23.dp, 21.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) AccentOrange else SecondaryGray.copy(alpha = 0.35f))
            )
        }
    }
}

/**
 * Horizontal Scrollable / Draggable Time Ruler Dial
 */
@Composable
fun RoutineScrollableRuler(
    label: String,
    totalMinutes: Int,
    onMinutesChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val onMinutesChangedState by rememberUpdatedState(onMinutesChanged)

    var isDragging by remember { mutableStateOf(false) }
    var currentMinutesFloat by remember { mutableFloatStateOf(totalMinutes.toFloat()) }
    var lastReportedMinutes by remember { mutableIntStateOf(totalMinutes) }

    LaunchedEffect(totalMinutes) {
        if (!isDragging) {
            currentMinutesFloat = totalMinutes.toFloat()
            lastReportedMinutes = totalMinutes
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, OutlineSubtle, RoundedCornerShape(16.dp))
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    val pxPerMinute = 6f
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            val snapped = ((currentMinutesFloat / 5f).roundToInt() * 5 + 1440) % 1440
                            currentMinutesFloat = snapped.toFloat()
                            if (snapped != lastReportedMinutes) {
                                lastReportedMinutes = snapped
                                onMinutesChangedState(snapped)
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            val snapped = ((currentMinutesFloat / 5f).roundToInt() * 5 + 1440) % 1440
                            currentMinutesFloat = snapped.toFloat()
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val deltaMinutes = -dragAmount / pxPerMinute
                            val nextMinutes = (currentMinutesFloat + deltaMinutes + 1440f) % 1440f
                            currentMinutesFloat = nextMinutes

                            // 5-minute step reporting
                            val intMinutes = ((nextMinutes / 5f).roundToInt() * 5) % 1440
                            if (intMinutes != lastReportedMinutes) {
                                lastReportedMinutes = intMinutes
                                onMinutesChangedState(intMinutes)
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val tickSpacing = 8.dp.toPx()
                val totalTicks = (size.width / tickSpacing).toInt() + 4
                val scrollOffset = currentMinutesFloat * tickSpacing * 0.5f

                for (i in -totalTicks..totalTicks) {
                    val x = (size.width / 2f) + (i * tickSpacing) - (scrollOffset % tickSpacing)
                    if (x in 6.dp.toPx()..(size.width - 6.dp.toPx())) {
                        val isMajor = (i % 4 == 0)
                        drawLine(
                            color = SecondaryGray.copy(alpha = if (isMajor) 0.45f else 0.2f),
                            start = Offset(x, size.height * (if (isMajor) 0.18f else 0.28f)),
                            end = Offset(x, size.height * (if (isMajor) 0.82f else 0.72f)),
                            strokeWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Center Orange Line (Top and Bottom Indicator)
                val centerX = size.width / 2f
                drawLine(
                    color = AccentOrange,
                    start = Offset(centerX, size.height * 0.08f),
                    end = Offset(centerX, size.height * 0.32f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = AccentOrange,
                    start = Offset(centerX, size.height * 0.68f),
                    end = Offset(centerX, size.height * 0.92f),
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
            fontSize = 11.sp,
            color = SecondaryGray,
            letterSpacing = 1.sp
        )
    }
}
