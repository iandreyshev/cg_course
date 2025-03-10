package ru.iandreyshev.cglab3.asteroids.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsState
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsViewModel
import ru.iandreyshev.cglab3.asteroids.ui.openGL.AsteroidsGLSurfaceView

private const val STICK_FIELD_DRAW_RADIUS_DP = 72
private const val STICK_FIELD_CONTROL_RADIUS_DP = 56
private const val STICK_RADIUS_DP = 32
private const val STICK_STROKE_DP = 3.5

@Composable
fun AsteroidsScreen(
    viewModel: AsteroidsViewModel = viewModel()
) {
    val state by viewModel.state

    Column(
        modifier = Modifier
            .background(AsteroidsColors.black)
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        GameScene(state, viewModel::onUpdateWorldSize)
        Spacer(modifier = Modifier.height(20.dp))
        GameController(
            stickCenter = state.stickCenter,
            onDragStart = viewModel::onDragStart,
            onDrag = viewModel::onDrag,
            onDragEnd = viewModel::onDragEnd,
            onFire = viewModel::onFire
        )
    }
}

@Composable
private fun ColumnScope.GameScene(
    state: AsteroidsState,
    onUpdateWorldSize: (IntSize) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .clip(shape)
            .border(BorderStroke(2.dp, AsteroidsColors.darkWhite), shape)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged(onUpdateWorldSize),
            factory = {
                AsteroidsGLSurfaceView(it)
            },
            update = { view ->
                view.update(state)
            }
        )
    }
}

@Composable
private fun ColumnScope.GameController(
    stickCenter: Offset?,
    onDragStart: (fieldCenter: Offset, fieldRadius: Float) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onFire: () -> Unit
) {
    Row(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val stickFieldDrawRadiusDp = STICK_FIELD_DRAW_RADIUS_DP.dp
        val stickFieldDrawRadiusPx = with(LocalDensity.current) { stickFieldDrawRadiusDp.toPx() }

        val stickFieldCenter = Offset(stickFieldDrawRadiusPx, stickFieldDrawRadiusPx)
        val stickFieldRadius = with(LocalDensity.current) { STICK_FIELD_CONTROL_RADIUS_DP.dp.toPx() }

        Canvas(
            modifier = Modifier
                .size(stickFieldDrawRadiusDp * 2)
                .pointerInput(true) {
                    detectDragGestures(
                        onDragStart = {
                            onDragStart(stickFieldCenter, stickFieldRadius)
                        },
                        onDrag = { change, _ ->
                            onDrag(change.position)
                        },
                        onDragEnd = onDragEnd
                    )
                }
        ) {
            drawCircle(AsteroidsColors.yellow, stickFieldDrawRadiusPx)

            val stickDrawCenter = stickCenter ?: stickFieldCenter
            drawCircle(AsteroidsColors.black, STICK_RADIUS_DP.dp.toPx(), stickDrawCenter)
            drawCircle(AsteroidsColors.white, STICK_RADIUS_DP.dp.toPx() - STICK_STROKE_DP.dp.toPx(), stickDrawCenter)
        }
        Button(
            onClick = onFire,
            modifier = Modifier
                .size((2 * STICK_FIELD_DRAW_RADIUS_DP).dp),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = AsteroidsColors.red, contentColor = AsteroidsColors.white)
        ) {
            Text("FIRE", fontSize = 20.sp)
        }
    }
}
