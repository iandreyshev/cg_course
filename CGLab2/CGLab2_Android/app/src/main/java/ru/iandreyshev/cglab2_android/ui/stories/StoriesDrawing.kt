package ru.iandreyshev.cglab2_android.ui.stories

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
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
import ru.iandreyshev.cglab2_android.presentation.common.aspectFill
import ru.iandreyshev.cglab2_android.presentation.stories.PathData
import ru.iandreyshev.cglab2_android.presentation.stories.StoriesState

fun DrawScope.drawPhoto(image: Bitmap) {
    drawImage(image.aspectFill(size).asImageBitmap())
}

fun DrawScope.drawArt(currentPath: PathData?, paths: List<PathData>) {
    (currentPath?.let { paths + currentPath } ?: paths)
        .forEach { pathData ->
            if (pathData.points.isEmpty()) {
                return@forEach
            }

            val path = Path()
            val firstPoint = pathData.points.first()

            path.moveTo(firstPoint.x, firstPoint.y)

            pathData.points.forEach {
                path.lineTo(it.x, it.y)
            }

            val style = Stroke(width = pathData.width, cap = StrokeCap.Round, join = StrokeJoin.Round)

            drawPath(path, pathData.color, style = style)
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
        val softwareBitmap: Bitmap = state.image
            ?.copy(Bitmap.Config.ARGB_8888, false)
            ?: throw IllegalStateException("Photo is null")
        drawPhoto(softwareBitmap)
        drawArt(state.currentPath, state.paths)
    }

    return bitmap.asAndroidBitmap()
        .copy(Bitmap.Config.ARGB_8888, false)
}
