package ru.iandreyshev.cglab2_android.presentation.common

import android.graphics.Bitmap

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
