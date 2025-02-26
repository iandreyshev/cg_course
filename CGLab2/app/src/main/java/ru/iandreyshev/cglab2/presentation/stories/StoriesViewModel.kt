package ru.iandreyshev.cglab2.presentation.stories

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.iandreyshev.cglab2.presentation.common.BaseViewModel
import ru.iandreyshev.cglab2.presentation.common.IDragListener
import ru.iandreyshev.cglab2.ui.stories.drawPath
import ru.iandreyshev.cglab2.ui.stories.drawToBitmap
import java.util.Date
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class StoriesViewModel(
    private val saveToFileContext: CoroutineContext = Dispatchers.IO
) : BaseViewModel<StoriesState, Any>(
    initialState = StoriesState()
) {

    val drawListener = DrawListener()
    val widthListener = WidthListener()
    val resultSize: Size
        get() = _resultSize

    private var _canvas: Canvas? = null
    private var _artBitmap: Bitmap? = null
    private var _currentPath: PathData? = null
    private var _resultSize = Size.Zero
    private var _brushWidthControllerHeight = 0f
    private var _drawScope = CanvasDrawScope()

    fun onSelectColor(color: Color) {
        updateState {
            copy(brushColor = color, isEraserMode = false)
        }
    }

    fun onSelectEraser() {
        updateState {
            copy(brushColor = Color.Transparent, isEraserMode = true)
        }
    }

    fun onSelectPhoto(bitmap: Bitmap) {
        updateState {
            copy(photo = bitmap)
        }
    }

    fun onSaveCanvasSize(size: Size) {
        if (_artBitmap == null) {
            _artBitmap = Bitmap.createBitmap(
                size.width.toInt(),
                size.height.toInt(),
                Bitmap.Config.ARGB_8888
            )

            updateState {
                copy(art = _artBitmap)
            }

            _canvas = Canvas(_artBitmap?.asImageBitmap() ?: return)
        }
        _resultSize = size
    }

    fun onSaveToFile(resolver: ContentResolver) {
        viewModelScope.launch {
            withContext(saveToFileContext) {
                val bitmap = drawToBitmap(stateValue, _resultSize)
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "${Date()}.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext

                resolver.openOutputStream(imageUri).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it ?: return@withContext)
                }
            }
        }
    }

    fun onSaveBrushWidthControllerHeight(height: Float) {
        _brushWidthControllerHeight = height
    }

    private fun drawArtToBitmap() {
        _drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = _canvas ?: return,
            size = _resultSize,
        ) {
            drawPath(_currentPath ?: return)
        }
        updateState { copy(recompositionToggle = !recompositionToggle) }
    }

    inner class DrawListener : IDragListener {
        override fun onDragStart(pos: Offset) {
            _currentPath = PathData(
                mode = if (stateValue.isEraserMode) PathMode.Eraser else PathMode.Color(stateValue.brushColor),
                width = MIN_BRUSH_WIDTH + (MAX_BRUSH_WIDTH - MIN_BRUSH_WIDTH) * stateValue.brushWidthPercent,
                points = listOf(pos)
            )

            drawArtToBitmap()
        }

        override fun onDrag(pos: Offset) {
            _currentPath = _currentPath?.copy(
                points = _currentPath?.points.orEmpty() + pos
            )

            drawArtToBitmap()
        }

        override fun onDragEnd() {
            _currentPath = null
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
