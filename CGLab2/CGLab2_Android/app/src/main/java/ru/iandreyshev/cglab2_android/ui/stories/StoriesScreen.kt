package ru.iandreyshev.cglab2_android.ui.stories

import android.graphics.ImageDecoder
import android.net.Uri
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.iandreyshev.cglab2_android.presentation.stories.StoriesState
import ru.iandreyshev.cglab2_android.presentation.stories.StoriesViewModel
import ru.iandreyshev.cglab2_android.system.ThemeYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    viewModel: StoriesViewModel
) {
    val state by viewModel.state

    val contentResolver = LocalContext.current.contentResolver
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val source = ImageDecoder.createSource(contentResolver, uri)
        imageBitmap = ImageDecoder.decodeBitmap(source).asImageBitmap()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                title = { Text("Story editor") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (imageBitmap != null) {
                return@Scaffold
            }
            ExtendedFloatingActionButton(
                onClick = { launcher.launch("image/*") },
                shape = CircleShape,
                containerColor = ThemeYellow,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Floating action button.") },
                text = { Text(text = "Выбрать изображение") }
            )
        }
    ) { innerPadding ->
        when (val bitmap = imageBitmap) {
            null -> DrawEmptyText()
            else -> DrawEditor(innerPadding, bitmap, state, viewModel)
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

        val text = "Для начала нужно выбрать картинку"
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
    bitmap: ImageBitmap,
    state: StoriesState,
    viewModel: StoriesViewModel
) {
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        StoryCanvas(state, viewModel)
        Column(
            modifier = Modifier.align(alignment = Alignment.BottomCenter)
        ) {
            ColorPicker(state, viewModel)
        }
    }
}

@Composable
private fun StoryCanvas(
    state: StoriesState,
    viewModel: StoriesViewModel
) {
    Box(
        modifier = Modifier
            .height(650.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = viewModel::onDragStart,
                    onDrag = { change, _ ->
                        viewModel.onDrag(change.position)
                    },
                    onDragEnd = viewModel::onDragEng
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .background(Color.Red)
                .height(IntrinsicSize.Max)
                .fillMaxWidth()
        ) {
            println("PATH COUNT: ${state.paths.count()}")
            val allPaths = state.currentPath
                ?.let { state.paths + state.currentPath }
                ?: state.paths

            allPaths.forEach { pathData ->
                if (pathData.points.isEmpty()) {
                    return@forEach
                }

                val path = Path()
                val firstPoint = pathData.points.first()

                path.moveTo(firstPoint.x, firstPoint.y)

                pathData.points.forEach {
                    path.lineTo(it.x, it.y)
                }

                drawPath(path, pathData.color, style = Stroke(width = 10f))
            }
        }
    }
}

@Composable
private fun ColorPicker(
    state: StoriesState,
    viewModel: StoriesViewModel
) {
    Row(modifier = Modifier.padding(16.dp)) {
        val colors = listOf(Color.White, Color.Black, Color.Red, Color.Green, Color.Blue, Color.Magenta)

        colors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .drawWithCache {
                        onDrawBehind {
                            drawCircle(color)
                        }
                    }
                    .fillMaxSize()
                    .clickable { viewModel.onSelectColor(color) }
            ) {
                if (state.brushColor == color) {
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
            }
            if (index != colors.lastIndex) {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}
