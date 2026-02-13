package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronViewModel

@Composable
fun PentagonalIcositetrahedronScreen(
    viewModel: PentagonalIcositetrahedronViewModel = viewModel { PentagonalIcositetrahedronViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change: PointerInputChange, dragAmount: Offset ->
                    if (dragAmount != Offset.Zero) {
                        viewModel.onDrag(dragAmount)
                    }
                }
            },
        factory = {
            PentagonalIcositetrahedronGLSurfaceView(it)
        },
        update = { view ->
            view.updateState(state)
        }
    )
}