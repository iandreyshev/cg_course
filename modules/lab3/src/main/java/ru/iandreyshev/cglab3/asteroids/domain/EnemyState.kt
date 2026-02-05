package ru.iandreyshev.cglab3.asteroids.domain

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.asteroids.domain.EnemyState.Level.*

data class EnemyState(
    val position: Offset,
    val level: Level,
    val rotation: Float,
    val rotationSpeed: Float,
    val direction: Offset,
    val speed: Float,
    val health: Int
) {
    enum class Level {
        REGULAR,
        BOSS
    }

    companion object {
        fun random(
            position: Offset,
            level: Level = when {
                (1..100).random() < AstConst.Enemy.SPAWN_BOSS_PROBABILITY_PERCENT -> BOSS
                else -> REGULAR
            },
            rotation: Float = (0..360).random().toFloat(),
            rotationSpeed: Float = (AstConst.Enemy.MIN_ROTATION_SPEED_DEG..AstConst.Enemy.MAX_ROTATION_SPEED_DEG).random()
                .toFloat(),
            direction: Offset,
            speed: Float = (AstConst.Enemy.MIN_SPEED..AstConst.Enemy.MAX_SPEED).random().toFloat()
        ) = EnemyState(
            position = position,
            level = level,
            rotation = rotation,
            rotationSpeed = rotationSpeed,
            direction = direction,
            speed = speed,
            health = when (level) {
                REGULAR -> AstConst.Enemy.HEALTH_REGULAR
                BOSS -> AstConst.Enemy.HEALTH_BOSS
            }
        )
    }

}


