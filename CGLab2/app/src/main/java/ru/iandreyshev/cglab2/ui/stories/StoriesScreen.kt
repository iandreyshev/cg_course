package ru.iandreyshev.cglab2.ui.stories

import android.graphics.ImageDecoder
import android.net.Uri
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.iandreyshev.cglab2.presentation.common.IDragListener
import ru.iandreyshev.cglab2.presentation.common.InkEraser
import ru.iandreyshev.cglab2.presentation.stories.BRUSH_WIDTH_CONTROLLER_ACTIVE_OFFSET_DP
import ru.iandreyshev.cglab2.presentation.stories.BRUSH_WIDTH_CONTROLLER_HEIGHT_DP
import ru.iandreyshev.cglab2.presentation.stories.MAX_BRUSH_WIDTH
import ru.iandreyshev.cglab2.presentation.stories.MIN_BRUSH_WIDTH
import ru.iandreyshev.cglab2.presentation.stories.STORIES_COLOR_PICKER_COLORS
import ru.iandreyshev.cglab2.presentation.stories.StoriesState
import ru.iandreyshev.cglab2.presentation.stories.StoriesViewModel
import ru.iandreyshev.cglab2.system.ThemeYellow

private val BACKGROUND_COLOR = Color(0xFF101820)
private val BRUSH_WIDTH_CONTROLLER_COLOR = Color(0xA0FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    viewModel: StoriesViewModel
) {
    val state by viewModel.state

    val brushWidthControllerActivationPercent by animateFloatAsState(
        if (state.isBrushControllerActive) 1f else 0f, label = "brush_controller_percent"
    )

    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val source = ImageDecoder.createSource(contentResolver, uri)
        viewModel.onSelectPhoto(ImageDecoder.decodeBitmap(source))
    }

    val openPhotoPicker = { launcher.launch("image/*") }

    val brushWidthControllerHeight = with(LocalDensity.current) { BRUSH_WIDTH_CONTROLLER_HEIGHT_DP.dp.toPx() }
    viewModel.onSaveBrushWidthControllerHeight(brushWidthControllerHeight)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BACKGROUND_COLOR,
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = BACKGROUND_COLOR,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                title = { Text("Story editor") },
                actions = {
                    state.photo ?: return@TopAppBar
                    IconButton(onClick = openPhotoPicker) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
                    }
                    IconButton(onClick = { viewModel.onSaveToFile(contentResolver) }) {
                        Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.photo != null) {
                return@Scaffold
            }
            ExtendedFloatingActionButton(
                onClick = openPhotoPicker,
                shape = CircleShape,
                containerColor = ThemeYellow,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Floating action button.") },
                text = { Text(text = "Выбрать изображение") }
            )
        }
    ) { innerPadding ->
        when (state.photo) {
            null -> DrawEmptyText()
            else -> DrawEditor(
                innerPadding = innerPadding,
                state = state,
                brushWidthControllerOffset = brushWidthControllerActivationPercent,
                drawListener = viewModel.drawListener,
                brushWidthControllerListener = viewModel.widthListener,
                onSelectColor = viewModel::onSelectColor,
                onSelectEraser = viewModel::onSelectEraser,
                onSaveSize = viewModel::onSaveCanvasSize,
            )
        }
    }
}

@Composable
private fun DrawEmptyText() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val text = "Выберите изображение"
        val paint = TextPaint().apply {
            color = android.graphics.Color.WHITE
            textSize = 16.sp.toPx()
        }

        val textWidth = paint.measureText(text)
        val textHeight = paint.fontMetrics.run { descent - ascent }

        drawContext.canvas.nativeCanvas
            .drawText(text, (canvasWidth - textWidth) / 2, (canvasHeight + textHeight) / 2, paint)
    }
}

@Composable
private fun DrawEditor(
    innerPadding: PaddingValues,
    state: StoriesState,
    brushWidthControllerOffset: Float,
    drawListener: IDragListener,
    brushWidthControllerListener: IDragListener,
    onSelectColor: (Color) -> Unit,
    onSelectEraser: () -> Unit,
    onSaveSize: (Size) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(innerPadding)) {
            EditorArea(state, drawListener, onSaveSize)
            Spacer(modifier = Modifier.height(12.dp))
            ColorPicker(state.brushColor, state.isEraserMode, onSelectColor, onSelectEraser)
            Spacer(modifier = Modifier.height(16.dp))
        }
        BrushWidthController(
            state.brushWidthPercent,
            state.isBrushControllerActive,
            brushWidthControllerOffset,
            brushWidthControllerListener
        )
    }
}

@Composable
private fun BoxScope.BrushWidthController(
    widthPercent: Float,
    isActive: Boolean,
    activationPercent: Float,
    listener: IDragListener
) {
    Canvas(
        modifier = Modifier
            .width(72.dp)
            .height(BRUSH_WIDTH_CONTROLLER_HEIGHT_DP.dp)
            .align(Alignment.CenterStart)
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = listener::onDragStart,
                    onDrag = { change, _ ->
                        listener.onDrag(change.position)
                    },
                    onDragEnd = listener::onDragEnd
                )
            }
    ) {
        val offsetX = BRUSH_WIDTH_CONTROLLER_ACTIVE_OFFSET_DP.coerceAtLeast(2f).dp.toPx() * activationPercent
        val offset = Offset(offsetX, 0f)
        val controllerHeight = BRUSH_WIDTH_CONTROLLER_HEIGHT_DP.dp.toPx()
        val lineWidth = 4.dp.toPx().coerceAtLeast(MIN_BRUSH_WIDTH)
        val size = Size(lineWidth, controllerHeight)
        drawRoundRect(BRUSH_WIDTH_CONTROLLER_COLOR, offset, size, cornerRadius = CornerRadius(24f))

        val center = Offset(offsetX + lineWidth / 2f, controllerHeight - controllerHeight * widthPercent)
        val activeWidth = MIN_BRUSH_WIDTH + (MAX_BRUSH_WIDTH - MIN_BRUSH_WIDTH) * widthPercent
        val diameter = if (isActive) activeWidth else MAX_BRUSH_WIDTH
        drawCircle(color = Color.White, center = center, radius = diameter / 2f)
    }
}

@Composable
private fun ColumnScope.EditorArea(
    state: StoriesState,
    drawListener: IDragListener,
    onSaveCanvasSize: (Size) -> Unit
) {
    val image = state.photo ?: return
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            onDraw = {
                println("DRAW PHOTO")
                drawPhoto(image)
                onSaveCanvasSize(size)
            }
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(true) {
                    detectDragGestures(
                        onDragStart = drawListener::onDragStart,
                        onDrag = { change, _ ->
                            drawListener.onDrag(change.position)
                        },
                        onDragEnd = drawListener::onDragEnd
                    )
                },
            onDraw = {
                println("DRAW ART")
                drawPhoto(state.art ?: return@Canvas)
//                drawArt(state.currentPath, state.paths)
            }
        )
    }
}

@Composable
private fun ColumnScope.ColorPicker(
    brushColor: Color,
    isEraserMode: Boolean,
    onSelectColor: (Color) -> Unit,
    onSelectEraser: () -> Unit
) {
    Row(
        modifier = Modifier
            .align(alignment = Alignment.CenterHorizontally)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
        ) {
            STORIES_COLOR_PICKER_COLORS.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .drawWithCache {
                            onDrawBehind {
                                drawCircle(color)
                            }
                        }
                        .fillMaxSize()
                        .clickable { onSelectColor(color) }
                ) {
                    ColorTarget(color, brushColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isEraserMode) Color.White else Color.Transparent, RoundedCornerShape(16.dp))
                .clickable { onSelectEraser() }
        ) {
            Icon(
                modifier = Modifier
                    .size(if (isEraserMode) 28.dp else 24.dp)
                    .align(Alignment.Center),
                imageVector = InkEraser,
                contentDescription = null,
                tint = if (isEraserMode) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun ColorTarget(color: Color, brushColor: Color) {
    if (color != brushColor) {
        return
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .drawWithCache {
                onDrawBehind {
                    val strokeWidth = 3.dp.toPx()
                    val padding = 2.dp.toPx()
                    drawArc(
                        color = if (color == Color.Black) Color.White else Color.Black,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(padding.dp.toPx(), padding.dp.toPx()),
                        size = Size(
                            32.dp.toPx() - 2 * strokeWidth - 2 * padding,
                            32.dp.toPx() - 2 * strokeWidth - 2 * padding
                        ),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            .fillMaxSize()
    )
}
