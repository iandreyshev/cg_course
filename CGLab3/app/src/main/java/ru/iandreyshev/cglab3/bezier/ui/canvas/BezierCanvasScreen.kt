package ru.iandreyshev.cglab3.bezier.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import ru.iandreyshev.cglab3.bezier.domain.BezierPoint
import ru.iandreyshev.cglab3.bezier.presentation.BezierState
import ru.iandreyshev.cglab3.bezier.presentation.BezierViewModel

@Composable
fun BezierCanvasScreen(
    viewModel: BezierViewModel
) {
    val state by viewModel.state

    BezierCanvas(state)
}

@Composable
private fun BezierCanvas(state: BezierState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCurve(state.curvePoints)
        drawPoint(state.p0)
        drawPoint(state.p1)
        drawPoint(state.p2)
        drawPoint(state.p3)
    }
}

private fun DrawScope.drawPoint(point: BezierPoint) {
    drawCircle(Color.Red, 20f, point.center)
}

private fun DrawScope.drawCurve(points: List<Offset>) {
    drawPoints(points, PointMode.Polygon, Color.Cyan, strokeWidth = 10f)
}
