package ru.iandreyshev.cglab3.asteroids.ui.openGL

import android.content.Context
import android.opengl.GLSurfaceView
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsState

class AsteroidsGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: AsteroidsGLRenderer

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(3)

        renderer = AsteroidsGLRenderer(context.resources)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun update(state: AsteroidsState) {
        renderer.update(state)
        requestRender()
    }
}

