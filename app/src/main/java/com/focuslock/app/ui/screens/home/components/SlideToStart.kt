package com.focuslock.app.ui.screens.home.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.R
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SurfaceVariant
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToStart(
    onSlideComplete: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "SLIDE TO START"
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val onCompleteState by rememberUpdatedState(onSlideComplete)

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animOffsetX = remember { Animatable(0f) }
    var hasTriggeredThresholdHaptic by remember { mutableStateOf(false) }

    val containerHeight = 72.dp
    val handleWidth = 76.dp
    val handleHeight = 56.dp
    val padding = 8.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceVariant)
            .padding(padding),
        contentAlignment = Alignment.CenterStart
    ) {
        val maxDragPx = with(density) {
            (maxWidth - handleWidth - padding * 2).toPx().coerceAtLeast(1f)
        }

        val currentOffset = if (isDragging) dragOffsetX else animOffsetX.value
        val progress = (currentOffset / maxDragPx).coerceIn(0f, 1f)

        // Centered "SLIDE TO START" text (fades subtly as handle moves over it)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (1f - progress * 1.5f).coerceIn(0f, 1f) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = SecondaryGray,
                fontSize = 12.sp,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 48.dp)
            )
        }

        // Draggable Handle
        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffset.roundToInt(), 0) }
                .size(width = handleWidth, height = handleHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(CharcoalPrimary)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffsetX = animOffsetX.value
                            hasTriggeredThresholdHaptic = false
                        },
                        onDragEnd = {
                            isDragging = false
                            scope.launch {
                                if (dragOffsetX >= maxDragPx * 0.72f) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    animOffsetX.snapTo(dragOffsetX)
                                    animOffsetX.animateTo(maxDragPx, tween(120))
                                    onCompleteState()
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

                            if (next >= maxDragPx * 0.72f) {
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
                painter = painterResource(id = R.drawable.ic_play),
                contentDescription = "Start",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

