package com.focuslock.app.ui.screens.home.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current

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
            (maxWidth - handleWidth - padding * 2).toPx()
        }

        // Centered "SLIDE TO START" text
        Box(
            modifier = Modifier.fillMaxSize(),
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
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(width = handleWidth, height = handleHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(CharcoalPrimary)
                .pointerInput(maxDragPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= maxDragPx * 0.72f) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    offsetX.animateTo(maxDragPx, tween(150))
                                    onSlideComplete()
                                }
                                offsetX.animateTo(0f, tween(300))
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(0f, maxDragPx)
                                offsetX.snapTo(next)
                                if (next >= maxDragPx * 0.72f) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
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
