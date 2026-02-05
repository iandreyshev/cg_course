package ru.iandreyshev.cglab4.stellateddodecahedron.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.figure.presentation.FigureViewModel

@Composable
fun StellatedDodecahedronScreen(
    viewModel: FigureViewModel = viewModel { FigureViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val velocityTracker = VelocityTracker()

                    val down = awaitFirstDown()
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        if (!change.pressed) {
                            val velocity = velocityTracker.calculateVelocity()
                            viewModel.onFling(Offset(velocity.x, velocity.y))
                            break
                        }

                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )

                        val dragAmount = change.positionChange()
                        if (dragAmount != Offset.Zero) {
                            viewModel.onDrag(dragAmount)
                            change.consume()
                        }
                    }
                }
            },
        factory = {
            StellatedDodecahedronGLSurfaceView(it)
        },
        update = { view ->
            view.updateState(state)
        }
    )
}
