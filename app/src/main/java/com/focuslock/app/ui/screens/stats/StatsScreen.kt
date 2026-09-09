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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatsScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: StatsViewModel = viewModel()
) {
    var selectedPeriodMode by remember { mutableIntStateOf(1) } // 0: Day, 1: Month, 2: Year
    var selectedMonthYear by remember { mutableStateOf("SEPTEMBER 2026") }
    var rotaryAngle by remember { mutableFloatStateOf(210f) }

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

        // 2. TOP WHITE CARD (Minimalist Bar Chart + Dotted Timeline)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            shape = RoundedCornerShape(38.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bottomY = size.height - 10.dp.toPx()
                    val barWidth = 6.dp.toPx()
                    val barGap = 4.dp.toPx()

                    // Vertical bars on the left
                    val barHeights = listOf(0.85f, 0.50f, 0.72f)
                    barHeights.forEachIndexed { i, fraction ->
                        val h = (size.height * 0.75f) * fraction
                        val x = i * (barWidth + barGap) + 4.dp.toPx()
                        drawRoundRect(
                            color = CharcoalPrimary,
                            topLeft = Offset(x, bottomY - h),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }

                    // Dotted Baseline
                    val startX = 3 * (barWidth + barGap) + 8.dp.toPx()
                    val dotCount = 24
                    val dotSize = 4.dp.toPx()
                    val dotSpacing = (size.width - startX) / dotCount

                    for (d in 0 until dotCount) {
                        val isFirstDot = (d == 0)
                        val color = if (isFirstDot) AccentOrange else Color(0xFFD0D0D0)
                        drawRect(
                            color = color,
                            topLeft = Offset(startX + (d * dotSpacing), bottomY - dotSize),
                            size = Size(dotSize, dotSize)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subtitle: FRIDAY 4 · 0 MIN
        Text(
            text = "FRIDAY 4  ·  0 MIN",
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = SecondaryGray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. DATE SELECTOR CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(22.dp),
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
                        .size(68.dp)
                        .clickable { /* Prev */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        tint = SecondaryGray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(68.dp)
                        .background(OutlineSubtle)
                )

                // Month / Year Label
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedMonthYear,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp,
                        color = CharcoalPrimary
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(68.dp)
                        .background(OutlineSubtle)
                )

                // Next Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clickable { /* Next */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next",
                        tint = SecondaryGray.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. BOTTOM INTERACTIVE SECTION (Day/Month/Year + Rotary Wheel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column: Day / Month / Year Mode Buttons
            Column(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Day", "Month", "Year").forEachIndexed { index, mode ->
                    val isSelected = index == selectedPeriodMode
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) CharcoalPrimary else SurfaceVariant)
                            .clickable { selectedPeriodMode = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) SurfaceBright else SecondaryGray
                        )
                    }
                }
            }

            // Right Column: Circular Rotary Wheel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(26.dp),
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
                        modifier = Modifier.size(170.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RotaryGaugeView(
    angleDegrees: Float,
    onAngleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val touch = change.position
                val rad = atan2(touch.y - center.y, touch.x - center.x)
                var deg = Math.toDegrees(rad.toDouble()).toFloat()
                if (deg < 0) deg += 360f
                onAngleChanged(deg)
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f - 10.dp.toPx()
        val innerCircleRadius = outerRadius * 0.72f

        // Draw radial perimeter tick marks
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

        // Draw inner circular track
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

        // Draw Orange Knob / Dot on inner track
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
