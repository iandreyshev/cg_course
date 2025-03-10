package ru.iandreyshev.cglab3.asteroids.ui.openGL

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsState
import ru.iandreyshev.cglab3.asteroids.ui.openGL.objects.ShipCGDrawable
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AsteroidsGLRenderer(
    private val res: Resources
) : GLSurfaceView.Renderer {

    private val _projectionMatrix = FloatArray(16)
    private val _viewMatrix = FloatArray(16)

    private lateinit var _state: AsteroidsState

    private lateinit var _ship: ShipCGDrawable

    init {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(
            _viewMatrix,    // raw matrix
            0,              // rm offset
            0f, 0f, 3f,     // eye point
            0f, 0f, 0f,     // view center
            0f, 1.0f, 0.0f  // up factor
        )
    }

    fun update(state: AsteroidsState) {
        _state = state
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        _ship = ShipCGDrawable(res)
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // Draw shape
        _ship.draw(_state.ship, _viewMatrix, _projectionMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.orthoM(
            _projectionMatrix,                                   // matrix
            0,                                                   // mOffset
            -width / 2f, width / 2f, -height / 2f, height / 2f,  // left, right, bottom, top
            -1f, 7f                                              // near, far
        )
    }
}

