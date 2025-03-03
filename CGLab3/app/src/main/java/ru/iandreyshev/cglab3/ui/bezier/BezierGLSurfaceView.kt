package ru.iandreyshev.cglab3.ui.bezier

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab3.ui.guide.GuideGLRenderer

class BezierGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: GuideGLRenderer

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(3)

        renderer = GuideGLRenderer(context.resources)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}