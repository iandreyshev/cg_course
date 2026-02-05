package ru.iandreyshev.cglab1.house

import androidx.compose.ui.geometry.Offset

data class HouseState(
    val pos1X: Float = 0f,
    val pos1Y: Float = 0f,

    val pos2X: Float = 0f,
    val pos2Y: Float = 0f,

    val dragState: DragState = DragState.NO_DRAG
) {

    val pos1 = Offset(pos1X, pos1Y)
    val pos2 = Offset(pos2X, pos2Y)

}

enum class DragState {
    DRAG_FIRST,
    DRAG_SECOND,
    NO_DRAG;
}
