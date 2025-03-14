package ru.iandreyshev.core

import androidx.compose.ui.geometry.Offset

interface IDragListener {
    fun onDragStart(pos: Offset)
    fun onDrag(pos: Offset)
    fun onDragEnd()
}
