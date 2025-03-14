package ru.iandreyshev.cglab2.ui.stories

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import ru.iandreyshev.cglab2.presentation.common.aspectFill
import ru.iandreyshev.cglab2.presentation.stories.PathData
import ru.iandreyshev.cglab2.presentation.stories.PathMode
import ru.iandreyshev.cglab2.presentation.stories.StoriesState

fun DrawScope.drawPhoto(image: Bitmap) {
    drawImage(image.aspectFill(size).asImageBitmap())
}

fun DrawScope.drawArt(currentPath: PathData?, paths: List<PathData>) {
    (currentPath?.let { paths + currentPath } ?: paths)
        .forEach { pathData ->
            if (pathData.points.isEmpty()) {
                return@forEach
            }

            drawPath(pathData)
        }
}

fun DrawScope.drawPath(pathData: PathData) {
    val path = Path()
    val firstPoint = pathData.points.first()

    path.moveTo(firstPoint.x, firstPoint.y)

    pathData.points.forEach {
        path.lineTo(it.x, it.y)
    }

    val style = Stroke(width = pathData.width, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (val mode = pathData.mode) {
        is PathMode.Color ->
            drawPath(path, mode.color, style = style)
        PathMode.Eraser ->
            drawPath(path, Color.White, style = style, blendMode = BlendMode.Clear)
    }
}

fun drawToBitmap(state: StoriesState, size: Size): Bitmap {
    val drawScope = CanvasDrawScope()
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        val softwareBitmap: Bitmap = state.photo
            ?.copy(Bitmap.Config.ARGB_8888, false)
            ?: throw IllegalStateException("Photo is null")
        drawPhoto(softwareBitmap)
        drawPhoto(state.art ?: return@draw)
//        drawArt(state.currentPath, state.paths)
    }

    return bitmap.asAndroidBitmap()
        .copy(Bitmap.Config.ARGB_8888, false)
}
