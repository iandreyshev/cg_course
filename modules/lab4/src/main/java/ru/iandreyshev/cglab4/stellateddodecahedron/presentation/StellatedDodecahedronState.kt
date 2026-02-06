package ru.iandreyshev.cglab4.stellateddodecahedron.presentation

import androidx.compose.ui.geometry.Offset

data class StellatedDodecahedronState(
    val rotation: Offset = Offset.Zero,
    val velocity: Offset = Offset.Zero,
    val scale: Float = 0.5f,
)
