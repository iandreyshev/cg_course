package ru.iandreyshev.cglab4.stellateddodecahedron.ui

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab4.stellateddodecahedron.presentation.StellatedDodecahedronState

class StellatedDodecahedronGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val _renderer: StellatedDodecahedronGLRenderer

    init {
        setEGLContextClientVersion(3)
        _renderer = StellatedDodecahedronGLRenderer(resources)
        setRenderer(_renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateState(state: StellatedDodecahedronState) {
        _renderer.updateState(state)
        requestRender()
    }
}
