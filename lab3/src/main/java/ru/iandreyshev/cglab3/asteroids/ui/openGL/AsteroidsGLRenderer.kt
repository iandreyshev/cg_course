package ru.iandreyshev.cglab3.asteroids.ui.openGL

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import androidx.compose.ui.geometry.Size
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsState
import ru.iandreyshev.cglab3.asteroids.ui.openGL.objects.BulletRenderer
import ru.iandreyshev.cglab3.asteroids.ui.openGL.objects.EnemyRenderer
import ru.iandreyshev.cglab3.asteroids.ui.openGL.objects.ParticleRenderer
import ru.iandreyshev.cglab3.asteroids.ui.openGL.objects.ShipRenderer
import ru.iandreyshev.cglab3.asteroids.ui.openGL.objects.StarRenderer
import ru.iandreyshev.core.handleErrorsGLES30
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class AsteroidsGLRenderer(
    private val res: Resources
) : GLSurfaceView.Renderer {

    private val _projectionMatrix = FloatArray(16)
    private val _viewMatrix = FloatArray(16)

    private lateinit var _state: AsteroidsState

    private lateinit var _shipRenderer: ShipRenderer
    private lateinit var _enemyRenderer: EnemyRenderer
    private lateinit var _bulletRenderer: BulletRenderer
    private lateinit var _particleRenderer: ParticleRenderer
    private lateinit var _starRenderer: StarRenderer

    init {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(
            _viewMatrix,    // raw matrix
            0,              // rm offset
            0f, 0f, 3f,     // eye point
            0f, 0f, 0f,     // view center
            0f, 1.0f, 0.0f  // up factor
        )
    }

    fun update(state: AsteroidsState) {
        _state = state
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        _shipRenderer = ShipRenderer(res)
        _enemyRenderer = EnemyRenderer(res)
        _bulletRenderer = BulletRenderer(res)
        _particleRenderer = ParticleRenderer(res)
        _starRenderer = StarRenderer(res)
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        val time = System.currentTimeMillis()

        // Draw shape
        _state.enemies.forEach {
            _enemyRenderer.draw(it, time, _viewMatrix, _projectionMatrix)
        }
        _state.bullets.forEach {
            _bulletRenderer.draw(it, _viewMatrix, _projectionMatrix)
        }
        _state.stars.forEach {
            _starRenderer.draw(it, _viewMatrix, _projectionMatrix)
        }
        _state.particles.forEach {
            _particleRenderer.draw(it, _viewMatrix, _projectionMatrix)
        }
        _state.ship?.let {
            _shipRenderer.draw(it, _viewMatrix, _projectionMatrix)
        }

        handleErrorsGLES30()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        _starRenderer.onResolutionChange(Size(width.toFloat(), height.toFloat()))

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.orthoM(
            _projectionMatrix,                                   // matrix
            0,                                                   // mOffset
            -width / 2f, width / 2f, -height / 2f, height / 2f,  // left, right, bottom, top
            -1f, 7f                                              // near, far
        )
    }
}

