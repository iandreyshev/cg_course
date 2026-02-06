package ru.iandreyshev.cglab4.stellateddodecahedron.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.stellateddodecahedron.presentation.StellatedDodecahedronViewModel
import kotlin.math.sqrt

@Composable
fun StellatedDodecahedronScreen(
    viewModel: StellatedDodecahedronViewModel = viewModel { StellatedDodecahedronViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val velocityTracker = VelocityTracker()
                    var previousDistance = 0f
                    var isPinching = false

                    val down = awaitFirstDown()
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }

                        if (pressed.isEmpty()) {
                            if (!isPinching) {
                                val velocity = velocityTracker.calculateVelocity()
                                viewModel.onFling(Offset(velocity.x, velocity.y))
                            }
                            break
                        }

                        if (pressed.size >= 2) {
                            isPinching = true
                            val p1 = pressed[0].position
                            val p2 = pressed[1].position
                            val dx = p1.x - p2.x
                            val dy = p1.y - p2.y
                            val distance = sqrt(dx * dx + dy * dy)

                            if (previousDistance > 0f) {
                                val scaleFactor = distance / previousDistance
                                viewModel.onScale(scaleFactor)
                            }
                            previousDistance = distance

                            event.changes.forEach { it.consume() }
                        } else if (!isPinching) {
                            val change = pressed[0]
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
