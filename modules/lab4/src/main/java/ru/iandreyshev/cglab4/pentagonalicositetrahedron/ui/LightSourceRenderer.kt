package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class LightSourceRenderer(res: Resources) {
    private val vertices = floatArrayOf(
        0f,  1f,  0f,    1f,  0f,  0f,    0f,  0f,  1f,  // верх, +X, +Z
        0f,  1f,  0f,    0f,  0f,  1f,   -1f,  0f,  0f,  // верх, +Z, -X
        0f,  1f,  0f,   -1f,  0f,  0f,    0f,  0f, -1f,  // верх, -X, -Z
        0f,  1f,  0f,    0f,  0f, -1f,    1f,  0f,  0f,  // верх, -Z, +X

        0f, -1f,  0f,    0f,  0f,  1f,    1f,  0f,  0f,  // низ, +Z, +X
        0f, -1f,  0f,   -1f,  0f,  0f,    0f,  0f,  1f,  // низ, -X, +Z
        0f, -1f,  0f,    0f,  0f, -1f,   -1f,  0f,  0f,  // низ, -Z, -X
        0f, -1f,  0f,    1f,  0f,  0f,    0f,  0f, -1f,  // низ, +X, -Z
    )

    private val colors = FloatArray(24 * 4) {
        if (it % 4 == 3) 1.0f else 1.0f
    }

    private val vertexCount = vertices.size / 3

    private var _program = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private val _vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(vertices); position(0) }

    private val _colorBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(colors.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(colors); position(0) }

    fun draw(
        position: FloatArray,
        scale: Float,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.translateM(_modelMatrix, 0, position[0], position[1], position[2])
        Matrix.scaleM(_modelMatrix, 0, scale, scale, scale)

        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        GLES30.glUseProgram(_program)

        val posHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        GLES30.glEnableVertexAttribArray(posHandle)
        GLES30.glVertexAttribPointer(
            posHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.SIZE_BYTES, _vertexBuffer
        )

        val colorHandle = GLES30.glGetAttribLocation(_program, "vColor")
        GLES30.glEnableVertexAttribArray(colorHandle)
        GLES30.glVertexAttribPointer(colorHandle, 4, GLES30.GL_FLOAT, false, 4 * Float.SIZE_BYTES, _colorBuffer)

        val mvpHande = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(mvpHande, 1, false, _mvpMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDisableVertexAttribArray(posHandle)
        GLES30.glDisableVertexAttribArray(colorHandle)
    }
}