package ru.iandreyshev.cglab4.figure.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.core.BaseViewModel

class FigureViewModel : BaseViewModel<FigureState, Any>(
    initialState = FigureState()
) {

    init {
        viewModelScope.launch {
            repeat(200) {
                delay(100)
                updateState {
                    copy(rotation = rotation + Offset(1f, 1f))
                }
            }
        }
    }

    fun onDrag(dragAmount: Offset) {
        updateState {
            copy(rotation = rotation + Offset(dragAmount.x, dragAmount.y))
        }
    }

}
