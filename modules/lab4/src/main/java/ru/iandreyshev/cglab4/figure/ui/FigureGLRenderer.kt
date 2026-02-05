package ru.iandreyshev.cglab4.figure.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.compose.ui.geometry.Size
import ru.iandreyshev.cglab4.figure.presentation.FigureState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FigureGLRenderer(
    private val resources: Resources
) : GLSurfaceView.Renderer {

    private val _projectionMatrix = FloatArray(16)
    private val _viewMatrix = FloatArray(16)
    private lateinit var _cubeDrawable: CubeRenderer

    @Volatile
    private var _state = FigureState()

    init {
        // Set the camera position (View matrix)
//        Matrix.setIdentityM(_viewMatrix, 0)
        Matrix.setLookAtM(
            _viewMatrix,    // raw matrix
            0,              // rm offset
            0f, 0f, 1f,     // eye point
            0f, 0f, 0f,     // view center
            0f, 1.0f, 0.0f  // up factor
        )
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        _cubeDrawable = CubeRenderer(resources)
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        _cubeDrawable.draw(_state, _viewMatrix, _projectionMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        println("width: $width")
        println("height: $height")

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
//        Matrix.frustumM(
//            _projectionMatrix,  // matrix
//            0,                  // mOffset
//            -width / 2f,        // left
//            width / 2f,         // right
//            -height / 2f,       // bottom
//            height / 2f,        // top
//            1f,                 // near
//            20f                 // far
//        )

        val aspect = width.toFloat() / height.toFloat()
        println("aspect: $aspect")
        Matrix.perspectiveM(
            _projectionMatrix,  // matrix
            0,                  // mOffset,
            45f,
            aspect,
            0.1f,                 // near
            100f                 // far
        )
    }

    fun updateState(state: FigureState) {
        _state = state
    }
}

