package ru.iandreyshev.cglab4.figure.presentation

import androidx.compose.ui.geometry.Offset

data class FigureState(
    val rotation: Offset = Offset.Zero,
    val velocity: Offset = Offset.Zero
)
