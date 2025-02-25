package ru.iandreyshev.cglab2_android.presentation.common

import androidx.compose.ui.geometry.Offset

interface IDragListener {
    fun onDragStart(pos: Offset)
    fun onDrag(pos: Offset)
    fun onDragEnd()
}
