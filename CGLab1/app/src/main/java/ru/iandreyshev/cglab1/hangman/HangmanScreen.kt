package ru.iandreyshev.cglab1.hangman

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HangmanScreen(
    viewModel: HangmanViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()


    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {

    }
}
