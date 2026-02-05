package ru.iandreyshev.cglab4.figure.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.figure.presentation.FigureViewModel

@Composable
fun FigureScreen(
    viewModel: FigureViewModel = viewModel { FigureViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .pointerInput(true) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        viewModel.onDrag(dragAmount)
                    }
                )
            },
        factory = {
            FigureGLSurfaceView(it)
        },
        update = { view ->
            view.updateState(state)
        }
    )
}
