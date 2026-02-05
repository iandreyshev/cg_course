package ru.iandreyshev.cglab1.house

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val HOUSE_1_START_X = 375f
private const val HOUSE_1_START_Y = 1685f

private const val HOUSE_2_START_X = 975f
private const val HOUSE_2_START_Y = 1685f

class HouseViewModel : ViewModel() {

    private val _state = MutableStateFlow(HouseState())
    val state = _state.asStateFlow()

    init {
        initState()
    }

    fun onDragStart(position: Offset) {
        val rect1 = Rect(_state.value.pos1, Size(500f, 500f))
        val rect2 = Rect(_state.value.pos2, Size(500f, 500f))

        val dragState = when {
            rect1.contains(position) -> DragState.DRAG_FIRST
            rect2.contains(position) -> DragState.DRAG_SECOND
            else -> DragState.NO_DRAG
        }

        _state.update {
            it.copy(dragState = dragState)
        }
    }

    fun onDragChange(newPosition: Offset) {
        _state.update {
            when (_state.value.dragState) {
                DragState.DRAG_FIRST ->
                    it.copy(pos1X = newPosition.x, pos1Y = newPosition.y)
                DragState.DRAG_SECOND ->
                    it.copy(pos2X = newPosition.x, pos2Y = newPosition.y)
                else -> it
            }
        }
    }

    fun onDragStop() {
        _state.update {
            it.copy(dragState = DragState.NO_DRAG)
        }
    }

    private fun initState() {
        _state.update {
            it.copy(
                pos1X = HOUSE_1_START_X,
                pos1Y = HOUSE_1_START_Y,
                pos2X = HOUSE_2_START_X,
                pos2Y = HOUSE_2_START_Y
            )
        }
    }

}
