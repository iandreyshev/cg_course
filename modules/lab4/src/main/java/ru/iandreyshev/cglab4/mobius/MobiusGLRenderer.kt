package ru.iandreyshev.cglab4.mobius

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.compose.material3.FloatingActionButton
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MobiusGLRenderer(private val resources: Resources) : GLSurfaceView.Renderer {
    private val _projectionMatrix = FloatArray(16)
    private val _viewMatrix = FloatArray(16)

    private lateinit var _drawable: MobiusRenderer

    @Volatile
    private var _state = MobiusState()

    init {
        Matrix.setLookAtM(
            _viewMatrix, 0,
            0f, 0f, 5f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.02f, 0.05f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        _drawable = MobiusRenderer(resources)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        _drawable.draw(_state, _viewMatrix, _projectionMatrix)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(_projectionMatrix, 0, 45f, aspect, 0.1f, 100f)
    }

    fun updateState(state: MobiusState) {
        _state = state
    }

}