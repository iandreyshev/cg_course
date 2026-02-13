package ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation

import android.opengl.Matrix
import androidx.compose.ui.geometry.Offset

data class PentagonalIcositetrahedronState(
    val rotationMatrix: FloatArray = FloatArray(16).apply {
        Matrix.setIdentityM(this, 0)
    },
    val velocity: Offset = Offset.Zero,
    val scale: Float = 0.5f
)
