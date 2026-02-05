package ru.iandreyshev.cglab3.asteroids.presentation.gameState

import androidx.compose.ui.geometry.Offset

interface GameState {
    fun onDragStart(stickFieldCenter: Offset, stickFieldRadius: Float) {}
    fun onDrag(position: Offset) {}
    fun onDragEnd() {}
    fun onFireClick() {}
    fun doFrameTick(elapsedTime: Float) {}
}
