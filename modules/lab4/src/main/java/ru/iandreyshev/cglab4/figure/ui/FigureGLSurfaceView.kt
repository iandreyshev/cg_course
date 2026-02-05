package ru.iandreyshev.cglab4.figure.ui

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab4.figure.presentation.FigureState

class FigureGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val _renderer: FigureGLRenderer

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)

        _renderer = FigureGLRenderer(resources)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(_renderer)

        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateState(state: FigureState) {
        _renderer.updateState(state)
        requestRender()
    }

}
