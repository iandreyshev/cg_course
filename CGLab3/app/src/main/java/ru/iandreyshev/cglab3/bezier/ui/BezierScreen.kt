package ru.iandreyshev.cglab3.bezier.ui

import androidx.compose.runtime.Composable
import ru.iandreyshev.cglab3.bezier.presentation.BezierViewModel
import ru.iandreyshev.cglab3.bezier.ui.canvas.BezierCanvasScreen
import ru.iandreyshev.cglab3.bezier.ui.openGL.BezierOpenGLScreen

@Composable
fun BezierScreen(
    isCanvas: Boolean = false,
    viewModel: BezierViewModel
) {
    when {
        isCanvas -> BezierCanvasScreen(viewModel)
        else -> BezierOpenGLScreen(viewModel)
    }
}
