package ru.iandreyshev.cglab2_android.presentation.stories

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class StoriesState(
    val currentPath: PathData? = null,
    val paths: List<PathData> = emptyList(),
    val image: Bitmap? = null,
    val brushColor: Color = Color.White,
    val brushWidthPercent: Float = 0f,
    val isBrushControllerActive: Boolean = false
)

data class PathData(
    val color: Color,
    val width: Float,
    val points: List<Offset>
)
