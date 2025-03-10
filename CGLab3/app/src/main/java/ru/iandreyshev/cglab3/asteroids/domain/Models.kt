package ru.iandreyshev.cglab3.asteroids.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

data class ShipState(
    val pos: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val velocity: Float = 0f
)

data class Enemy(
    val pos: Offset,
    val radius: Float,
    val rotation: Float,
    val velocity: Float
)

data class Bullet(
    val pos: Offset,
    val rotation: Float,
    val size: Size
)

data class Explosion(
    val pos: Offset,
    val particles: List<Particle>
)

data class Particle(
    val pos: Offset,
)
