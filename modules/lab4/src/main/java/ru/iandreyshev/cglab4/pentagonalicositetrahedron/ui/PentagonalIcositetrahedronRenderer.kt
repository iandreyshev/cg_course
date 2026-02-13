package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronState
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val COORDS_PER_VERTEX = 3
private const val COLORS_PER_VERTEX = 4

class PentagonalIcositetrahedronRenderer(res: Resources) {

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
            .apply { put(vertices); position(0) }

        _colorBuffer = ByteBuffer.allocateDirect(colors.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(colors); position(0) }
    }

    fun draw(
        state: PentagonalIcositetrahedronState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.scaleM(_modelMatrix, 0, state.scale, state.scale, state.scale)
        val scaledModel = _modelMatrix.copyOf()
        Matrix.multiplyMM(_modelMatrix, 0, scaledModel, 0, state.rotationMatrix, 0)

        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

        GLES30.glUseProgram(_program)

        _positionHandle = GLES30.glGetAttribLocation(_program, "vPosition")
        GLES30.glEnableVertexAttribArray(_positionHandle)
        GLES30.glVertexAttribPointer(
            _positionHandle, COORDS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COORDS_PER_VERTEX * Float.SIZE_BYTES, _vertexBuffer,
        )

        _colorHandle = GLES30.glGetAttribLocation(_program, "vColor")
        GLES30.glEnableVertexAttribArray(_colorHandle)
        GLES30.glVertexAttribPointer(
            _colorHandle, COLORS_PER_VERTEX, GLES30.GL_FLOAT,
            false, COLORS_PER_VERTEX * Float.SIZE_BYTES, _colorBuffer,
        )

        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }



    private fun generateGeometry(): Pair<FloatArray, FloatArray> {
        val C0 = 0.2187966430f
        val C1 = 0.7401837414f
        val C2 = 1.0236561781f
        val C3 = 1.3614101519f

        val polyVerts = arrayOf(
            floatArrayOf( C3,   0f,   0f),
            floatArrayOf(-C3,   0f,   0f),
            floatArrayOf(  0f,  C3,   0f),
            floatArrayOf(  0f, -C3,   0f),
            floatArrayOf(  0f,   0f,  C3),
            floatArrayOf(  0f,   0f, -C3),
            floatArrayOf( C1,  C1,  C1),
            floatArrayOf( C1,  C1, -C1),
            floatArrayOf( C1, -C1,  C1),
            floatArrayOf( C1, -C1, -C1),
            floatArrayOf(-C1,  C1,  C1),
            floatArrayOf(-C1,  C1, -C1),
            floatArrayOf(-C1, -C1,  C1),
            floatArrayOf(-C1, -C1, -C1),
            floatArrayOf( C0,  C2,  C1),
            floatArrayOf( C0, -C2, -C1),
            floatArrayOf(-C0,  C2, -C1),
            floatArrayOf(-C0, -C2,  C1),
            floatArrayOf( C2,  C1,  C0),
            floatArrayOf( C2, -C1, -C0),
            floatArrayOf(-C2,  C1, -C0),
            floatArrayOf(-C2, -C1,  C0),
            floatArrayOf( C1,  C0,  C2),
            floatArrayOf( C1, -C0, -C2),
            floatArrayOf(-C1,  C0, -C2),
            floatArrayOf(-C1, -C0,  C2),
            floatArrayOf( C0,  C1, -C2),
            floatArrayOf( C0, -C1,  C2),
            floatArrayOf(-C0,  C1,  C2),
            floatArrayOf(-C0, -C1, -C2),
            floatArrayOf( C1,  C2, -C0),
            floatArrayOf( C1, -C2,  C0),
            floatArrayOf(-C1,  C2,  C0),
            floatArrayOf(-C1, -C2, -C0),
            floatArrayOf( C2,  C0, -C1),
            floatArrayOf( C2, -C0,  C1),
            floatArrayOf(-C2,  C0,  C1),
            floatArrayOf(-C2, -C0, -C1),
        )

        val facesByVerts = arrayOf(
            intArrayOf(15, 29,  5, 23,  9),
            intArrayOf(33, 21,  1, 37, 13),
            intArrayOf(21, 33,  3, 17, 12),
            intArrayOf(29, 15,  3, 33, 13),
            intArrayOf(19, 31,  3, 15,  9),
            intArrayOf(31, 19,  0, 35,  8),
            intArrayOf(17, 27,  4, 25, 12),
            intArrayOf(35, 22,  4, 27,  8),
            intArrayOf(27, 17,  3, 31,  8),
            intArrayOf(37, 24,  5, 29, 13),
            intArrayOf(24, 37,  1, 20, 11),
            intArrayOf(23, 34,  0, 19,  9),
            intArrayOf(34, 23,  5, 26,  7),
            intArrayOf(25, 36,  1, 21, 12),
            intArrayOf(32, 20,  1, 36, 10),
            intArrayOf(30, 18,  0, 34,  7),
            intArrayOf(16, 26,  5, 24, 11),
            intArrayOf(20, 32,  2, 16, 11),
            intArrayOf(26, 16,  2, 30,  7),
            intArrayOf(36, 25,  4, 28, 10),
            intArrayOf(28, 14,  2, 32, 10),
            intArrayOf(22, 35,  0, 18,  6),
            intArrayOf(18, 30,  2, 14,  6),
            intArrayOf(14, 28,  4, 22,  6),
        )

        val faceColorPalette = arrayOf(
            floatArrayOf(0.95f, 0.25f, 0.20f, 0.23f),
            floatArrayOf(0.20f, 0.80f, 0.30f, 0.23f),
            floatArrayOf(0.20f, 0.40f, 0.95f, 0.23f),
            floatArrayOf(0.95f, 0.85f, 0.20f, 0.23f),
            floatArrayOf(0.85f, 0.25f, 0.85f, 0.23f),
            floatArrayOf(0.20f, 0.85f, 0.85f, 0.23f),
            floatArrayOf(0.95f, 0.55f, 0.15f, 0.23f),
            floatArrayOf(0.55f, 0.20f, 0.90f, 0.23f),
            floatArrayOf(0.25f, 0.65f, 0.25f, 0.23f),
            floatArrayOf(0.85f, 0.75f, 0.30f, 0.23f),
            floatArrayOf(0.25f, 0.75f, 0.75f, 0.23f),
            floatArrayOf(0.85f, 0.30f, 0.55f, 0.23f),
            floatArrayOf(0.90f, 0.45f, 0.15f, 0.23f),
            floatArrayOf(0.40f, 0.15f, 0.85f, 0.23f),
            floatArrayOf(0.15f, 0.85f, 0.45f, 0.23f),
            floatArrayOf(0.85f, 0.85f, 0.50f, 0.23f),
            floatArrayOf(0.50f, 0.85f, 0.85f, 0.23f),
            floatArrayOf(0.85f, 0.50f, 0.85f, 0.23f),
            floatArrayOf(0.70f, 0.30f, 0.25f, 0.23f),
            floatArrayOf(0.30f, 0.65f, 0.30f, 0.23f),
            floatArrayOf(0.60f, 0.45f, 0.80f, 0.23f),
            floatArrayOf(0.80f, 0.60f, 0.40f, 0.23f),
            floatArrayOf(0.45f, 0.70f, 0.55f, 0.23f),
            floatArrayOf(0.75f, 0.40f, 0.40f, 0.23f),
        )

        val faceVertices = mutableListOf<Float>()
        val faceColors = mutableListOf<Float>()

        facesByVerts.forEachIndexed { faceIndex, face ->
            val faceColor = faceColorPalette[faceIndex]
            val faceVerts = face.map { polyVerts[it] }

            val cx = faceVerts.map { it[0] }.average().toFloat()
            val cy = faceVerts.map { it[1] }.average().toFloat()
            val cz = faceVerts.map { it[2] }.average().toFloat()

            for (i in 0 until 5) {
                val centroid = floatArrayOf(cx, cy, cz)
                val centroidB = faceVerts[i]
                val centroidC = faceVerts[(i + 1) % 5]

                val ab = floatArrayOf(centroidB[0] - centroid[0], centroidB[1] - centroid[1], centroidB[2] - centroid[2])
                val ac = floatArrayOf(centroidC[0] - centroid[0], centroidC[1] - centroid[1], centroidC[2] - centroid[2])
                val normal = ab * ac

                val tcx = (centroid[0] + centroidB[0] + centroidC[0]) / 3f
                val tcy = (centroid[1] + centroidB[1] + centroidC[1]) / 3f
                val tcz = (centroid[2] + centroidB[2] + centroidC[2]) / 3f
                val dot = normal[0] * tcx + normal[1] * tcy + normal[2] * tcz

                if (dot >= 0) {
                    faceVertices.addAll(centroid.toList())
                    faceVertices.addAll(centroidB.toList())
                    faceVertices.addAll(centroidC.toList())
                } else {
                    faceVertices.addAll(centroid.toList())
                    faceVertices.addAll(centroidC.toList())
                    faceVertices.addAll(centroidB.toList())
                }

                faceColors.addAll(faceColor.toList())
                faceColors.addAll(faceColor.toList())
                faceColors.addAll(faceColor.toList())
            }
        }

        return Pair(faceVertices.toFloatArray(), faceColors.toFloatArray())
    }

    private infix operator fun FloatArray.times(b: FloatArray) = floatArrayOf(
        this[1] * b[2] - this[2] * b[1],
        this[2] * b[0] - this[0] * b[2],
        this[0] * b[1] - this[1] * b[0]
    )

}
