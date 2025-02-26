package ru.iandreyshev.cglab2.presentation.viewImages

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

data class ViewImagesState(
    val position: Offset = Offset.Zero,
    val imageBitmap: ImageBitmap? = null
)
