package ru.iandreyshev.cglab3.asteroids.domain

import androidx.compose.ui.geometry.Offset

data class ShipState(
    val pos: Offset = Offset.Zero,
    val rotation: Float = 0f
)
