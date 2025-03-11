package ru.iandreyshev.cglab3.asteroids.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

data class ShipState(
    val pos: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val velocity: Float = 0f
)

data class EnemyState(
    val pos: Offset,
    val radius: Int,
    val rotation: Float,
    val speed: Float,
    val direction: Offset
)

data class BulletState(
    val pos: Offset,
    val rotation: Float,
    val size: Size
)
