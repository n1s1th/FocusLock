package com.focuslock.app.ui.screens.lock.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LETTER_D = intArrayOf(
    0, 1, 1, 0,
    1, 0, 0, 1,
    1, 0, 0, 1,
    1, 0, 0, 1,
    1, 1, 1, 0
)

private val LETTER_O = intArrayOf(
    1, 1, 1, 1,
    1, 0, 0, 1,
    1, 0, 0, 1,
    1, 0, 0, 1,
    1, 1, 1, 1
)

private val LETTER_N = intArrayOf(
    1, 0, 0, 1,
    1, 1, 0, 1,
    1, 0, 1, 1,
    1, 0, 0, 1,
    1, 0, 0, 1
)

private val LETTER_E = intArrayOf(
    1, 1, 1, 1,
    1, 0, 0, 0,
    1, 1, 1, 0,
    1, 0, 0, 0,
    1, 1, 1, 1
)

private val DONE_LETTERS = listOf(LETTER_D, LETTER_O, LETTER_N, LETTER_E)
private const val LETTER_WIDTH = 4
private const val LETTER_HEIGHT = 5
private const val LETTER_SPACING = 1 // in blocks

@Composable
fun DonePixelArtView(
    modifier: Modifier = Modifier,
    blockSize: Dp = 14.dp,
    color: Color = Color.White
) {
    val totalCols = DONE_LETTERS.size * LETTER_WIDTH + (DONE_LETTERS.size - 1) * LETTER_SPACING
    val totalWidth = blockSize * totalCols
    val totalHeight = blockSize * LETTER_HEIGHT

    Canvas(modifier = modifier.size(totalWidth, totalHeight)) {
        val px = blockSize.toPx()
        var colOffset = 0

        for (letter in DONE_LETTERS) {
            for (row in 0 until LETTER_HEIGHT) {
                for (col in 0 until LETTER_WIDTH) {
                    val index = row * LETTER_WIDTH + col
                    if (letter[index] == 1) {
                        drawRect(
                            color = color,
                            topLeft = Offset((colOffset + col) * px, row * px),
                            size = Size(px, px)
                        )
                    }
                }
            }
            colOffset += LETTER_WIDTH + LETTER_SPACING
        }
    }
}
