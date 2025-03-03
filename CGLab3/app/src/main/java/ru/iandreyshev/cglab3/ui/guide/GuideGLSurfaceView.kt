package ru.iandreyshev.cglab3.ui.guide

import android.content.Context
import android.opengl.GLSurfaceView

class GuideGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: GuideGLRenderer

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(3)

        renderer = GuideGLRenderer(context.resources)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        renderMode = RENDERMODE_WHEN_DIRTY
    }
}
