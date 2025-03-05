package ru.iandreyshev.cglab3.bezier.ui.openGL

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import androidx.compose.ui.geometry.Size
import ru.iandreyshev.cglab3.bezier.presentation.BezierState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class BezierGLRenderer(
    private val resources: Resources
) : GLSurfaceView.Renderer {

    @Volatile
    var state: BezierState = BezierState()

    private var _screenSize: Size = Size.Zero

    private lateinit var _curveDrawable: CurveOpenGLDrawable
    private lateinit var _pointsDrawable: PointOpenGLDrawable

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        _curveDrawable = CurveOpenGLDrawable(resources)
        _pointsDrawable = PointOpenGLDrawable(resources)
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        _curveDrawable.draw(_screenSize, state)
        _pointsDrawable.draw(_screenSize, state)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        _screenSize = Size(width.toFloat(), height.toFloat())
    }
}

