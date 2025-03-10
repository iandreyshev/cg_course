package ru.iandreyshev.cglab3.common

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun Offset.distanceTo(offset: Offset): Float {
    val dx = offset.x - x
    val dy = offset.y - y
    return sqrt(dx * dx + dy * dy)
}


fun Offset.rotate(degrees: Double): Offset {
    val radians = Math.toRadians(degrees) // Конвертируем градусы в радианы
    val cosTheta = cos(radians)
    val sinTheta = sin(radians)

    val newX = x * cosTheta - y * sinTheta
    val newY = x * sinTheta + y * cosTheta

    return Offset(newX.toFloat(), newY.toFloat())
}
