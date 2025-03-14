package ru.iandreyshev.cglab3.asteroids.presentation

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.core.distanceTo
import ru.iandreyshev.core.findRotationAngle
import ru.iandreyshev.core.normalize

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
            // Нормализуем направляющий вектор
            val normalized = (position - center).normalize()

            // Точка пересечения на окружности
            val intersectionX = center.x + normalized.x * radius
            val intersectionY = center.y + normalized.y * radius

            val stickCenter = when {
                center.distanceTo(position) >= radius -> Offset(intersectionX, intersectionY)
                else -> position
            }

            val normalizedReverted = normalized.copy(y = -normalized.y)
            val percent = center.distanceTo(stickCenter) / radius

            return StickInfo(
                center = stickCenter,
                angle = findRotationAngle(normalizedReverted) - 90,
                percent = percent,
                normalized = normalizedReverted
            )
        }
    }

}
