package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.common.distanceTo
import ru.iandreyshev.cglab3.common.findRotationAngle
import kotlin.math.sqrt

data class StickInfo(
    val center: Offset,
    val angle: Float,
    val percent: Float,
    val normalized: Offset
) {

    companion object {
        fun create(
            center: Offset,
            radius: Float,
            position: Offset
        ): StickInfo {
            val stickPosition = position - center
            // Нормализуем направляющий вектор
            val length = sqrt(stickPosition.x * stickPosition.x + stickPosition.y * stickPosition.y)
            val unitDx = stickPosition.x / length
            val unitDy = stickPosition.y / length

            // Точка пересечения на окружности
            val intersectionX = center.x + unitDx * radius
            val intersectionY = center.y + unitDy * radius

            val stickCenter = when {
                center.distanceTo(position) >= radius -> Offset(intersectionX, intersectionY)
                else -> position
            }

            val normalized = Offset(unitDx, -unitDy)
            val percent = center.distanceTo(stickCenter) / radius

            return StickInfo(
                center = stickCenter,
                angle = findRotationAngle(normalized) - 90,
                percent = center.distanceTo(stickCenter) / radius,
                normalized = normalized
            )
        }
    }

}
