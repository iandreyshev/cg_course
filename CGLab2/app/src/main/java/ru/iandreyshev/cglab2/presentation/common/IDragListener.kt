package ru.iandreyshev.cglab2.presentation.common

import androidx.compose.ui.geometry.Offset

interface IDragListener {
    fun onDragStart(pos: Offset)
    fun onDrag(pos: Offset)
    fun onDragEnd()
}
