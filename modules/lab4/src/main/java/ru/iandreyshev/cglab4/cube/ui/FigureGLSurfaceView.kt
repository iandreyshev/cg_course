package ru.iandreyshev.cglab4.cube.ui

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab4.cube.presentation.FigureState

class FigureGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val _renderer: FigureGLRenderer

    init {
        setEGLContextClientVersion(3)
        _renderer = FigureGLRenderer(resources)
        setRenderer(_renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateState(state: FigureState) {
        _renderer.updateState(state)
        requestRender()
    }
}
