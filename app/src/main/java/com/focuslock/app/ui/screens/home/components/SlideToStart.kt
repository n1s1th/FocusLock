package com.focuslock.app.ui.screens.home.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SecondaryEmerald
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToStart(
    onSlideComplete: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "SLIDE TO LOCK"
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current

    val height = 64.dp
    val thumbSize = 52.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceCard)
            .padding(6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Label Text in Background
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        // Swipeable Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryIndigo, SecondaryEmerald)
                    )
                )
                .pointerInput(Unit) {
                    val maxOffsetPx = size.width * 4f // will be clamped dynamically

                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value > 450f) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onSlideComplete()
                                }
                                offsetX.animateTo(0f)
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(0f, 600f)
                                offsetX.snapTo(next)
                                if (next > 450f) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
