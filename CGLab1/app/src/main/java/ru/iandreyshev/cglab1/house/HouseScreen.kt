package ru.iandreyshev.cglab1.house

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HouseScreen(
    viewModel: HouseViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()


    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = viewModel::onDragStart,
                    onDrag = { change, _ ->
                        viewModel.onDragChange(change.position)
                    },
                    onDragEnd = viewModel::onDragStop
                )
            },
    ) {
        drawSky()
        drawStars()
        drawMoon()
        drawGround()
        drawFence()
        drawFlag()
        drawHouse(state)
    }
}


private fun DrawScope.drawSky() {
    val skyCenter = Offset(140f, 140f)
    val brush = Brush.radialGradient(
        colors = listOf(Color(0xFF3B49A1), Color(0xFF060B3A)),
        center = skyCenter,
        radius = 1300f
    )
    drawCircle(brush, center = skyCenter, radius = 10000f)
}

private fun DrawScope.drawGround() {
    val groundCenter = Offset(size.width / 2, 3700f)
    val brush = Brush.radialGradient(
        colors = listOf(Color(0xFF00FF02), Color(0xFF1D8D3B)),
        center = groundCenter,
        radius = 2000f
    )
    drawCircle(brush, center = groundCenter, radius = 2000f)
}

private fun DrawScope.drawHouse(state: HouseState) {
    drawNozzle(state)

    drawRect(Color(0xFFC97C05), topLeft = Offset(state.posX, state.posY), size = Size(350f, 180f))

    val path = Path()
    path.moveTo(state.posX - 60f, state.posY)
    path.lineTo(state.posX + 350f / 2, state.posY - 150f)
    path.lineTo(state.posX + 350f + 60f, state.posY)
    path.lineTo(state.posX - 60f, state.posY)
    path.close()
    drawPath(path, Color(0xFF6B6B6B))

    drawRect(Color.Cyan, topLeft = Offset(state.posX + 40f, state.posY + 40f), size = Size(60f, 75f))

    drawRect(Color.Cyan, topLeft = Offset(state.posX + 140f, state.posY + 40f), size = Size(60f, 75f))

    drawRect(Color.DarkGray, topLeft = Offset(state.posX + 240f, state.posY + 40f), size = Size(60f, 180f - 40f))
    drawCircle(Color.LightGray, center = Offset(state.posX + 260f, state.posY + 120f), radius = 6f)

    drawCircle(Color.LightGray, center = Offset(state.posX + 70f, state.posY - 120f), radius = 25f)
    drawCircle(Color.LightGray, center = Offset(state.posX + 60f, state.posY - 140f), radius = 22f)
    drawCircle(Color.LightGray, center = Offset(state.posX + 45f, state.posY - 165f), radius = 19f)
    drawCircle(Color.LightGray, center = Offset(state.posX + 25f, state.posY - 195f), radius = 16f)
    drawCircle(Color.LightGray, center = Offset(state.posX + 0f, state.posY - 230f), radius = 13f)

    drawRect(Color(0xFF4A4D47), topLeft = Offset(state.posX + 60f, state.posY - 120f), size = Size(30f, 60f))
}

private fun DrawScope.drawNozzle(state: HouseState) {
    if (!state.isDrag) {
        return
    }

    val nozzlePath = Path()
    nozzlePath.moveTo(state.posX + 350f / 2, state.posY + 50f)
    nozzlePath.lineTo(state.posX + 350f / 2 + 60f, state.posY + 240f)
    nozzlePath.lineTo(state.posX + 350f / 2 - 60f, state.posY + 240f)
    nozzlePath.lineTo(state.posX + 175f, state.posY + 50f)
    nozzlePath.close()
    drawPath(nozzlePath, Color(0xF0C7C7C7))


    val flamePath1 = Path()
    flamePath1.moveTo(state.posX + 350f / 2 + 50f, state.posY + 240f)
    flamePath1.lineTo(state.posX + 350f / 2, state.posY + 330f)
    flamePath1.lineTo(state.posX + 350f / 2 - 50f, state.posY + 240f)
    flamePath1.lineTo(state.posX + 350f / 2 + 50f, state.posY + 240f)
    flamePath1.close()
    drawPath(flamePath1, Color(0xF0FFDC21))


    val flamePath2 = Path()
    flamePath2.moveTo(state.posX + 350f / 2 + 30f, state.posY + 240f)
    flamePath2.lineTo(state.posX + 350f / 2, state.posY + 300f)
    flamePath2.lineTo(state.posX + 350f / 2 - 30f, state.posY + 240f)
    flamePath2.lineTo(state.posX + 350f / 2 + 30f, state.posY + 240f)
    flamePath2.close()
    drawPath(flamePath2, Color(0xF0FA532A))
}

private fun DrawScope.drawFence() {
    val fenceHeight = 100f
    val fenceWidth = 30f

    var itemPos = Offset(60f, 1650f)

    fun drawFenceItem(offset: Offset) {
        itemPos += offset

        val path = Path()
        path.moveTo(itemPos.x, itemPos.y + fenceHeight)
        path.lineTo(itemPos.x, itemPos.y + fenceHeight * 0.2f)
        path.lineTo(itemPos.x + fenceWidth / 2, itemPos.y)
        path.lineTo(itemPos.x + fenceWidth, itemPos.y + fenceHeight * 0.2f)
        path.lineTo(itemPos.x + fenceWidth, itemPos.y + fenceHeight)
        path.lineTo(itemPos.x, itemPos.y + fenceHeight)
        path.close()
        drawPath(path, Color.White)
    }

    repeat(12) {
        drawFenceItem(Offset(40f, 40f))
    }

    repeat(12) {
        drawFenceItem(Offset(50f, 0f))
    }
}

private fun DrawScope.drawMoon() {
    drawCircle(Color(0xFFC2C5CC), center = Offset(140f, 140f), radius = 300f)

    drawCircle(Color(0xFF989DA9), center = Offset(10f, 140f), radius = 60f)
    drawCircle(Color(0xFF989DA9), center = Offset(170f, 80f), radius = 30f)
    drawCircle(Color(0xFF989DA9), center = Offset(220f, 190f), radius = 20f)
    drawCircle(Color(0xFF989DA9), center = Offset(340f, 140f), radius = 45f)
    drawCircle(Color(0xFF989DA9), center = Offset(250f, 320f), radius = 25f)
    drawCircle(Color(0xFF989DA9), center = Offset(90f, 280f), radius = 35f)
}

private fun DrawScope.drawStars() {
    drawStar(Size(30f, 30f), center = Offset(486f, 476f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(60f, 60f), center = Offset(599f, 405f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(45f, 45f), center = Offset(680f, 530f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(30f, 30f), center = Offset(904f, 406f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(45f, 45f), center = Offset(945f, 498f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(30f, 30f), center = Offset(641f, 654f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(50f, 50f), center = Offset(720f, 853f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(35f, 35f), center = Offset(955f, 1020f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(60f, 60f), center = Offset(499f, 934f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(40f, 40f), center = Offset(461f, 1106f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(45f, 45f), center = Offset(261f, 893f), Color.Yellow, outlineStyle = Stroke(2f))
    drawStar(Size(55f, 55f), center = Offset(131f, 960f), Color.Yellow, outlineStyle = Stroke(2f))
}

private fun DrawScope.drawStar(
    starSize: Size,
    center: Offset,
    color: Color,
    outlineStyle: DrawStyle,
) {
    val path = Path()
    path.moveTo(center.x, center.y - starSize.height * 0.5f)

    path.quadraticTo(
        center.x,
        center.y,
        center.x + starSize.width / 2,
        center.y,
    )

    path.quadraticTo(
        center.x,
        center.y,
        center.x,
        center.y + starSize.height / 2,
    )

    path.quadraticTo(
        center.x,
        center.y,
        center.x - starSize.width / 2,
        center.y,
    )

    path.quadraticTo(
        center.x,
        center.y,
        center.x,
        center.y - starSize.height / 2,
    )

    drawPath(path = path, color = color, style = outlineStyle)
}

private fun DrawScope.drawFlag() {
    drawRect(Color(0xFFFFA200), topLeft = Offset(330f, 1550f), size = Size(6f, 250f))

    drawRect(Color.White, topLeft = Offset(330f, 1550f), size = Size(130f, 30f))
    drawRect(Color(0xFF0039A6), topLeft = Offset(330f, 1580f), size = Size(130f, 30f))
    drawRect(Color(0xFFD52B1E), topLeft = Offset(330f, 1610f), size = Size(130f, 30f))
}
