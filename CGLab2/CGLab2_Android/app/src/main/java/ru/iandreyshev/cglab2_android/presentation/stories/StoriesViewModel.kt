package ru.iandreyshev.cglab2_android.presentation.stories

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import ru.iandreyshev.cglab2_android.presentation.common.BaseViewModel

class StoriesViewModel : BaseViewModel<StoriesState, Any>(
    initialState = StoriesState()
) {

    fun onDragStart(pos: Offset) {
        updateState {
            copy(
                currentPath = PathData(
                    color = stateValue.brushColor,
                    width = stateValue.brushWidth,
                    points = listOf(pos)
                )
            )
        }
    }

    fun onDrag(pos: Offset) {
        println("DRAG_1")
        val currentPath = (stateValue.currentPath ?: run {
            println("RETURN")
            return
        })
        println("DRAG_2")

        updateState {
            copy(
                currentPath = currentPath.copy(
                    points = currentPath.points + pos
                )
            )
        }
    }

    fun onDragEng() {
        val lastPath = stateValue.currentPath ?: return
        updateState {
            copy(
                currentPath = null,
                paths = paths + lastPath
            )
        }
    }

    fun onSelectColor(color: Color) {
        println("Change color to: $color")
        updateState {
            copy(brushColor = color)
        }
    }

}
