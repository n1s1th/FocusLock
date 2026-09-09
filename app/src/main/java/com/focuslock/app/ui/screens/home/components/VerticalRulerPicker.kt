package com.focuslock.app.ui.screens.home.components

import android.view.HapticFeedbackConstants
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.OutlineCard
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.SecondaryGray
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
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

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
                .pointerInput(selectedMinutes) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            // Drag up decreases, drag down increases (or vice versa)
                            dragAccumulator += dragAmount
                            val stepPx = 10f
                            if (kotlin.math.abs(dragAccumulator) >= stepPx) {
                                val steps = (dragAccumulator / stepPx).toInt()
                                val next = (selectedMinutes - steps).coerceIn(minMinutes, maxMinutes)
                                if (next != selectedMinutes) {
                                    onMinutesChanged(next)
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                                dragAccumulator %= stepPx
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
                val offsetFraction = (selectedMinutes % 60) / 60f
                val scrollOffset = offsetFraction * tickSpacing * 6

                val tickStartX = rulerWidth * 0.22f
                val tickEndX = rulerWidth * 0.78f

                for (i in -totalTicks..totalTicks) {
                    val y = (rulerHeight / 2f) + (i * tickSpacing) - (scrollOffset % tickSpacing)
                    if (y in 8.dp.toPx()..(rulerHeight - 8.dp.toPx())) {
                        drawLine(
                            color = SecondaryGray.copy(alpha = 0.35f),
                            start = Offset(tickStartX, y),
                            end = Offset(tickEndX, y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Fixed Orange Selection Bar Indicator
                val indicatorWidth = rulerWidth * 0.58f
                val indicatorHeight = 7.dp.toPx()
                val indicatorY = rulerHeight * 0.52f - (indicatorHeight / 2f)
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
            // Up Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        val next = (selectedMinutes + 5).coerceAtMost(maxMinutes)
                        onMinutesChanged(next)
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

            // Down Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        val next = (selectedMinutes - 5).coerceAtLeast(minMinutes)
                        onMinutesChanged(next)
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
