package ru.iandreyshev.cglab3.bezier.ui.openGL

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab3.bezier.presentation.BezierState

class BezierGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: BezierGLRenderer

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)

        renderer = BezierGLRenderer(resources)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateState(state: BezierState) {
        renderer.state = state
        requestRender()
    }
}