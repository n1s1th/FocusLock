package com.focuslock.app.ui.screens.home.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.SecondaryGray
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun VerticalRulerPicker(
    selectedMinutes: Int,
    onMinutesChanged: (Int) -> Unit,
    minMinutes: Int = 1,
    maxMinutes: Int = 720,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val onMinutesChangedState by rememberUpdatedState(onMinutesChanged)
    val minLimit by rememberUpdatedState(minMinutes)
    val maxLimit by rememberUpdatedState(maxMinutes)

    var isDragging by remember { mutableStateOf(false) }
    var currentMinutesFloat by remember { mutableFloatStateOf(selectedMinutes.toFloat()) }
    var lastReportedMinutes by remember { mutableIntStateOf(selectedMinutes) }

    // Synchronize if selectedMinutes changed externally (e.g. preset clicked) and not currently dragging
    LaunchedEffect(selectedMinutes) {
        if (!isDragging) {
            currentMinutesFloat = selectedMinutes.toFloat()
            lastReportedMinutes = selectedMinutes
        }
    }

    Column(
        modifier = modifier.width(92.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ruler Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, OutlineSubtle, RoundedCornerShape(28.dp))
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    val pxPerMinute = 8f
                    detectVerticalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            val snapped = currentMinutesFloat.roundToInt().coerceIn(minLimit, maxLimit)
                            currentMinutesFloat = snapped.toFloat()
                            if (snapped != lastReportedMinutes) {
                                lastReportedMinutes = snapped
                                onMinutesChangedState(snapped)
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            val snapped = currentMinutesFloat.roundToInt().coerceIn(minLimit, maxLimit)
                            currentMinutesFloat = snapped.toFloat()
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            // Drag up decreases screen Y (negative), meaning time increases
                            val deltaMinutes = -dragAmount / pxPerMinute
                            val nextMinutes = (currentMinutesFloat + deltaMinutes)
                                .coerceIn(minLimit.toFloat(), maxLimit.toFloat())
                            currentMinutesFloat = nextMinutes

                            val intMinutes = nextMinutes.roundToInt()
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
                val rulerHeight = size.height
                val rulerWidth = size.width
                val tickSpacing = 8.dp.toPx()
                val totalTicks = (rulerHeight / tickSpacing).toInt() + 4

                // Continuous tick offset from currentMinutesFloat
                val scrollOffset = currentMinutesFloat * tickSpacing * 0.75f

                val tickStartX = rulerWidth * 0.22f
                val tickEndX = rulerWidth * 0.78f

                for (i in -totalTicks..totalTicks) {
                    val y = (rulerHeight / 2f) + (i * tickSpacing) - (scrollOffset % tickSpacing)
                    if (y in 8.dp.toPx()..(rulerHeight - 8.dp.toPx())) {
                        val isMajor = (i % 5 == 0)
                        drawLine(
                            color = if (isMajor) SecondaryGray.copy(alpha = 0.5f) else SecondaryGray.copy(alpha = 0.25f),
                            start = Offset(if (isMajor) tickStartX - 4.dp.toPx() else tickStartX, y),
                            end = Offset(if (isMajor) tickEndX + 4.dp.toPx() else tickEndX, y),
                            strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Fixed Orange Selection Bar Indicator
                val indicatorWidth = rulerWidth * 0.58f
                val indicatorHeight = 7.dp.toPx()
                val indicatorY = rulerHeight * 0.50f - (indicatorHeight / 2f)
                val indicatorX = (rulerWidth - indicatorWidth) / 2f

                drawRoundRect(
                    color = AccentOrange,
                    topLeft = Offset(indicatorX, indicatorY),
                    size = Size(indicatorWidth, indicatorHeight),
                    cornerRadius = CornerRadius(indicatorHeight / 2f, indicatorHeight / 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Up / Down Button Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, OutlineSubtle, RoundedCornerShape(22.dp))
                .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up Button (+5 min)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        val next = (selectedMinutes + 5).coerceAtMost(maxLimit)
                        currentMinutesFloat = next.toFloat()
                        lastReportedMinutes = next
                        onMinutesChangedState(next)
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Increment time",
                    tint = CharcoalPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OutlineSubtle)
            )

            // Down Button (-5 min)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        val next = (selectedMinutes - 5).coerceAtLeast(minLimit)
                        currentMinutesFloat = next.toFloat()
                        lastReportedMinutes = next
                        onMinutesChangedState(next)
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Decrement time",
                    tint = SecondaryGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

