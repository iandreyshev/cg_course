package ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation

import android.opengl.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.iandreyshev.core.BaseViewModel

class PentagonalIcositetrahedronViewModel : BaseViewModel<PentagonalIcositetrahedronState, Any>(
    initialState = PentagonalIcositetrahedronState()
) {

    init {
        viewModelScope.launch {
            while (true) {
                updateState {
                    copy(lightAngle = lightAngle + 0.02f)
                }
                delay(16)
            }
        }
    }

    fun onDrag(dragAmount: Offset) {
        updateState {
            val dragMatrix = FloatArray(16)
            val resultDragMatrix = FloatArray(16)
            println(dragAmount)

            Matrix.setIdentityM(dragMatrix, 0)
            Matrix.rotateM(dragMatrix, 0, dragAmount.y * 0.5f, 1f, 0f, 0f)
            Matrix.rotateM(dragMatrix, 0, dragAmount.x * 0.5f, 0f, 1f, 0f)

            Matrix.multiplyMM(resultDragMatrix, 0, dragMatrix, 0, rotationMatrix, 0)

            copy(rotationMatrix = resultDragMatrix)
        }
    }

}