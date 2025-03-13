package ru.iandreyshev.cglab3.asteroids.domain

import androidx.compose.ui.geometry.Offset

data class BulletState(
    val position: Offset,
    val direction: Offset,
    val speed: Float
)
