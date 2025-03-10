package ru.iandreyshev.cglab3.common

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.sqrt

// center, radius - параметры окружности
// x, y - направляющий вектор линии
fun findIntersectionWithCircle(
    center: Offset,
    radius: Float,
    point: Offset
): Offset {
    // Нормализуем направляющий вектор
    val length = sqrt(point.x * point.x + point.y * point.y)
    val unitDx = point.x / length
    val unitDy = point.y / length

    // Точка пересечения на окружности
    val intersectionX = center.x + unitDx * radius
    val intersectionY = center.y + unitDy * radius

    return Offset(intersectionX, intersectionY)
}

fun findRotationAngle(point: Offset): Float =
    (atan2(point.y.toDouble(), point.x.toDouble()) * 180f / Math.PI).toFloat()
