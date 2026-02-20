package ru.iandreyshev.cglab4.mobius

import android.opengl.Matrix

data class MobiusState(
    val rotationMatrix: FloatArray = FloatArray(16)
        .apply { Matrix.setIdentityM(this, 0) },
    val scale: Float = 0f
)
