package ru.iandreyshev.cglab1.bresenhamCircle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun BresenhamCircleScreen() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        drawBresenhamCircle(size.width / 2, size.height / 2, 10)
    }
}

fun DrawScope.drawBresenhamCircle(centerX: Float, centerY: Float, radius: Int) {
    val brush = SolidColor(Color.White)
    val pointsToDraw = mutableListOf<Offset>()

    var x = 0
    var y = radius
    var gap = 0
    var delta = 2 - 2 * radius
    var stepNumber = 0

    while (y >= 0) {
        println("--- Шаг номер $stepNumber ---")
        println("x = $x")
        println("y = $y")
        println("gap = $gap")
        println("delta = $delta")
        stepNumber++

        pointsToDraw.apply {
            add(Offset(centerX + x, centerY + y))
            add(Offset(centerX + x, centerY - y))
            add(Offset(centerX - x, centerY - y))
            add(Offset(centerX - x, centerY + y))
            drawPoints(this, PointMode.Points, brush, strokeWidth = 2f)
            clear()
        }

        gap = 2 * (delta + y) - 1

        if (delta < 0 && gap <= 0) {
            x++
            delta += 2 * x + 1
            continue
        }

        if (delta > 0 && gap > 0) {
            y--
            delta -= 2 * y + 1
            continue
        }

        x++
        delta += 2 * (x - y)
        y--
    }
}
