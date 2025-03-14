package ru.iandreyshev.cglab3.asteroids.domain

import androidx.compose.ui.geometry.Offset

data class ParticleState(
    val startPosition: Offset,
    val position: Offset,
    val direction: Offset
)
