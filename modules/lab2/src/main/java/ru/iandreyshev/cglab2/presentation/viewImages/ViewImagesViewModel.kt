package ru.iandreyshev.cglab2.presentation.viewImages

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import ru.iandreyshev.core.BaseViewModel

class ViewImagesViewModel : BaseViewModel<ViewImagesState, Any>(
    initialState = ViewImagesState()
) {

    private var _dragOffset = Offset.Zero
    private var _canvasSize = Size.Zero
    private var _imageSize = Size.Zero

    fun initCanvasSize(size: Size) {
        _canvasSize = size
    }

    fun onSelectImage(bitmap: ImageBitmap, size: Size) {
        _imageSize = size

        updateState {
            copy(imageBitmap = bitmap)
        }
    }

    fun onChangeOrientation() {
        updateState {
            copy(position = Offset.Zero)
        }
    }

    fun onDragStart(pos: Offset) {
        _dragOffset = stateValue.position - pos
    }

    fun onDrag(pos: Offset) {
        updateState {
            copy(
                position = Offset(
                    x = (pos.x + _dragOffset.x).coerceIn(0f, _canvasSize.width - _imageSize.width),
                    y = (pos.y + _dragOffset.y).coerceIn(0f, _canvasSize.height - _imageSize.height)
                )
            )
        }
    }

    fun onDragEng() {
        _dragOffset = Offset.Zero
    }

}
