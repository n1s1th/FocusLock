package com.focuslock.app.ui.screens.bags.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.SecondaryGray

private val BAG_MATRIX = arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1)
)

@Composable
fun SingleBagPixelArt(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    pixelSize: Dp = 5.dp
) {
    val bagColor = if (isSelected) AccentOrange else CharcoalPrimary
    val dotColor = if (isSelected) AccentOrange else SecondaryGray

    val width = pixelSize * 15
    val height = pixelSize * 16 // 12 rows bag + 1 row gap + 3 rows dots

    Canvas(modifier = modifier.size(width, height)) {
        val px = pixelSize.toPx()

        // Draw 12x15 bag body
        for (r in 0 until 12) {
            for (c in 0 until 15) {
                if (BAG_MATRIX[r][c] == 1) {
                    drawRect(
                        color = bagColor,
                        topLeft = Offset(c * px, r * px),
                        size = Size(px, px)
                    )
                }
            }
        }

        // Draw 3 bottom square slot dots
        val dotRowY = 14 * px
        val dotSize = px * 3
        
        // Dot 1 (left)
        drawRect(
            color = dotColor,
            topLeft = Offset(2 * px, dotRowY),
            size = Size(dotSize, dotSize)
        )
        // Dot 2 (middle)
        drawRect(
            color = dotColor,
            topLeft = Offset(6 * px, dotRowY),
            size = Size(dotSize, dotSize)
        )
        // Dot 3 (right)
        drawRect(
            color = dotColor,
            topLeft = Offset(10 * px, dotRowY),
            size = Size(dotSize, dotSize)
        )
    }
}

@Composable
fun ThreeBagsRowDisplay(
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleBagPixelArt(isSelected = selectedIndex == 0, pixelSize = 5.5.dp)
        SingleBagPixelArt(isSelected = selectedIndex == 1, pixelSize = 5.5.dp)
        SingleBagPixelArt(isSelected = selectedIndex == 2, pixelSize = 5.5.dp)
    }
}
