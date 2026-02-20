package ru.iandreyshev.cglab4.mobius

import android.content.Context
import android.opengl.GLSurfaceView

class MobiusGLSurfaceView(contex: Context): GLSurfaceView(contex) {
    private val _renderer: MobiusGLRenderer

    init {
        setEGLContextClientVersion(3)
        _renderer = MobiusGLRenderer(resources)
        setRenderer(_renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateState(state: MobiusState) {
        _renderer.updateState(state)
        requestRender()
    }
}