package ru.iandreyshev.cglab3.asteroids.ui.openGL.objects

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab3.R
import ru.iandreyshev.cglab3.asteroids.domain.ShipState
import ru.iandreyshev.cglab3.common.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val COORDS_PER_VERTEX = 3
private val SHIP_COORDS = floatArrayOf(
    +00f, +50f, 0f,
    -25f, -30f, 0f,
    +25f, -30f, 0f
)

class ShipCGDrawable(
    res: Resources
) {
    // create empty OpenGL ES Program
    private var mProgram: Int = GLES30.glCreateProgram().also {
        // add the vertex shader to program
        val vertShader = res.loadShader(GLES30.GL_VERTEX_SHADER, R.raw.ship_vert)
        GLES30.glAttachShader(it, vertShader)

        // add the fragment shader to program
        val fragShader = res.loadShader(GLES30.GL_FRAGMENT_SHADER, R.raw.ship_frag)
        GLES30.glAttachShader(it, fragShader)

        // creates OpenGL ES program executables
        GLES30.glLinkProgram(it)
    }

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _mvpMatrixHandle: Int = 0
    private var _positionHandle: Int = 0
    private var _colorHandle: Int = 0

    private val _vertexCount: Int = SHIP_COORDS.size / COORDS_PER_VERTEX
    private val _vertexStride: Int = COORDS_PER_VERTEX * Float.SIZE_BYTES // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(SHIP_COORDS.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(SHIP_COORDS)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    fun draw(
        state: ShipState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) { // pass in the calculated transformation matrix
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.translateM(_modelMatrix, 0, state.pos.x, state.pos.y, 0f)
        Matrix.rotateM(_modelMatrix, 0, state.rotation, 0f, 0f, 1f)

        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        GLES30.glUseProgram(mProgram)

        _positionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")

        GLES30.glEnableVertexAttribArray(_positionHandle)
        GLES30.glVertexAttribPointer(
            _positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,               // normalized
            _vertexStride,       // шаг в буффере
            vertexBuffer         // буффер координат
        )

        _colorHandle = GLES30.glGetUniformLocation(mProgram, "vColor")
        GLES30.glUniform4fv(_colorHandle, 1, color, 0)

        _mvpMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, _vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
    }
}

