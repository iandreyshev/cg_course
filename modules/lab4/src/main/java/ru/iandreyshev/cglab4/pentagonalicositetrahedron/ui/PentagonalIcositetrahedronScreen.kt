package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

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
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronViewModel
import kotlin.math.sqrt

/**
 * Compose-экран визуализации пентагонального икоситетраэдра.
 *
 * Экран занимает всю доступную область и обрабатывает жесты пользователя:
 * - Перетаскивание одним пальцем — вращение фигуры
 * - Щипок двумя пальцами (pinch) — масштабирование
 * - Бросок (fling) — инерционное вращение после отпускания пальца
 *
 * Внутри используется [AndroidView] для встраивания OpenGL-поверхности
 * [PentagonalIcositetrahedronGLSurfaceView] в Compose-иерархию.
 */
@Composable
fun PentagonalIcositetrahedronScreen(
    viewModel: PentagonalIcositetrahedronViewModel = viewModel { PentagonalIcositetrahedronViewModel() }
) {
    // Подписка на состояние ViewModel — при изменении state происходит рекомпозиция
    val state by viewModel.state

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            // Обработка жестов касания: вращение, масштабирование и инерция
            .pointerInput(Unit) {
                // awaitEachGesture обрабатывает каждый жест от нажатия до отпускания
                awaitEachGesture {
                    // Трекер скорости для вычисления fling-скорости при отпускании пальца
                    val velocityTracker = VelocityTracker()
                    // Предыдущее расстояние между двумя пальцами (для pinch-жеста)
                    var previousDistance = 0f
                    // Флаг: идёт ли сейчас жест масштабирования (pinch)
                    var isPinching = false

                    // Ожидаем первое касание экрана
                    val down = awaitFirstDown()
                    down.consume()

                    // Цикл обработки всех событий указателя до завершения жеста
                    while (true) {
                        val event = awaitPointerEvent()
                        // Фильтруем только нажатые пальцы (те, что касаются экрана)
                        val pressed = event.changes.filter { it.pressed }

                        // Все пальцы отпущены — жест завершён
                        if (pressed.isEmpty()) {
                            // Если это был не pinch, запускаем инерционное вращение
                            if (!isPinching) {
                                val velocity = velocityTracker.calculateVelocity()
                                viewModel.onFling(Offset(velocity.x, velocity.y))
                            }
                            break
                        }

                        // Два и более пальцев — жест масштабирования (pinch)
                        if (pressed.size >= 2) {
                            isPinching = true
                            // Позиции двух пальцев
                            val p1 = pressed[0].position
                            val p2 = pressed[1].position
                            // Вычисляем расстояние между пальцами (евклидова норма)
                            val dx = p1.x - p2.x
                            val dy = p1.y - p2.y
                            val distance = sqrt(dx * dx + dy * dy)

                            // Если уже есть предыдущее расстояние — вычисляем коэффициент масштаба
                            if (previousDistance > 0f) {
                                val scaleFactor = distance / previousDistance
                                viewModel.onScale(scaleFactor)
                            }
                            previousDistance = distance

                            // Отмечаем все события как обработанные
                            event.changes.forEach { it.consume() }
                        } else if (!isPinching) {
                            // Один палец и не было pinch — жест вращения (drag)
                            val change = pressed[0]
                            // Добавляем позицию в трекер для расчёта скорости fling
                            velocityTracker.addPosition(
                                change.uptimeMillis,
                                change.position
                            )

                            // Вычисляем смещение пальца с прошлого события
                            val dragAmount = change.positionChange()
                            if (dragAmount != Offset.Zero) {
                                // Передаём смещение во ViewModel для обновления угла поворота
                                viewModel.onDrag(dragAmount)
                                change.consume()
                            }
                        }
                    }
                }
            },
        // Фабрика создания Android View — вызывается один раз при первой композиции
        factory = {
            PentagonalIcositetrahedronGLSurfaceView(it)
        },
        // Вызывается при каждой рекомпозиции — передаём актуальное состояние в GL-поверхность
        update = { view ->
            view.updateState(state)
        }
    )
}
