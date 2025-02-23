package ru.iandreyshev.cglab2_android.presentation.stories

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.iandreyshev.cglab2_android.presentation.common.BaseViewModel
import ru.iandreyshev.cglab2_android.presentation.common.IDragListener
import java.io.IOException
import java.util.Date
import kotlin.math.abs

class StoriesViewModel : BaseViewModel<StoriesState, Any>(
    initialState = StoriesState()
) {

    val drawListener = DrawListener()
    val widthListener = WidthListener()
    val resultSize: Size
        get() = _resultSize

    private var _resultSize = Size.Zero
    private var _brushWidthControllerHeight = 0f

    fun onSelectColor(color: Color) {
        updateState {
            copy(brushColor = color)
        }
    }

    fun onSelectPhoto(bitmap: Bitmap) {
        updateState {
            copy(image = bitmap)
        }
    }

    fun onSaveCanvasSize(size: Size) {
        _resultSize = size
    }

    fun onSaveToFile(resolver: ContentResolver, bitmap: Bitmap) {
        viewModelScope.launch {
            writeToFile(resolver, bitmap)
        }
    }

    fun onSaveBrushWidthControllerHeight(height: Float) {
        _brushWidthControllerHeight = height
    }

    @Throws(IOException::class)
    private fun writeToFile(resolver: ContentResolver, bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${Date()}.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return

        resolver.openOutputStream(imageUri).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it ?: return)
        }
    }

    inner class DrawListener : IDragListener {
        override fun onDragStart(pos: Offset) {
            updateState {
                copy(
                    currentPath = PathData(
                        color = stateValue.brushColor,
                        width = MIN_BRUSH_WIDTH + (MAX_BRUSH_WIDTH - MIN_BRUSH_WIDTH) * stateValue.brushWidthPercent,
                        points = listOf(pos)
                    )
                )
            }
        }

        override fun onDrag(pos: Offset) {
            val currentPath = (stateValue.currentPath ?: run {
                return
            })

            updateState {
                copy(
                    currentPath = currentPath.copy(
                        points = currentPath.points + pos
                    )
                )
            }
        }

        override fun onDragEnd() {
            val lastPath = stateValue.currentPath ?: return
            updateState {
                copy(
                    currentPath = null,
                    paths = paths + lastPath
                )
            }
        }
    }

    inner class WidthListener : IDragListener {
        override fun onDragStart(pos: Offset) {
            updateState {
                copy(
                    isBrushControllerActive = true,
                    brushWidthPercent = getBrushWidthPercent(pos),
                )
            }
        }

        override fun onDrag(pos: Offset) {
            updateState {
                copy(
                    isBrushControllerActive = true,
                    brushWidthPercent = getBrushWidthPercent(pos),
                )
            }
        }

        override fun onDragEnd() {
            updateState {
                copy(isBrushControllerActive = false)
            }
        }

        private fun getBrushWidthPercent(pos: Offset) =
            abs(1f - pos.y.coerceIn(0f, _brushWidthControllerHeight) / _brushWidthControllerHeight)
    }

}
