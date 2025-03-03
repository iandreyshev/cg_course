package ru.iandreyshev.cglab3.ui.guide

import android.content.res.Resources
import android.opengl.GLES30
import ru.iandreyshev.cglab3.R
import ru.iandreyshev.cglab3.ui.common.loadShaderGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val COORDS_PER_VERTEX = 3
var triangleCoords = floatArrayOf(  // in counterclockwise order:
    0.0f, 0.5f, 0.0f,               // top
    -0.5f, -0.0f, 0.0f,             // bottom left
    0.5f, -0.0f, 0.0f       // bottom right
)

class Triangle(
    res: Resources
) {
    private var mProgram: Int

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(triangleCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    init {
        // create empty OpenGL ES Program
        mProgram = GLES30.glCreateProgram().also {
            // add the vertex shader to program
            val vertShader = res.loadShaderGLES30(GLES30.GL_VERTEX_SHADER, R.raw.vert_triangle)
            GLES30.glAttachShader(it, vertShader)

            // add the fragment shader to program
            val fragShader = res.loadShaderGLES30(GLES30.GL_FRAGMENT_SHADER, R.raw.frag_triangle)
            GLES30.glAttachShader(it, fragShader)

            // creates OpenGL ES program executables
            GLES30.glLinkProgram(it)
        }
    }

    fun draw() {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        positionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(positionHandle)

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,              // normalized
            vertexStride,       // шаг в буффере
            vertexBuffer        // буффер координат
        )

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor")
        // Set color for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, color, 0)

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle)
    }
}
