package ru.iandreyshev.cglab2_android.presentation.common

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size

fun Bitmap.aspectFit(maxSize: Int): Bitmap {
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

fun Bitmap.aspectFill(size: Size): Bitmap {
    val aspectRatioBitmap = width.toFloat() / height.toFloat()
    val aspectRatioCanvas = size.width / size.height

    val newWidth: Int
    val newHeight: Int

    if (aspectRatioBitmap > aspectRatioCanvas) {
        // Изображение шире, чем Canvas, масштабируем по высоте
        newWidth = (size.height * aspectRatioBitmap).toInt()
        newHeight = size.height.toInt()
    } else {
        // Изображение выше, чем Canvas, масштабируем по ширине
        newWidth = size.width.toInt()
        newHeight = (size.width / aspectRatioBitmap).toInt()
    }

    // Масштабируем изображение до новых размеров
    val scaledBitmap = Bitmap.createScaledBitmap(this, newWidth, newHeight, true)

    // Обрезаем изображение до размеров Canvas
    val xOffset = (scaledBitmap.width - size.width) / 2
    val yOffset = (scaledBitmap.height - size.height) / 2

    return Bitmap.createBitmap(scaledBitmap, xOffset.toInt(), yOffset.toInt(), size.width.toInt(), size.height.toInt())
}