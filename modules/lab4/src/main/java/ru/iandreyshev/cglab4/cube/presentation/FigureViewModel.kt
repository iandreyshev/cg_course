package ru.iandreyshev.cglab4.cube.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.core.BaseViewModel
import kotlin.math.abs

class FigureViewModel : BaseViewModel<FigureState, Any>(
    initialState = FigureState()
) {
    private var flingJob: Job? = null
    private val friction = 0.95f
    private val minVelocity = 0.1f

    fun onDrag(dragAmount: Offset) {
        flingJob?.cancel()
        updateState {
            copy(rotation = rotation + dragAmount)
        }
    }

    fun onScale(scaleFactor: Float) {
        updateState {
            copy(scale = (scale * scaleFactor).coerceIn(0.1f, 5f))
        }
    }

    fun onFling(velocity: Offset) {
        val scaledVelocity = velocity / 50f

        flingJob?.cancel()
        flingJob = viewModelScope.launch {
            updateState { copy(velocity = scaledVelocity) }

            while (true) {
                val currentVelocity = stateValue.velocity

                if (abs(currentVelocity.x) < minVelocity && abs(currentVelocity.y) < minVelocity) {
                    updateState { copy(velocity = Offset.Zero) }
                    break
                }

                updateState {
                    copy(
                        rotation = rotation + Offset(currentVelocity.x, currentVelocity.y),
                        velocity = currentVelocity * friction
                    )
                }

                delay(16)
            }
        }
    }
}
