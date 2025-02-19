package ru.iandreyshev.cglab2_android.ui.craft

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ru.iandreyshev.cglab2_android.domain.Element.AIR
import ru.iandreyshev.cglab2_android.domain.Element.FIRE
import ru.iandreyshev.cglab2_android.domain.Element.GROUND
import ru.iandreyshev.cglab2_android.domain.Element.WATER
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_SIZE
import ru.iandreyshev.cglab2_android.presentation.craft.CraftElement
import ru.iandreyshev.cglab2_android.presentation.craft.CraftState
import ru.iandreyshev.cglab2_android.presentation.craft.CraftViewModel

@Composable
fun CraftScreen(
    viewModel: CraftViewModel
) {

    val state by viewModel.state
    val alpha by animateFloatAsState(if (state.dragElement == null) 1f else 0f, label = "alpha")

    Canvas(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
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
        drawElements(state)
        drawTrash(alpha)
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        FloatingActionButton(
            viewModel::onOpenElementsList,
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .offset(x = (-32).dp, y = (-32).dp)
                .graphicsLayer(alpha = alpha),
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }
}

private fun DrawScope.drawElements(state: CraftState) {
    state.elements.forEach {
        drawRect(it.color, it.topLeft, ELEMENT_SIZE)
    }
}

private fun DrawScope.drawTrash(alpha: Float) {
    drawCircle(Color.Red, radius = 100f, center = Offset(200f, 1000f), alpha = alpha)
}

private val CraftElement.color
    get() = when (element) {
        WATER -> Color.Blue
        FIRE -> Color.Red
        GROUND -> Color.Green
        AIR -> Color.LightGray
        else -> Color.Magenta
    }

private val CraftElement.topLeft: Offset
    get() = Offset(x, y)
