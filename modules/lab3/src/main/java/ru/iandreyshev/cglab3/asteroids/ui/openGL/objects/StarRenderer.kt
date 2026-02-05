package ru.iandreyshev.cglab3.asteroids.ui.openGL.objects

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import androidx.compose.ui.geometry.Size
import ru.iandreyshev.cglab3.R
import ru.iandreyshev.cglab3.asteroids.domain.AstConst
import ru.iandreyshev.cglab3.asteroids.domain.BulletState
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val COORDS_PER_VERTEX = 2

class StarRenderer(res: Resources) {
    private var _program = createProgramGLES30(res, R.raw.star_vert, R.raw.star_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _positionHandle: Int = 0
    private var _resolutionHandle: Int = 0
    private var _pointSizeHandle: Int = 0
    private var _mvpMatrixHandle: Int = 0

    private var _resolution = floatArrayOf(0f, 0f)
    private var _vertexBuffer =
        ByteBuffer.allocateDirect(COORDS_PER_VERTEX * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(floatArrayOf(0f, 0f))
                position(0)
            }
        }

    fun onResolutionChange(resolution: Size) {
        _resolution = floatArrayOf(resolution.width, resolution.height)
    }

    fun draw(
        state: BulletState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.translateM(_modelMatrix, 0, state.position.x, state.position.y, 0f)

        Matrix.setIdentityM(_viewModelMatrix, 0)
        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        // Add program to OpenGL ES environment
        GLES30.glUseProgram(_program)

        // get handle to vertex shader's vPosition member
        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(_positionHandle)
        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
            _positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,                                      // normalized
            COORDS_PER_VERTEX * Float.SIZE_BYTES,       // шаг в буффере
            _vertexBuffer                               // буффер координат
        )

        val wHandle = GLES30.glGetUniformLocation(_program, "vResolutionWidth")
        GLES30.glUniform1f(wHandle,_resolution[0])

        val hHandle = GLES30.glGetUniformLocation(_program, "vResolutionHeight")
        GLES30.glUniform1f(hHandle,_resolution[1])

        _pointSizeHandle = GLES30.glGetUniformLocation(_program, "vPointSize")
        GLES30.glUniform1f(_pointSizeHandle, AstConst.Bullet.RADIUS * 2)

        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, COORDS_PER_VERTEX)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(_positionHandle)
    }
}
