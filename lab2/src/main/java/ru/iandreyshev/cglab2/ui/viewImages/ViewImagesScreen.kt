package ru.iandreyshev.cglab2.ui.viewImages

import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.net.Uri
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.onEach
import ru.iandreyshev.cglab2.presentation.common.aspectFit
import ru.iandreyshev.cglab2.presentation.viewImages.ViewImagesViewModel
import ru.iandreyshev.core.ThemeYellow

private const val MAX_IMAGE_SIZE = 720

@Composable
fun ViewImagesScreen(
    viewModel: ViewImagesViewModel
) {
    val state by viewModel.state

    val contentResolver = LocalContext.current.contentResolver
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val source = ImageDecoder.createSource(contentResolver, uri)

        ImageDecoder.decodeBitmap(source)
            .aspectFit(MAX_IMAGE_SIZE)
            .asImageBitmap()
            .also {
                val size = Size(it.width.toFloat(), it.height.toFloat())
                viewModel.onSelectImage(it, size)
            }
    }

    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .onEach { viewModel.onChangeOrientation() }
            .collect { orientation = it }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
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
        viewModel.initCanvasSize(size)

        when (val bitmap = state.imageBitmap) {
            null -> drawEmptyText()
            else -> drawImage(bitmap, state.position)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        ExtendedFloatingActionButton(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .offset(x = (-32).dp, y = (-32).dp),
            shape = CircleShape,
            containerColor = ThemeYellow,
            contentColor = Color.White,
            icon = { Icon(Icons.Default.Add, "Floating action button.") },
            text = { Text(text = "Выбрать изображение") }
        )
    }

}

private fun DrawScope.drawEmptyText() {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val text = "Для начала нужно выбрать картинку"
    val paint = TextPaint().apply {
        color = android.graphics.Color.BLACK
        textSize = 16.sp.toPx()
    }

    val textWidth = paint.measureText(text)
    val textHeight = paint.fontMetrics.run { descent - ascent }

    drawContext.canvas.nativeCanvas
        .drawText(text, (canvasWidth - textWidth) / 2, (canvasHeight + textHeight) / 2, paint)
}
