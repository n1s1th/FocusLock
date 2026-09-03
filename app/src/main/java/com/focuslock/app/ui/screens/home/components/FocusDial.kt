package com.focuslock.app.ui.screens.home.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SecondaryEmerald
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FocusDial(
    selectedMinutes: Int,
    onMinutesChanged: (Int) -> Unit,
    maxMinutes: Int = 180,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var currentAngle by remember(selectedMinutes) {
        mutableFloatStateOf((selectedMinutes.toFloat() / maxMinutes) * 300f)
    }

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val touchX = change.position.x - center.x
                        val touchY = change.position.y - center.y

                        var angleRad = atan2(touchY, touchX) + (PI.toFloat() / 2f)
                        if (angleRad < 0) angleRad += (2 * PI.toFloat())

                        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                        // Map angle [0..300] deg to [5..maxMinutes]
                        if (angleDeg in 0f..300f) {
                            currentAngle = angleDeg
                            val calculatedMins = ((angleDeg / 300f) * maxMinutes).roundToInt().coerceIn(5, maxMinutes)
                            if (calculatedMins != selectedMinutes) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                onMinutesChanged(calculatedMins)
                            }
                        }
                    }
                }
        ) {
            val strokeWidth = 24.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background Track Arc (300 degrees sweep starting from 120 deg)
            drawArc(
                color = Color(0xFF1C1F2E),
                startAngle = 120f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            val activeSweep = ((selectedMinutes.toFloat() / maxMinutes) * 300f).coerceIn(5f, 300f)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(PrimaryIndigo, SecondaryEmerald, PrimaryIndigo)
                ),
                startAngle = 120f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dial Thumb Handle
            val thumbAngleRad = Math.toRadians((120f + activeSweep).toDouble())
            val thumbX = center.x + radius * cos(thumbAngleRad).toFloat()
            val thumbY = center.y + radius * sin(thumbAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = 16.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = PrimaryIndigo,
                radius = 8.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        // Center Time Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$selectedMinutes",
                color = TextPrimary,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (selectedMinutes >= 60) {
                    val hrs = selectedMinutes / 60
                    val mins = selectedMinutes % 60
                    if (mins == 0) "$hrs hr focus" else "${hrs}h ${mins}m focus"
                } else {
                    "minutes focus"
                },
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
