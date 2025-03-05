package ru.iandreyshev.cglab3.bezier.ui.openGL

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.iandreyshev.cglab3.bezier.presentation.BezierState
import ru.iandreyshev.cglab3.bezier.presentation.BezierViewModel

@Composable
fun BezierOpenGLScreen(viewModel: BezierViewModel) {
    val state by viewModel.state

    val pointTouchRadius = with(LocalDensity.current) { 32.dp.toPx() }
    viewModel.initPointTouchRadius(pointTouchRadius)

    Box {
        BezierView(state, viewModel)
        DetalizationController(state.detalization, viewModel::onChangeDetalization)
    }
}

@Composable
fun BezierView(state: BezierState, viewModel: BezierViewModel) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = viewModel::onDragStart,
                    onDrag = { change, _ ->
                        viewModel.onDrag(change.position)
                    },
                    onDragEnd = viewModel::onDragEnd
                )
            },
        factory = {
            BezierGLSurfaceView(it)
        },
        update = { view ->
            view.updateState(state)
        }
    )
}

@Composable
fun BoxScope.DetalizationController(
    detalization: Float,
    onChangeDetalization: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .align(alignment = Alignment.BottomCenter)
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        val detalizationLabel = when (detalization.toInt()) {
            BezierState.MAX_DETALIZATION -> "Максимальное сглаживание"
            else -> "Количество отрезков: ${detalization.toInt()}"
        }
        Text(text = "$detalizationLabel", color = Color.White)
        Slider(
            value = detalization,
            onValueChange = onChangeDetalization,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = BezierState.MAX_DETALIZATION - 2,
            valueRange = 1f..BezierState.MAX_DETALIZATION.toFloat()
        )
    }
}
