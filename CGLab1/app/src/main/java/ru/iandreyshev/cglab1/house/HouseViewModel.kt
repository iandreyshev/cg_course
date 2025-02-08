package ru.iandreyshev.cglab1.house

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val HOUSE_START_X = 625f
private const val HOUSE_START_Y = 1785f

class HouseViewModel : ViewModel() {

    private val _state = MutableStateFlow(HouseState())
    val state = _state.asStateFlow()

    private var offset = Offset.Zero

    init {
        initState()
    }

    fun onDragStart(position: Offset) {
        offset = Offset(state.value.posX, state.value.posY) - position
        _state.update {
            it.copy(isDrag = true)
        }
    }

    fun onDragChange(newPosition: Offset) {
        _state.update {
            it.copy(posX = newPosition.x + offset.x, posY = newPosition.y + offset.y, isDrag = true)
        }
    }

    fun onDragStop() {
        _state.update {
            it.copy(isDrag = false)
        }
    }

    private fun initState() {
        _state.update {
            it.copy(posX = HOUSE_START_X, posY = HOUSE_START_Y)
        }
    }

}
