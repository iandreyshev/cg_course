package ru.iandreyshev.cglab2_android.ui.craft

import android.content.res.Resources
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import ru.iandreyshev.cglab2_android.R
import ru.iandreyshev.cglab2_android.domain.Element
import ru.iandreyshev.cglab2_android.domain.Element.AIR
import ru.iandreyshev.cglab2_android.domain.Element.FIRE
import ru.iandreyshev.cglab2_android.domain.Element.GROUND
import ru.iandreyshev.cglab2_android.domain.Element.WATER
import ru.iandreyshev.cglab2_android.presentation.common.BIN_BOTTOM_MARGIN_DP
import ru.iandreyshev.cglab2_android.presentation.common.BIN_RADIUS_PX
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_SIDE
import ru.iandreyshev.cglab2_android.presentation.common.ElementDrawableResProvider
import ru.iandreyshev.cglab2_android.presentation.common.SELECT_ELEMENT_NAV_KEY
import ru.iandreyshev.cglab2_android.presentation.common.aspectFit
import ru.iandreyshev.cglab2_android.presentation.craft.CraftElement
import ru.iandreyshev.cglab2_android.presentation.craft.CraftState
import ru.iandreyshev.cglab2_android.presentation.craft.CraftViewModel
import ru.iandreyshev.cglab2_android.presentation.craft.VibrateTouchBin
import ru.iandreyshev.cglab2_android.system.ThemeBlue
import ru.iandreyshev.cglab2_android.system.ThemeYellow

@Composable
fun CraftScreen(
    viewModel: CraftViewModel,
    savedStateHandle: SavedStateHandle,
    imageProvider: ElementDrawableResProvider = ElementDrawableResProvider()
) {
    val state by viewModel.state
    val events = viewModel.events.collectAsState(initial = null)

    val resources = LocalContext.current.resources
    val bitmaps by remember { createBitmapsCache(resources, imageProvider) }

    val selectElementState = savedStateHandle.getLiveData<Element>(SELECT_ELEMENT_NAV_KEY)
    selectElementState.value?.let {
        viewModel.onSpawnElement(it)
        savedStateHandle.remove<Element>(SELECT_ELEMENT_NAV_KEY)
    }

    val haptic = LocalHapticFeedback.current

    val addButtonAlpha by animateFloatAsState(if (state.isDrag) 0f else 1f, label = "alpha")
    val binAlpha by animateFloatAsState(if (state.isDrag) 1f else 0f, label = "alpha")
    val binSizeFactor by animateFloatAsState(if (state.isDragAboveTheBin) 1.2f else 1f, label = "size")

    val systemBars = WindowInsets.systemBars
    val insets = systemBars.getBottom(LocalDensity.current) + systemBars.getTop(LocalDensity.current)
    val binBottomMargin = with(LocalDensity.current) { BIN_BOTTOM_MARGIN_DP.dp.toPx() }
    viewModel.initScreenMetrics(insets, binBottomMargin)

    Canvas(
        modifier = Modifier
            .background(ThemeBlue)
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
        drawElements(state, bitmaps)
        drawBin(state.binCenter, binAlpha, binSizeFactor)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        ExtendedFloatingActionButton(
            onClick = viewModel::onOpenElementsList,
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .offset(x = (-32).dp, y = (-32).dp)
                .graphicsLayer(alpha = addButtonAlpha),
            shape = CircleShape,
            containerColor = ThemeYellow,
            contentColor = Color.White,
            icon = { Icon(Icons.AutoMirrored.Filled.List, "Floating action button.") },
            text = { Text(text = "Список элементов") }
        )
    }

    val event = events.value
//    val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//        val vibratorManager = LocalContext.current.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//        vibratorManager.defaultVibrator
//    } else {
//        LocalContext.current.getSystemService(VIBRATOR_SERVICE) as Vibrator
//    }
    LaunchedEffect(event) {
        when (event) {
            VibrateTouchBin -> {
//                println("Handle vibrate")
//                v.vibrate(VibrationEffect.EFFECT_CLICK)
//                vibrator.defaultVibrator.vibrate(VibrationEffect.EFFECT_CLICK)
//                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            null -> Unit
        }
    }
}

private fun DrawScope.drawElements(state: CraftState, bitmaps: Map<Element, ImageBitmap>) {
    state.elements.forEach {
        drawImage(bitmaps[it.element] ?: return@forEach, it.pos)
    }
}

private fun DrawScope.drawBin(center: Offset, alpha: Float, sizeFactor: Float) {
    drawCircle(Color.Red, BIN_RADIUS_PX * sizeFactor, center, alpha = alpha)
}

private val CraftElement.color
    get() = when (element) {
        WATER -> Color.Blue
        FIRE -> Color.Red
        GROUND -> Color.Green
        AIR -> Color.LightGray
        else -> Color.Magenta
    }

private fun createBitmapsCache(
    resources: Resources,
    imageProvider: ElementDrawableResProvider
) = mutableStateOf(Element.entries.associate {
    it to ImageBitmap.imageResource(resources, id = imageProvider[it])
        .asAndroidBitmap()
        .aspectFit(ELEMENT_SIDE.toInt())
        .asImageBitmap()
})