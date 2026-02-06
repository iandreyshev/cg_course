package ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation

import androidx.compose.ui.geometry.Offset

/**
 * Состояние экрана визуализации пентагонального икоситетраэдра.
 *
 * @param rotation текущий поворот фигуры (x — вокруг вертикальной оси, y — вокруг горизонтальной)
 * @param velocity текущая скорость инерционного вращения после fling-жеста
 * @param scale масштаб отображения фигуры (от 0.1 до 5.0)
 */
data class PentagonalIcositetrahedronState(
    val rotation: Offset = Offset.Zero,
    val velocity: Offset = Offset.Zero,
    val scale: Float = 0.5f,
)
