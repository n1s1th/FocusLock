package com.focuslock.app.ui.screens.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary

private val LOGO_MATRIX = intArrayOf(
    1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0,
    1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1,
    1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0,
    1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0
)

private const val MATRIX_COLS = 23
private const val MATRIX_ROWS = 4

@Composable
fun BlockLogoView(
    modifier: Modifier = Modifier,
    pixelSize: Dp = 5.dp,
    spacing: Dp = 0.dp,
    color: Color = CharcoalPrimary,
    pulseColor: Color = AccentOrange
) {
    val totalWidth = pixelSize * MATRIX_COLS + spacing * (MATRIX_COLS - 1)
    val totalHeight = pixelSize * MATRIX_ROWS + spacing * (MATRIX_ROWS - 1)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier.size(totalWidth, totalHeight)) {
        val px = pixelSize.toPx()
        val sp = spacing.toPx()

        for (row in 0 until MATRIX_ROWS) {
            for (col in 0 until MATRIX_COLS) {
                val index = row * MATRIX_COLS + col
                if (LOGO_MATRIX[index] == 1) {
                    val x = col * (px + sp)
                    val y = row * (px + sp)
                    
                    // Specific accent pixel dot in "i" dot or "t" cross (e.g. col 16, row 0)
                    val isAccentPixel = (col == 16 && row == 0)
                    val drawColor = if (isAccentPixel) pulseColor.copy(alpha = pulseAlpha) else color

                    drawRect(
                        color = drawColor,
                        topLeft = Offset(x, y),
                        size = Size(px, px)
                    )
                }
            }
        }
    }
}
