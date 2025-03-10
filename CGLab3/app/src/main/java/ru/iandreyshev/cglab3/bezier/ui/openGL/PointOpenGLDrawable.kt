package ru.iandreyshev.cglab3.bezier.ui.openGL

import android.content.res.Resources
import android.opengl.GLES30
import androidx.compose.ui.geometry.Size
import ru.iandreyshev.cglab3.R
import ru.iandreyshev.cglab3.bezier.presentation.BezierState
import ru.iandreyshev.cglab3.common.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val COORDS_PER_VERTEX = 2
private const val VERTEX_COUNT = 4
private const val BYTES_PER_VERTEX: Int = COORDS_PER_VERTEX * Float.SIZE_BYTES
private const val MAX_COORDS_COUNT = VERTEX_COUNT * COORDS_PER_VERTEX

class PointOpenGLDrawable(resources: Resources) {
    private var _program: Int = GLES30.glCreateProgram().also {
        val vertShader = resources.loadShader(GLES30.GL_VERTEX_SHADER, R.raw.curve_point_vert)
        GLES30.glAttachShader(it, vertShader)

        // add the fragment shader to program
        val fragShader = resources.loadShader(GLES30.GL_FRAGMENT_SHADER, R.raw.curve_frag)
        GLES30.glAttachShader(it, fragShader)

        // creates OpenGL ES program executables
        GLES30.glLinkProgram(it)
    }

    private var _positionHandle: Int = 0

    private var _vertexBuffer: FloatBuffer =
        // (number of coordinate values * bytes per float)
        ByteBuffer.allocateDirect(MAX_COORDS_COUNT * Float.SIZE_BYTES).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer()
        }

    fun draw(screenSize: Size, state: BezierState) {
        fillBuffer(screenSize, state)

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
            false,               // normalized
            BYTES_PER_VERTEX,       // шаг в буффере
            _vertexBuffer        // буффер координат
        )

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, VERTEX_COUNT)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(_positionHandle)
    }

    private fun fillBuffer(screenSize: Size, state: BezierState) {
        _vertexBuffer.apply {
            put(CoordsToOpenGLMapper.mapX(screenSize, state.p0.center.x))
            put(CoordsToOpenGLMapper.mapY(screenSize, state.p0.center.y))
            put(CoordsToOpenGLMapper.mapX(screenSize, state.p1.center.x))
            put(CoordsToOpenGLMapper.mapY(screenSize, state.p1.center.y))
            put(CoordsToOpenGLMapper.mapX(screenSize, state.p2.center.x))
            put(CoordsToOpenGLMapper.mapY(screenSize, state.p2.center.y))
            put(CoordsToOpenGLMapper.mapX(screenSize, state.p3.center.x))
            put(CoordsToOpenGLMapper.mapY(screenSize, state.p3.center.y))
            position(0)
        }
    }
}