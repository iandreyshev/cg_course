package ru.iandreyshev.cglab2_android.ui.craft

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
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
import ru.iandreyshev.cglab2_android.domain.Element
import ru.iandreyshev.cglab2_android.domain.Element.AIR
import ru.iandreyshev.cglab2_android.domain.Element.FIRE
import ru.iandreyshev.cglab2_android.domain.Element.GROUND
import ru.iandreyshev.cglab2_android.domain.Element.WATER
import ru.iandreyshev.cglab2_android.presentation.common.BIN_BOTTOM_MARGIN_DP
import ru.iandreyshev.cglab2_android.presentation.common.BIN_RADIUS_PX
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_SIZE
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_WIDTH
import ru.iandreyshev.cglab2_android.presentation.common.ElementDrawableResProvider
import ru.iandreyshev.cglab2_android.presentation.common.SELECT_ELEMENT_NAV_KEY
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
    elementDrawableResProvider: ElementDrawableResProvider = ElementDrawableResProvider()
) {
    val state by viewModel.state
    val events = viewModel.events.collectAsState(initial = null)

    val selectElementState = savedStateHandle.getLiveData<Element>(SELECT_ELEMENT_NAV_KEY)
    selectElementState.value?.let {
        viewModel.onSpawnElement(it)
        savedStateHandle.remove<Element>(SELECT_ELEMENT_NAV_KEY)
    }

    val haptic = LocalHapticFeedback.current

    val addButtonAlpha by animateFloatAsState(if (state.isDrag) 0f else 1f, label = "alpha")
    val binAlpha by animateFloatAsState(if (state.isDrag) 1f else 0f, label = "alpha")
    val binSizeFactor by animateFloatAsState(if (state.isDragAboveTheBin) 1.2f else 1f, label = "size")

    val bottomInset = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    val binBottomMargin = with(LocalDensity.current) { BIN_BOTTOM_MARGIN_DP.dp.toPx() }
    viewModel.initScreenMetrics(bottomInset, binBottomMargin)

    val resources = LocalContext.current.resources

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
        drawElements(state) { elementDrawableResProvider[it]?.let { ImageBitmap.imageResource(resources, it) } }
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

private fun DrawScope.drawElements(state: CraftState, b: (Element) -> ImageBitmap?) {
    state.elements.forEach {
        when (val bitmap = b(it.element)) {
            null -> drawRect(it.color, it.topLeft, ELEMENT_SIZE)
            else -> {
                val sBm = bitmap.asAndroidBitmap().resize(ELEMENT_WIDTH.toInt())
                drawImage(sBm.asImageBitmap(), topLeft = it.topLeft)
            }
        }
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

private val CraftElement.topLeft: Offset
    get() = Offset(x, y)

private fun Bitmap.resize(maxSize: Int): Bitmap {
    var width = width
    var height = height

    val bitmapRatio = width.toFloat() / height.toFloat()
    if (bitmapRatio > 1) {
        width = maxSize
        height = (width / bitmapRatio).toInt()
    } else {
        height = maxSize
        width = (height * bitmapRatio).toInt()
    }
    return Bitmap.createScaledBitmap(this, width, height, true)
}