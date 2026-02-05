package ru.iandreyshev.cglab3.bezier.presentation

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.bezier.domain.BezierPoint

data class BezierState(
    val p0: BezierPoint = BezierPoint(),
    val p1: BezierPoint = BezierPoint(),
    val p2: BezierPoint = BezierPoint(),
    val p3: BezierPoint = BezierPoint(),
    val curvePoints: List<Offset> = emptyList(),
    val detalization: Float = MAX_DETALIZATION.toFloat()
) {

    companion object {
        const val MAX_DETALIZATION = 10
    }

}
