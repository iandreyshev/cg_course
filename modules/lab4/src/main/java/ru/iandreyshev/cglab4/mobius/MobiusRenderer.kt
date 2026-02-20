package ru.iandreyshev.cglab4.mobius

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

private const val COORDS_PER_VERTEX = 3
private const val COLORS_PER_VERTEX = 4

class MobiusRenderer(res: Resources) {

    private val vertices: FloatArray
    private val colors: FloatArray
    private val vertexCount: Int

    private var _program: Int = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _mvpMatrixHandle: Int = 0
    private var _positionHandle: Int = 0
    private var _colorHandle: Int = 0

    private val _vertexBuffer: FloatBuffer
    private val _colorBuffer: FloatBuffer

    init {
        val (verts, cols) = generateGeometry()
        vertices = verts
        colors = cols
        vertexCount = vertices.size / COORDS_PER_VERTEX

        _vertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        _colorBuffer = ByteBuffer.allocateDirect(colors.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(colors)
                position(0)
            }

        Log.d("Mobius", "vertexCount=$vertexCount")
    }

    fun draw(
        state: MobiusState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
        val scaleModel = _modelMatrix.copyOf()
        Matrix.multiplyMM(_modelMatrix, 0, scaleModel, 0, state.rotationMatrix, 0)

        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        Log.d("Mobius", "mvp=${_mvpMatrix.take(4)}")

        GLES30.glUseProgram(_program)

        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        GLES30.glEnableVertexAttribArray(_positionHandle)
        GLES30.glVertexAttribPointer(
            _positionHandle, COORDS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COORDS_PER_VERTEX * Float.SIZE_BYTES, _vertexBuffer
        )

        _colorHandle = GLES30.glGetAttribLocation(_program, "vColor")
        GLES30.glEnableVertexAttribArray(_colorHandle)
        GLES30.glVertexAttribPointer(
            _colorHandle, COLORS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COLORS_PER_VERTEX * Float.SIZE_BYTES, _colorBuffer
        )

        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        Log.d("Mobius", "pos=$_positionHandle color=$_colorHandle mvp=$_mvpMatrixHandle program=$_program")
        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }

    private fun generateGeometry(): Pair<FloatArray, FloatArray> {
        val uSteps = 80
        val vSteps = 20
        val PIx2 = (2.0 * Math.PI).toFloat()

        val faceVertices = mutableListOf<Float>()
        val faceColors = mutableListOf<Float>()

        for (i in 0 until uSteps) {
            for (n in 0 until vSteps) {
                val u0 = PIx2 * i / uSteps
                val u1 = PIx2 * (i + 1) / uSteps
                val v0 = -0.5f + n.toFloat() / vSteps
                val v1 = -0.5f + (n + 1).toFloat() / vSteps

                val p00 = mobiusPoint(u0, v0)
                val p10 = mobiusPoint(u1, v0)
                val p11 = mobiusPoint(u1, v1)
                val p01 = mobiusPoint(u0, v1)

                val hue = i.toFloat() / uSteps
                val r = (sin(hue * PIx2) * 0.4f + 0.6f)
                val g = (sin(hue * PIx2 + PIx2 / 3f) * 0.4f + 0.6f)
                val b = (sin(hue * PIx2 + 2f * PIx2 / 3f) * 0.4f + 0.6f)
                val color = floatArrayOf(r, g, b, 1.0f)

                faceVertices.addAll(p00.toList())
                faceVertices.addAll(p10.toList())
                faceVertices.addAll(p11.toList())

                faceVertices.addAll(p00.toList())
                faceVertices.addAll(p11.toList())
                faceVertices.addAll(p01.toList())

                repeat(6) { faceColors.addAll(color.toList()) }
            }
        }

        return faceVertices.toFloatArray() to faceColors.toFloatArray()
    }

    private fun mobiusPoint(u: Float, v: Float): FloatArray {
        val halfU = u / 2f
        val cosU = cos(u)
        val sinU = sin(u)
        val cosHalfU = cos(halfU)
        val sinHalfU = sin(halfU)

        return floatArrayOf(
            (1f + v * cosHalfU) * cosU,
            (1f + v * cosHalfU) * sinU,
            v * sinHalfU
        )
    }

}