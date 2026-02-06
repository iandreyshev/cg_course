package ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.core.BaseViewModel
import kotlin.math.abs

/**
 * ViewModel для экрана пентагонального икоситетраэдра.
 * Управляет вращением, масштабированием и инерцией (fling) фигуры.
 */
class PentagonalIcositetrahedronViewModel : BaseViewModel<PentagonalIcositetrahedronState, Any>(
    initialState = PentagonalIcositetrahedronState()
) {
    /** Корутина, выполняющая анимацию инерционного вращения */
    private var flingJob: Job? = null

    /** Коэффициент затухания скорости (применяется каждый кадр) */
    private val friction = 0.95f

    /** Порог скорости, ниже которого анимация останавливается */
    private val minVelocity = 0.1f

    /**
     * Обрабатывает жест перетаскивания — обновляет угол поворота фигуры.
     * Отменяет текущую fling-анимацию, чтобы пользователь мог перехватить вращение.
     */
    fun onDrag(dragAmount: Offset) {
        flingJob?.cancel()
        updateState {
            copy(rotation = rotation + dragAmount)
        }
    }

    /**
     * Обрабатывает жест масштабирования (pinch).
     * Масштаб ограничен диапазоном [0.1, 5.0].
     */
    fun onScale(scaleFactor: Float) {
        updateState {
            copy(scale = (scale * scaleFactor).coerceIn(0.1f, 5f))
        }
    }

    /**
     * Запускает инерционное вращение после fling-жеста.
     * Скорость уменьшается каждый кадр на [friction], пока не упадёт ниже [minVelocity].
     */
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
