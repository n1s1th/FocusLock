package com.focuslock.app.ui.screens.home.components

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
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.SecondaryGray

private val DIGIT_MATRICES = mapOf(
    0 to listOf(
        1, 1, 1,
        1, 0, 1,
        1, 0, 1,
        1, 0, 1,
        1, 1, 1
    ),
    1 to listOf(
        0, 0, 1,
        0, 0, 1,
        0, 0, 1,
        0, 0, 1,
        0, 0, 1
    ),
    2 to listOf(
        1, 1, 1,
        0, 0, 1,
        1, 1, 1,
        1, 0, 0,
        1, 1, 1
    ),
    3 to listOf(
        1, 1, 1,
        0, 0, 1,
        1, 1, 1,
        0, 0, 1,
        1, 1, 1
    ),
    4 to listOf(
        1, 0, 1,
        1, 0, 1,
        1, 1, 1,
        0, 0, 1,
        0, 0, 1
    ),
    5 to listOf(
        1, 1, 1,
        1, 0, 0,
        1, 1, 1,
        0, 0, 1,
        1, 1, 1
    ),
    6 to listOf(
        1, 1, 1,
        1, 0, 0,
        1, 1, 1,
        1, 0, 1,
        1, 1, 1
    ),
    7 to listOf(
        1, 1, 1,
        0, 0, 1,
        0, 0, 1,
        0, 0, 1,
        0, 0, 1
    ),
    8 to listOf(
        1, 1, 1,
        1, 0, 1,
        1, 1, 1,
        1, 0, 1,
        1, 1, 1
    ),
    9 to listOf(
        1, 1, 1,
        1, 0, 1,
        1, 1, 1,
        0, 0, 1,
        1, 1, 1
    )
)

@Composable
fun SquareDigit(
    digit: Int,
    blockSize: Dp = 16.dp,
    color: Color = CharcoalPrimary,
    modifier: Modifier = Modifier
) {
    val matrix = DIGIT_MATRICES[digit.coerceIn(0, 9)] ?: DIGIT_MATRICES[0]!!
    val width = blockSize * 3
    val height = blockSize * 5

    Canvas(modifier = modifier.size(width, height)) {
        val pxBlock = blockSize.toPx()
        for (row in 0 until 5) {
            for (col in 0 until 3) {
                if (matrix[row * 3 + col] == 1) {
                    drawRect(
                        color = color,
                        topLeft = Offset(col * pxBlock, row * pxBlock),
                        size = Size(pxBlock, pxBlock)
                    )
                }
            }
        }
    }
}

@Composable
fun SquareColon(
    blockSize: Dp = 16.dp,
    color: Color = SecondaryGray,
    modifier: Modifier = Modifier
) {
    val width = blockSize
    val height = blockSize * 5

    Canvas(modifier = modifier.size(width, height)) {
        val pxBlock = blockSize.toPx()
        // Top dot at row 1
        drawRect(
            color = color,
            topLeft = Offset(0f, 1 * pxBlock),
            size = Size(pxBlock, pxBlock)
        )
        // Bottom dot at row 3
        drawRect(
            color = color,
            topLeft = Offset(0f, 3 * pxBlock),
            size = Size(pxBlock, pxBlock)
        )
    }
}

@Composable
fun DigitalTimerDisplay(
    hours: Int,
    minutes: Int,
    modifier: Modifier = Modifier,
    blockSize: Dp = 16.dp,
    digitColor: Color = CharcoalPrimary,
    colonColor: Color = SecondaryGray,
    digitSpacing: Dp? = null
) {
    val h1 = (hours / 10).coerceIn(0, 9)
    val h2 = (hours % 10).coerceIn(0, 9)
    val m1 = (minutes / 10).coerceIn(0, 9)
    val m2 = (minutes % 10).coerceIn(0, 9)

    val spacing = digitSpacing ?: (blockSize * 0.75f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquareDigit(digit = h1, blockSize = blockSize, color = digitColor)
        SquareDigit(digit = h2, blockSize = blockSize, color = digitColor)
        SquareColon(blockSize = blockSize, color = colonColor)
        SquareDigit(digit = m1, blockSize = blockSize, color = digitColor)
        SquareDigit(digit = m2, blockSize = blockSize, color = digitColor)
    }
}

