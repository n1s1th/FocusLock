package com.focuslock.app.ui.screens.stats

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.ui.screens.home.components.TopHeaderBar
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.ui.theme.SurfaceVariant
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatsScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: StatsViewModel = viewModel()
) {
    var selectedPeriodMode by remember { mutableIntStateOf(1) } // 0: Day, 1: Month, 2: Year
    var rotaryAngle by remember { mutableFloatStateOf(210f) }

    // Fix 5: Real data from StateFlows
    val recentSessions by viewModel.recentSessions.collectAsState()
    val parachuteCount by viewModel.parachutesCount.collectAsState()
    val todayMinutes by viewModel.todayMinutes.collectAsState()
    val totalLifetimeMinutes by viewModel.totalLifetimeMinutes.collectAsState()
    val totalCompletedSessions by viewModel.totalCompletedSessions.collectAsState()

    // Fix 5: Date navigation state — Calendar-based, starts at current month
    var displayedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val monthYearLabel = viewModel.formatMonthYear(displayedCalendar)

    // Fix 5: Real day label from today's session data
    val dayLabel = viewModel.formatDayLabel(recentSessions)

    // Fix 5: Real bar chart data from last 7 days
    val weeklyBars = viewModel.getWeeklyBarData(recentSessions)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP HEADER — Fix 5: live streak + parachute count
        TopHeaderBar(
            streakCount = viewModel.getStreak(),
            parachuteCount = parachuteCount,
            onProfileClick = onNavigateToSettings
        )

        // 2. TOP WHITE CARD — Fix 5: real bar chart from weekly session data
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bottomY = size.height - 8.dp.toPx()
                    val barWidth = 6.dp.toPx()
                    val barGap = 4.dp.toPx()

                    // Fix 5: Real bar heights from last 7 days of sessions
                    weeklyBars.forEachIndexed { i, fraction ->
                        val h = (size.height * 0.75f) * fraction.coerceAtLeast(0.04f)
                        val x = i * (barWidth + barGap) + 4.dp.toPx()
                        drawRoundRect(
                            color = if (i == weeklyBars.lastIndex) AccentOrange else CharcoalPrimary,
                            topLeft = Offset(x, bottomY - h),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }

                    // Dotted Baseline timeline
                    val startX = weeklyBars.size * (barWidth + barGap) + 8.dp.toPx()
                    val dotCount = 24
                    val dotSize = 4.dp.toPx()
                    val dotSpacing = (size.width - startX) / dotCount.toFloat()
                    for (d in 0 until dotCount) {
                        val color = if (d == 0) AccentOrange else Color(0xFFD0D0D0)
                        drawRect(
                            color = color,
                            topLeft = Offset(startX + (d * dotSpacing), bottomY - dotSize),
                            size = Size(dotSize, dotSize)
                        )
                    }
                }
            }
        }

        // Fix 5: Real day label from today's actual session data
        Text(
            text = dayLabel,
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            color = SecondaryGray
        )

        // 3. DATE SELECTOR CARD — Fix 5: working prev/next navigation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable {
                            // Fix 5: Navigate backward in time
                            val prev = displayedCalendar.clone() as Calendar
                            when (selectedPeriodMode) {
                                0 -> prev.add(Calendar.DAY_OF_YEAR, -1)
                                1 -> prev.add(Calendar.MONTH, -1)
                                2 -> prev.add(Calendar.YEAR, -1)
                            }
                            displayedCalendar = prev
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        tint = SecondaryGray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(52.dp)
                        .background(OutlineSubtle)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Fix 5: Real calendar-driven label
                    Text(
                        text = monthYearLabel,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.1.sp,
                        color = CharcoalPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(52.dp)
                        .background(OutlineSubtle)
                )

                // Next Button (disabled if at current month/year)
                val isAtPresent = run {
                    val now = Calendar.getInstance()
                    when (selectedPeriodMode) {
                        0 -> displayedCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                                displayedCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                        1 -> displayedCalendar.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                                displayedCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                        else -> displayedCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable(enabled = !isAtPresent) {
                            // Fix 5: Navigate forward in time
                            val next = displayedCalendar.clone() as Calendar
                            when (selectedPeriodMode) {
                                0 -> next.add(Calendar.DAY_OF_YEAR, 1)
                                1 -> next.add(Calendar.MONTH, 1)
                                2 -> next.add(Calendar.YEAR, 1)
                            }
                            displayedCalendar = next
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next",
                        tint = SecondaryGray.copy(alpha = if (isAtPresent) 0.25f else 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 4. BOTTOM SECTION: Period mode + Rotary wheel + Stats summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column: Day / Month / Year mode buttons
            Column(
                modifier = Modifier
                    .width(88.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Day", "Month", "Year").forEachIndexed { index, mode ->
                    val isSelected = index == selectedPeriodMode
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) CharcoalPrimary else SurfaceVariant)
                            .clickable {
                                selectedPeriodMode = index
                                // Reset calendar to today on mode change
                                displayedCalendar = Calendar.getInstance()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) SurfaceBright else SecondaryGray
                        )
                    }
                }
            }

            // Right Column: Rotary Gauge + Stats overlay
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    RotaryGaugeView(
                        angleDegrees = rotaryAngle,
                        onAngleChanged = { rotaryAngle = it },
                        modifier = Modifier.size(135.dp)
                    )
                    // Fix 5: Show real total sessions count in the center of the gauge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalCompletedSessions",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = CharcoalPrimary
                        )
                        Text(
                            text = "sessions",
                            fontFamily = GoogleSans,
                            fontSize = 10.sp,
                            color = SecondaryGray
                        )
                    }
                }
            }
        }

        // Fix 5: Real lifetime stats summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatSummaryCard(
                label = "Today",
                value = "${todayMinutes ?: 0} min",
                modifier = Modifier.weight(1f)
            )
            StatSummaryCard(
                label = "All Time",
                value = "${(totalLifetimeMinutes ?: 0) / 60}h ${(totalLifetimeMinutes ?: 0) % 60}m",
                modifier = Modifier.weight(1f)
            )
            StatSummaryCard(
                label = "Streak",
                value = "${viewModel.getStreak()} days",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatSummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBright),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CharcoalPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = GoogleSans,
                fontSize = 11.sp,
                color = SecondaryGray
            )
        }
    }
}

@Composable
fun RotaryGaugeView(
    angleDegrees: Float,
    onAngleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val onAngleChangedState by rememberUpdatedState(onAngleChanged)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, _ ->
                change.consume()
                val center = Offset(size.width / 2f, size.height / 2f)
                val touch = change.position
                val rad = atan2(touch.y - center.y, touch.x - center.x)
                var deg = Math.toDegrees(rad.toDouble()).toFloat()
                if (deg < 0) deg += 360f
                onAngleChangedState(deg)
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f - 10.dp.toPx()
        val innerCircleRadius = outerRadius * 0.72f

        // Radial tick marks
        val tickCount = 40
        for (i in 0 until tickCount) {
            val angle = (i * (360f / tickCount))
            val rad = Math.toRadians(angle.toDouble())
            val startR = outerRadius - 6.dp.toPx()
            val endR = outerRadius

            val startX = (center.x + startR * cos(rad)).toFloat()
            val startY = (center.y + startR * sin(rad)).toFloat()
            val endX = (center.x + endR * cos(rad)).toFloat()
            val endY = (center.y + endR * sin(rad)).toFloat()

            drawLine(
                color = SecondaryGray.copy(alpha = 0.25f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Inner circular track
        drawCircle(
            color = ScreenBackground,
            radius = innerCircleRadius,
            center = center
        )
        drawCircle(
            color = OutlineSubtle,
            radius = innerCircleRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Orange knob / dot on inner track
        val knobDistance = innerCircleRadius * 0.58f
        val currentRad = Math.toRadians(angleDegrees.toDouble())
        val knobX = (center.x + knobDistance * cos(currentRad)).toFloat()
        val knobY = (center.y + knobDistance * sin(currentRad)).toFloat()

        drawCircle(
            color = AccentOrange,
            radius = 7.5.dp.toPx(),
            center = Offset(knobX, knobY)
        )
    }
}
