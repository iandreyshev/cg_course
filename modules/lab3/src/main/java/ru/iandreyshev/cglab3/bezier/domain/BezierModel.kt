package ru.iandreyshev.cglab3.bezier.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.pow

class BezierModel {
    fun getPoints(
        p0: BezierPoint,
        p1: BezierPoint,
        p2: BezierPoint,
        p3: BezierPoint,
        detalization: Int = MAX_DETALIZATION
    ) = buildList {
        val detalizationStep = 1f / detalization
        var t = 0f

        while (true) {
            val newT = t.coerceAtMost(1f)

            this += Offset(
                getCoordinate(newT, p0.center.x, p1.center.x, p2.center.x, p3.center.x),
                getCoordinate(newT, p0.center.y, p1.center.y, p2.center.y, p3.center.y)
            )

            if (t >= 1f) {
                break
            }

            t += detalizationStep
        }
    }

    private fun getCoordinate(t: Float, p0: Float, p1: Float, p2: Float, p3: Float) =
        (1 - t).pow(3) * p0 + 3 * t * (1 - t).pow(2) * p1 + 3 * t.pow(2) * (1 - t) * p2 + t.pow(3) * p3

    companion object {
        const val MAX_DETALIZATION = 50
    }
}
