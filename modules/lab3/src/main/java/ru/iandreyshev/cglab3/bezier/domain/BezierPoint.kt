package ru.iandreyshev.cglab3.bezier.domain

import androidx.compose.ui.geometry.Offset
import java.util.UUID

const val POINT_RADIUS = 50f

data class BezierPoint(
    val id: String = UUID.randomUUID().toString(),
    val position: Offset = Offset.Zero
) {
    val center = position + Offset(POINT_RADIUS, POINT_RADIUS)
}
