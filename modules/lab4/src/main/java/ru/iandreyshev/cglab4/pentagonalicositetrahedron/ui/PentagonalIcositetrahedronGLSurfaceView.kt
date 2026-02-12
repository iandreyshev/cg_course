package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronState

class PentagonalIcositetrahedronGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val _renderer: PentagonalIcositetrahedronGLRenderer

    init {
        setEGLContextClientVersion(3)
        _renderer = PentagonalIcositetrahedronGLSurfaceView(resources)
        setRenderer(_renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateState(state: PentagonalIcositetrahedronState) {
        _renderer.updateState(state)
        requestRender()
    }

}
