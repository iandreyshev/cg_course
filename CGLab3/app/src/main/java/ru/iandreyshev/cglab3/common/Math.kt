package ru.iandreyshev.cglab3.common

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

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

fun randomPointOnCircle(radius: Float): Offset {
    // Случайный угол от 0 до 2π
    val angle = Random.nextDouble(0.0, 2.0 * PI)

    // Координаты точки на окружности
    val x = radius * cos(angle).toFloat()
    val y = radius * sin(angle).toFloat()

    return Offset(x, y)
}

fun circlesIntersect(center1: Offset, r1: Float, center2: Offset, r2: Float) =
    center1.distanceTo(center2) <= r1 + r2

fun degreesToNormalizedVector(angleDegrees: Float): Offset {
    val angleRadians = Math.toRadians(angleDegrees.toDouble()) // Перевод градусов в радианы
    val x = cos(angleRadians).toFloat()
    val y = sin(angleRadians).toFloat()
    return Offset(x, y) // Возвращаем нормализованный вектор (единичной длины)
}