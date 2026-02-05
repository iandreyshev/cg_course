package ru.iandreyshev.cglab1.initials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel

const val CANVAS_PADDING = 100f
const val LETTERS_GUTTER = 60f

const val LETTER_HEIGHT = 360f
const val LINE_WIDTH = 50f

@Composable
fun InitialsScreen(
    displaySize: IntSize,
    viewModel: InitialsViewModel = viewModel {
        InitialsViewModel(
            screenWidth = displaySize.width,
            screenHeight = displaySize.height
        )
    }
) {
    val state by viewModel.state.collectAsState()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        drawBackground()
        drawLetter1(state.letter1)
        drawLetter2(state.letter2)
        drawLetter3(state.letter3)
    }
}

private fun DrawScope.drawBackground() {
    drawRect(
        color = Color.Black,
        size = Size(width = size.width, height = size.height),
    )
}

private fun DrawScope.drawLetter1(letter: InitialsLetter) {
    val color = Color.White
    val topLeft = Offset(letter.xPosition, letter.yPosition)
    drawRect(color, topLeft = topLeft, size = Size(LINE_WIDTH, LETTER_HEIGHT))

    drawRect(color, topLeft = topLeft, size = Size(letter.width, LINE_WIDTH))

    drawRect(
        color,
        topLeft = Offset(topLeft.x + letter.width - LINE_WIDTH, topLeft.y),
        size = Size(LINE_WIDTH, LETTER_HEIGHT)
    )

    drawRect(
        color,
        topLeft = Offset(letter.xPosition, letter.yPosition + LETTER_HEIGHT / 2),
        size = Size(letter.width, LINE_WIDTH)
    )
}

private fun DrawScope.drawLetter2(letter: InitialsLetter) {
    val color = Color.Blue
    val topLeft = Offset(letter.xPosition, letter.yPosition)
    drawRect(color, topLeft = topLeft, size = Size(LINE_WIDTH, LETTER_HEIGHT))

    val path = Path()
    path.moveTo(letter.xPosition, letter.yPosition + LETTER_HEIGHT)
    path.lineTo(letter.xPosition + LINE_WIDTH, letter.yPosition + LETTER_HEIGHT)
    path.lineTo(letter.xPosition + letter.width, letter.yPosition)
    path.lineTo(letter.xPosition + letter.width - LINE_WIDTH, letter.yPosition)
    path.lineTo(letter.xPosition, letter.yPosition + LETTER_HEIGHT)
    path.close()
    drawPath(path, color)

    drawRect(
        color,
        topLeft = Offset(topLeft.x + letter.width - LINE_WIDTH, topLeft.y),
        size = Size(LINE_WIDTH, LETTER_HEIGHT)
    )
}

private fun DrawScope.drawLetter3(letter: InitialsLetter) {
    val color = Color.Red
    val topLeft = Offset(letter.xPosition, letter.yPosition)
    drawRect(color, topLeft = topLeft, size = Size(LINE_WIDTH, LETTER_HEIGHT))

    val path = Path()
    path.moveTo(letter.xPosition, letter.yPosition + LETTER_HEIGHT)
    path.lineTo(letter.xPosition + LINE_WIDTH, letter.yPosition + LETTER_HEIGHT)
    path.lineTo(letter.xPosition + letter.width, letter.yPosition)
    path.lineTo(letter.xPosition + letter.width - LINE_WIDTH, letter.yPosition)
    path.lineTo(letter.xPosition, letter.yPosition + LETTER_HEIGHT)
    path.close()
    drawPath(path, color)

    drawRect(
        color,
        topLeft = Offset(topLeft.x + letter.width - LINE_WIDTH, topLeft.y),
        size = Size(LINE_WIDTH, LETTER_HEIGHT)
    )
}
