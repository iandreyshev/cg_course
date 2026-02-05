package ru.iandreyshev.cglab4.figure.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.cglab4.figure.presentation.FigureState
import ru.iandreyshev.core.ThemeRed
import ru.iandreyshev.core.createProgramGLES30
import ru.iandreyshev.core.floatArray
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private var z = -3.0f
private const val COORDS_PER_VERTEX = 3
private val COORDS: FloatArray
    get() = floatArrayOf(
        -0.5f, 0.5f, z,
        -0.5f, 0.1f, z,
         0.5f, 0.1f, z,

        -0.5f, 0.5f, z,   // bottom
         0.5f, 0.1f, z,   // bottom
         0.5f, 0.5f, z, // bottom
    )

class CubeRenderer(
    res: Resources
) {
    private var _program: Int = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _vpMatrixHandle: Int = 0
    private var _positionHandle: Int = 0
    private var _colorHandle: Int = 0

    private val _vertexCount: Int = COORDS.size / COORDS_PER_VERTEX
    private val _vertexStride: Int = COORDS_PER_VERTEX * Float.SIZE_BYTES // 4 bytes per vertex
    private val _color = ThemeRed.floatArray()

    fun draw(
        state: FigureState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) { // pass in the calculated transformation matrix
//        z++
//        println("draw z: $z")
        Matrix.setIdentityM(_modelMatrix, 0)
//        Matrix.translateM(_modelMatrix, 0, state.position.x, state.position.y, 0f)

//        Matrix.rotateM(_modelMatrix, 0, 0f, 90f, 0f, 1f)

//        Matrix.setIdentityM(_viewModelMatrix, 0)
        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        // Add program to OpenGL ES environment
        GLES30.glUseProgram(_program)

        // get handle to vertex shader's vPosition member
        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(_positionHandle)

        var _vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(COORDS.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(COORDS)
                    position(0)
                }
            }

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
            _positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,              // normalized
            _vertexStride,       // шаг в буффере
            _vertexBuffer        // буффер координат
        )

        // get handle to fragment shader's vColor member
        _colorHandle = GLES30.glGetUniformLocation(_program, "vColor")
        // Set color for drawing the triangle
        GLES30.glUniform4fv(_colorHandle, 1, _color, 0)

        // get handle to shape's transformation matrix
        _vpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(_vpMatrixHandle, 1, false, _mvpMatrix, 0)

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, _vertexCount)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(_positionHandle)
    }
}
