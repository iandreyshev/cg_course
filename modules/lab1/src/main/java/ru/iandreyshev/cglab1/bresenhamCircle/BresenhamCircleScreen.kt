package ru.iandreyshev.cglab1.bresenhamCircle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
        drawBresenhamCircle(size.width / 2, size.height / 2, 50)
    }
}

fun DrawScope.drawBresenhamCircle(centerX: Float, centerY: Float, radius: Int) {
    val brush = SolidColor(Color.White)

    var d = 3 - 2 * radius
    var x = 0
    var y = radius

    while (x < y) {
        val sectors = listOf(
            Offset(centerX + x, centerY - y),
            Offset(centerX + y, centerY - x),
            Offset(centerX + y, centerY + x),
            Offset(centerX + x, centerY + y),
            Offset(centerX - x, centerY + y),
            Offset(centerX - y, centerY + x),
            Offset(centerX - y, centerY - x),
            Offset(centerX - x, centerY - y)
        )

        drawPoints(sectors, PointMode.Points, brush, strokeWidth = 1f)
        drawRect(brush, topLeft = sectors[0], size = Size(1f, (sectors[3] - sectors[0]).y)) // 1-4
        drawRect(brush, topLeft = sectors[1], size = Size(1f, (sectors[2] - sectors[1]).y)) // 2-3
        drawRect(brush, topLeft = sectors[4], size = Size(1f, (sectors[7] - sectors[4]).y)) // 5-8
        drawRect(brush, topLeft = sectors[5], size = Size(1f, (sectors[6] - sectors[5]).y)) // 6-7

        if (d < 0) {
            d += 4 * x + 6
            x += 1
        } else {
            d += 4 * (x - y) + 10
            x += 1
            y -= 1
        }
    }
}
