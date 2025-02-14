package ru.iandreyshev.cglab1.hangman

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private const val HORIZONTAL_PADDING = 20
private const val LETTERS_GUTTER = 20f

@Composable
fun HangmanScreen(
    viewModel: HangmanViewModel = viewModel(),
    onNavigateToMenu: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .background(
                when (state.theme) {
                    Theme.NORMAL -> Color.White
                    Theme.STRONG -> Color.Black
                }
            )
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        when (state.theme) {
            Theme.NORMAL -> drawNormalTheme(state, textMeasurer)
            Theme.STRONG -> drawStrongTheme(state, textMeasurer)
        }
    }

    GameControls(
        onEnterChar = viewModel::onEnterChar,
        onChangeTheme = viewModel::onChangeTheme
    )

    when (state.gameState) {
        GameState.PLAYING -> Unit
        GameState.FINISHED_WIN -> {
            AlertDialogExample(
                title = "Вы выиграли!",
                onRestart = viewModel::onRestart,
                onExit = onNavigateToMenu
            )
        }

        GameState.FINISHED_LOSE -> {
            AlertDialogExample(
                title = "Вы проиграли :(",
                onRestart = viewModel::onRestart,
                onExit = onNavigateToMenu
            )
        }
    }
}

@Composable
fun GameControls(
    onEnterChar: (Char) -> Unit,
    onChangeTheme: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = HORIZONTAL_PADDING.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            TextFieldValue(),
            onValueChange = {
                onEnterChar(it.text.first())
            },
            placeholder = {
                Text(
                    "Click to open keyboard",
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                cursorColor = Color(0xFF4CAF50),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color(0xFF4CAF50),
                unfocusedContainerColor = Color(0xFF4CAF50)
            )
        )
        Spacer(Modifier.height(4.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.Black,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
            onClick = onChangeTheme
        ) {
            Text(
                "Change theme",
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun AlertDialogExample(
    title: String,
    text: String = "Перезапустить игру?",
    onExit: () -> Unit = {},
    onRestart: () -> Unit = {},
) {
    AlertDialog(
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        onDismissRequest = onExit,
        confirmButton = {
            TextButton(onRestart) {
                Text("Перезапустить")
            }
        },
        dismissButton = {
            TextButton(onExit) {
                Text("Выйти")
            }
        }
    )
}

private fun DrawScope.drawNormalTheme(state: HangmanState, textMeasurer: TextMeasurer) {
    drawHangman(state)
    translate(400f, 250f) {
        drawWord(state, textMeasurer)
    }
    translate(HORIZONTAL_PADDING.dp.toPx(), 500f) {
        drawClue(state.roundInfo.clue, textMeasurer, Color.Black)
    }
    drawLetters(state.letters, textMeasurer, LetterColors.forTheme(Theme.NORMAL))
}

private fun DrawScope.drawHangman(state: HangmanState) {
    drawRect(
        Color.Black,
        topLeft = Offset(HORIZONTAL_PADDING.dp.toPx(), 20.dp.toPx()),
        Size(20f, 400f)
    )
    drawRect(
        Color.Black,
        topLeft = Offset(HORIZONTAL_PADDING.dp.toPx(), 20.dp.toPx()),
        Size(200f, 20f)
    )

    val drawHangmanOperations = listOf(
        {
            drawRect(Color.Blue, Offset(200f, 20.dp.toPx()), Size(5f, 70f))
        },
        {
            drawCircle(Color.Blue, center = Offset(200f, 20.dp.toPx() + 70f), radius = 25f)
        },
        {
            drawRect(Color.Blue, Offset(190f, 20.dp.toPx() + 70f), Size(15f, 130f))
        },
        {
            val path = Path()
            path.moveTo(195f, 20.dp.toPx() + 190f)
            path.lineTo(125f, 20.dp.toPx() + 270f)
            path.close()
            drawPath(path, Color.Blue, style = Stroke(width = 10f))
        },
        {
            val path = Path()
            path.moveTo(195f, 20.dp.toPx() + 190f)
            path.lineTo(265f, 20.dp.toPx() + 270f)
            path.close()
            drawPath(path, Color.Blue, style = Stroke(width = 10f))
        },
        {
            val path = Path()
            path.moveTo(195f, 20.dp.toPx() + 90f)
            path.lineTo(125f, 20.dp.toPx() + 160f)
            path.close()
            drawPath(path, Color.Blue, style = Stroke(width = 10f))
        },
        {
            val path = Path()
            path.moveTo(195f, 20.dp.toPx() + 90f)
            path.lineTo(265f, 20.dp.toPx() + 160f)
            path.close()
            drawPath(path, Color.Blue, style = Stroke(width = 10f))
        },
    )

    drawHangmanOperations.subList(0, state.badUsedLetters.size)
        .forEach { it() }
}

private fun DrawScope.drawWord(state: HangmanState, textMeasurer: TextMeasurer) {
    var pos = Offset.Zero
    state.roundInfo.word
        .mapNotNull { state.letters[it] }
        .map {
            val color = when (it.state) {
                LetterState.UNUSED -> if (state.theme == Theme.NORMAL) Color.White else Color.Black
                LetterState.GOOD_USED -> Color.Green
                LetterState.BAD_USED -> Color.Red
            }
            val textStyle =
                TextStyle.Default.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            textMeasurer.measure(it.char.toString(), style = textStyle)
        }.forEach {
            drawText(it, topLeft = pos)
            drawRect(Color.Blue, Offset(pos.x, pos.y + 100f), Size(it.size.width.toFloat(), 5f))
            pos = Offset(pos.x + LETTERS_GUTTER + it.size.width, pos.y)
        }
}

private fun DrawScope.drawLetters(
    letters: Map<Char, Letter>,
    textMeasurer: TextMeasurer,
    colors: LetterColors
) {
    var position = Offset(HORIZONTAL_PADDING.dp.toPx(), 700f)
    val maxX = size.width - HORIZONTAL_PADDING.dp.toPx()

    letters.map {
        val style = TextStyle.Default
            .copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors[it.value.state])
        textMeasurer.measure(it.value.char.toString(), style = style)
    }.forEach {
        if (position.x + LETTERS_GUTTER + it.size.width > maxX) {
            position = Offset(HORIZONTAL_PADDING.dp.toPx(), position.y + 100f)
        }
        drawText(it, topLeft = position)
        position = Offset(position.x + LETTERS_GUTTER + it.size.width, position.y)
    }
}

private fun DrawScope.drawStrongTheme(state: HangmanState, textMeasurer: TextMeasurer) {
    translate(HORIZONTAL_PADDING.dp.toPx(), 200f) {
        drawWord(state, textMeasurer)
    }
    translate(HORIZONTAL_PADDING.dp.toPx(), 100f) {
        drawClue(state.roundInfo.clue, textMeasurer, Color.White)
    }
    drawLettersHistory(state, textMeasurer)
    drawAttemptsCount(state, textMeasurer)
    drawLetters(state.letters, textMeasurer, LetterColors.forTheme(Theme.STRONG))
}

private fun DrawScope.drawLettersHistory(state: HangmanState, textMeasurer: TextMeasurer) {
    drawText(
        textMeasurer.measure("История"),
        topLeft = Offset(HORIZONTAL_PADDING.dp.toPx(), 350f),
        color = Color.White
    )

    var position = Offset(HORIZONTAL_PADDING.dp.toPx(), 400f)
    val maxX = size.width - HORIZONTAL_PADDING.dp.toPx()

    state.history.map {
        val color = when (it.state) {
            LetterState.GOOD_USED -> Color.Green
            else -> Color.Red
        }
        val style =
            TextStyle.Default.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        textMeasurer.measure(it.char.toString(), style = style)
    }.forEach {
        if (position.x + LETTERS_GUTTER + it.size.width > maxX) {
            position = Offset(HORIZONTAL_PADDING.dp.toPx(), position.y + 100f)
        }
        drawText(it, topLeft = position)
        position = Offset(position.x + LETTERS_GUTTER + it.size.width, position.y)
    }
}

private fun DrawScope.drawAttemptsCount(state: HangmanState, textMeasurer: TextMeasurer) {
    drawText(
        textMeasurer.measure("Осталось попыток: ${MAX_BAD_LETTERS - state.badUsedLetters.count()}"),
        topLeft = Offset(HORIZONTAL_PADDING.dp.toPx(), 550f),
        color = Color.White
    )
}

private fun DrawScope.drawClue(clue: String, textMeasurer: TextMeasurer, color: Color) {
    val constraints = Constraints(maxWidth = (size.width - 2 * HORIZONTAL_PADDING).toInt())
    val style = TextStyle.Default.copy(fontSize = 16.sp, color = color)
    val measureResult = textMeasurer.measure(clue, constraints = constraints, style = style)
    drawText(measureResult, topLeft = Offset.Zero)
}

// 374