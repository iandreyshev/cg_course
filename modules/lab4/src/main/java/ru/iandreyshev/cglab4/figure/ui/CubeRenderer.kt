package ru.iandreyshev.cglab4.figure.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.cglab4.figure.presentation.FigureState
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val COORDS_PER_VERTEX = 3
private const val COLORS_PER_VERTEX = 4

private val CUBE_COORDS = floatArrayOf(
    // Front face (z = 0.5)
    -0.5f, -0.5f,  0.5f,
     0.5f, -0.5f,  0.5f,
     0.5f,  0.5f,  0.5f,
    -0.5f, -0.5f,  0.5f,
     0.5f,  0.5f,  0.5f,
    -0.5f,  0.5f,  0.5f,

    // Back face (z = -0.5)
     0.5f, -0.5f, -0.5f,
    -0.5f, -0.5f, -0.5f,
    -0.5f,  0.5f, -0.5f,
     0.5f, -0.5f, -0.5f,
    -0.5f,  0.5f, -0.5f,
     0.5f,  0.5f, -0.5f,

    // Top face (y = 0.5)
    -0.5f,  0.5f,  0.5f,
     0.5f,  0.5f,  0.5f,
     0.5f,  0.5f, -0.5f,
    -0.5f,  0.5f,  0.5f,
     0.5f,  0.5f, -0.5f,
    -0.5f,  0.5f, -0.5f,

    // Bottom face (y = -0.5)
    -0.5f, -0.5f, -0.5f,
     0.5f, -0.5f, -0.5f,
     0.5f, -0.5f,  0.5f,
    -0.5f, -0.5f, -0.5f,
     0.5f, -0.5f,  0.5f,
    -0.5f, -0.5f,  0.5f,

    // Right face (x = 0.5)
     0.5f, -0.5f,  0.5f,
     0.5f, -0.5f, -0.5f,
     0.5f,  0.5f, -0.5f,
     0.5f, -0.5f,  0.5f,
     0.5f,  0.5f, -0.5f,
     0.5f,  0.5f,  0.5f,

    // Left face (x = -0.5)
    -0.5f, -0.5f, -0.5f,
    -0.5f, -0.5f,  0.5f,
    -0.5f,  0.5f,  0.5f,
    -0.5f, -0.5f, -0.5f,
    -0.5f,  0.5f,  0.5f,
    -0.5f,  0.5f, -0.5f,
)

private val CUBE_COLORS = floatArrayOf(
    // Front - red
    1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,
    1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,

    // Back - green
    0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,
    0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,

    // Top - blue
    0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,
    0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,

    // Bottom - yellow
    1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,
    1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,

    // Right - cyan
    0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f,
    0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f,

    // Left - magenta
    1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,
    1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,
)

class CubeRenderer(res: Resources) {

    private var _program: Int = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _mvpMatrixHandle: Int = 0
    private var _positionHandle: Int = 0
    private var _colorHandle: Int = 0

    private val _vertexCount: Int = CUBE_COORDS.size / COORDS_PER_VERTEX

    private val _vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(CUBE_COORDS.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(CUBE_COORDS)
            position(0)
        }

    private val _colorBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(CUBE_COLORS.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(CUBE_COLORS)
            position(0)
        }

    fun draw(
        state: FigureState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
        Matrix.rotateM(_modelMatrix, 0, state.rotation.y, 1f, 0f, 0f)
        Matrix.rotateM(_modelMatrix, 0, state.rotation.x, 0f, 1f, 0f)

        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        GLES30.glUseProgram(_program)

        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        GLES30.glEnableVertexAttribArray(_positionHandle)
        GLES30.glVertexAttribPointer(
            _positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            COORDS_PER_VERTEX * Float.SIZE_BYTES,
            _vertexBuffer,
        )

        _colorHandle = GLES30.glGetAttribLocation(_program, "vColor")
        GLES30.glEnableVertexAttribArray(_colorHandle)
        GLES30.glVertexAttribPointer(
            _colorHandle,
            COLORS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            COLORS_PER_VERTEX * Float.SIZE_BYTES,
            _colorBuffer,
        )

        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, _vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }
}
