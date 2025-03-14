package ru.iandreyshev.cglab3.bezier.ui.openGL

import androidx.compose.ui.geometry.Size

object CoordsToOpenGLMapper {
    fun mapX(screenSize: Size, coord: Float): Float {
        val halfScreen = screenSize.width / 2

        return when {
            coord > halfScreen -> (coord - halfScreen) / halfScreen
            else -> (halfScreen - coord) / -halfScreen
        }
    }

    fun mapY(screenSize: Size, coord: Float): Float {
        val halfScreen = screenSize.height / 2

        return when {
            coord > halfScreen -> (coord - halfScreen) / -halfScreen
            else -> (halfScreen - coord) / halfScreen
        }
    }
}
