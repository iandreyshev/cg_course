package ru.iandreyshev.cglab3.common

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

fun Offset.distanceTo(offset: Offset): Float {
    val dx = offset.x - x
    val dy = offset.y - y
    return sqrt(dx * dx + dy * dy)
}
