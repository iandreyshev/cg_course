package ru.iandreyshev.cglab2_android.presentation.stories

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class StoriesState(
    val brushWidth: Float = 0f,
    val brushColor: Color = Color.White,
    val currentPath: PathData? = null,
    val paths: List<PathData> = emptyList()
)

data class PathData(
    val color: Color,
    val width: Float,
    val points: List<Offset>
)
