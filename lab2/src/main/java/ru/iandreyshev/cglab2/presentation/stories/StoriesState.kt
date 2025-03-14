package ru.iandreyshev.cglab2.presentation.stories

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class StoriesState(
    val art: Bitmap? = null,
    val photo: Bitmap? = null,
    val brushColor: Color = Color.White,
    val brushWidthPercent: Float = 0f,
    val isBrushControllerActive: Boolean = false,
    val isEraserMode: Boolean = false,
    val recompositionToggle: Boolean = false
)

data class PathData(
    val mode: PathMode,
    val width: Float,
    val points: List<Offset>
)

sealed interface PathMode {
    data class Color(val color: androidx.compose.ui.graphics.Color) : PathMode
    data object Eraser : PathMode
}
