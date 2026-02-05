package ru.iandreyshev.cglab4.stellateddodecahedron.ui

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab4.R
import ru.iandreyshev.cglab4.figure.presentation.FigureState
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

private const val COORDS_PER_VERTEX = 3
private const val COLORS_PER_VERTEX = 4

/**
 * Great Stellated Dodecahedron (Третья звёздчатая форма додекаэдра)
 * Kepler-Poinsot polyhedron {5/2, 3}
 * - 12 pentagrammic faces (star pentagons)
 * - 20 vertices
 * - 30 edges
 */
class StellatedDodecahedronRenderer(res: Resources) {

    private val phi = ((1.0 + sqrt(5.0)) / 2.0).toFloat()

    private val vertices: FloatArray
    private val colors: FloatArray
    private val vertexCount: Int

    init {
        val (verts, cols) = generateStellatedDodecahedron()
        vertices = verts
        colors = cols
        vertexCount = vertices.size / COORDS_PER_VERTEX
    }

    private fun generateStellatedDodecahedron(): Pair<FloatArray, FloatArray> {
        // 20 vertices of the great stellated dodecahedron
        // Same as vertices of a regular dodecahedron
        val dodecahedronVertices = listOf(
            // Cube vertices (±1, ±1, ±1)
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(1f, 1f, -1f),
            floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, -1f, -1f),
            floatArrayOf(-1f, 1f, 1f),
            floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(-1f, -1f, 1f),
            floatArrayOf(-1f, -1f, -1f),
            // Rectangle vertices (0, ±1/φ, ±φ)
            floatArrayOf(0f, 1f/phi, phi),
            floatArrayOf(0f, 1f/phi, -phi),
            floatArrayOf(0f, -1f/phi, phi),
            floatArrayOf(0f, -1f/phi, -phi),
            // Rectangle vertices (±1/φ, ±φ, 0)
            floatArrayOf(1f/phi, phi, 0f),
            floatArrayOf(1f/phi, -phi, 0f),
            floatArrayOf(-1f/phi, phi, 0f),
            floatArrayOf(-1f/phi, -phi, 0f),
            // Rectangle vertices (±φ, 0, ±1/φ)
            floatArrayOf(phi, 0f, 1f/phi),
            floatArrayOf(phi, 0f, -1f/phi),
            floatArrayOf(-phi, 0f, 1f/phi),
            floatArrayOf(-phi, 0f, -1f/phi),
        )

        // Normalize vertices to unit sphere
        val normalizedVerts = dodecahedronVertices.map { v ->
            val len = sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2])
            floatArrayOf(v[0]/len, v[1]/len, v[2]/len)
        }

        // 12 pentagonal faces of dodecahedron (indices into vertices)
        // These define the base pentagons that we'll stellate
        val pentagonFaces = listOf(
            listOf(0, 8, 10, 2, 16),   // Front-right
            listOf(0, 16, 17, 1, 12),  // Top-right
            listOf(0, 12, 14, 4, 8),   // Top-front
            listOf(1, 17, 3, 11, 9),   // Right-back
            listOf(1, 9, 5, 14, 12),   // Top-back
            listOf(2, 10, 6, 15, 13),  // Bottom-front
            listOf(2, 13, 3, 17, 16),  // Right-bottom
            listOf(3, 13, 15, 7, 11),  // Bottom-back
            listOf(4, 14, 5, 19, 18),  // Left-top
            listOf(4, 18, 6, 10, 8),   // Left-front
            listOf(5, 9, 11, 7, 19),   // Back
            listOf(6, 18, 19, 7, 15),  // Left-bottom
        )

        val faceVertices = mutableListOf<Float>()
        val faceColors = mutableListOf<Float>()

        // Colors for 12 faces
        val faceColorPalette = listOf(
            floatArrayOf(1.0f, 0.2f, 0.2f, 1.0f),   // Red
            floatArrayOf(0.2f, 1.0f, 0.2f, 1.0f),   // Green
            floatArrayOf(0.2f, 0.2f, 1.0f, 1.0f),   // Blue
            floatArrayOf(1.0f, 1.0f, 0.2f, 1.0f),   // Yellow
            floatArrayOf(1.0f, 0.2f, 1.0f, 1.0f),   // Magenta
            floatArrayOf(0.2f, 1.0f, 1.0f, 1.0f),   // Cyan
            floatArrayOf(1.0f, 0.6f, 0.2f, 1.0f),   // Orange
            floatArrayOf(0.6f, 0.2f, 1.0f, 1.0f),   // Purple
            floatArrayOf(0.2f, 0.6f, 0.2f, 1.0f),   // Dark green
            floatArrayOf(0.8f, 0.8f, 0.2f, 1.0f),   // Gold
            floatArrayOf(0.2f, 0.8f, 0.8f, 1.0f),   // Teal
            floatArrayOf(0.8f, 0.2f, 0.6f, 1.0f),   // Pink
        )

        // For each pentagon face, create a stellated (star) shape
        for ((faceIndex, face) in pentagonFaces.withIndex()) {
            val color = faceColorPalette[faceIndex % faceColorPalette.size]

            // Get the 5 vertices of the pentagon
            val v = face.map { normalizedVerts[it] }

            // Calculate center of pentagon
            val center = floatArrayOf(
                v.map { it[0] }.average().toFloat(),
                v.map { it[1] }.average().toFloat(),
                v.map { it[2] }.average().toFloat()
            )

            // Calculate stellated points (extend beyond the original vertices)
            // For great stellated dodecahedron, the spike extends by golden ratio
            val spikeHeight = phi * 1.5f

            // Normal vector of the pentagon face
            val edge1 = floatArrayOf(v[1][0]-v[0][0], v[1][1]-v[0][1], v[1][2]-v[0][2])
            val edge2 = floatArrayOf(v[2][0]-v[0][0], v[2][1]-v[0][1], v[2][2]-v[0][2])
            val normal = cross(edge1, edge2)
            val normalLen = sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2])
            normal[0] /= normalLen
            normal[1] /= normalLen
            normal[2] /= normalLen

            // Determine if normal points outward
            val centerToOrigin = floatArrayOf(-center[0], -center[1], -center[2])
            val dot = normal[0]*centerToOrigin[0] + normal[1]*centerToOrigin[1] + normal[2]*centerToOrigin[2]
            if (dot > 0) {
                normal[0] = -normal[0]
                normal[1] = -normal[1]
                normal[2] = -normal[2]
            }

            // Spike apex
            val apex = floatArrayOf(
                center[0] + normal[0] * spikeHeight,
                center[1] + normal[1] * spikeHeight,
                center[2] + normal[2] * spikeHeight
            )

            // Create 5 triangular faces for the spike
            for (i in 0 until 5) {
                val v1 = v[i]
                val v2 = v[(i + 1) % 5]

                // Triangle: apex, v1, v2
                faceVertices.addAll(apex.toList())
                faceColors.addAll(color.toList())
                faceVertices.addAll(v1.toList())
                faceColors.addAll(color.toList())
                faceVertices.addAll(v2.toList())
                faceColors.addAll(color.toList())
            }

            // Create inner star pattern (pentagram)
            // Find the intersection points of the pentagram
            val starPoints = mutableListOf<FloatArray>()
            for (i in 0 until 5) {
                // Each point of pentagram connects to the vertex 2 positions away
                val p1 = v[i]
                val p2 = v[(i + 2) % 5]
                val p3 = v[(i + 1) % 5]
                val p4 = v[(i + 3) % 5]

                // Find intersection of lines p1-p2 and p3-p4
                val intersection = lineIntersection3D(p1, p2, p3, p4, center, normal)
                starPoints.add(intersection)
            }

            // Create inner pentagram triangles pointing inward
            val innerApex = floatArrayOf(
                center[0] - normal[0] * spikeHeight * 0.3f,
                center[1] - normal[1] * spikeHeight * 0.3f,
                center[2] - normal[2] * spikeHeight * 0.3f
            )

            // Darker color for inner parts
            val innerColor = floatArrayOf(
                color[0] * 0.6f,
                color[1] * 0.6f,
                color[2] * 0.6f,
                1.0f
            )

            for (i in 0 until 5) {
                val sp1 = starPoints[i]
                val sp2 = starPoints[(i + 1) % 5]

                faceVertices.addAll(innerApex.toList())
                faceColors.addAll(innerColor.toList())
                faceVertices.addAll(sp1.toList())
                faceColors.addAll(innerColor.toList())
                faceVertices.addAll(sp2.toList())
                faceColors.addAll(innerColor.toList())
            }
        }

        return Pair(faceVertices.toFloatArray(), faceColors.toFloatArray())
    }

    private fun cross(a: FloatArray, b: FloatArray): FloatArray {
        return floatArrayOf(
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        )
    }

    private fun lineIntersection3D(
        p1: FloatArray, p2: FloatArray,
        p3: FloatArray, p4: FloatArray,
        planePoint: FloatArray, planeNormal: FloatArray
    ): FloatArray {
        // Project lines onto the plane and find intersection
        // Simplified: use parametric intersection
        val d1 = floatArrayOf(p2[0]-p1[0], p2[1]-p1[1], p2[2]-p1[2])
        val d2 = floatArrayOf(p4[0]-p3[0], p4[1]-p3[1], p4[2]-p3[2])

        // Find t where the lines are closest
        val r = floatArrayOf(p1[0]-p3[0], p1[1]-p3[1], p1[2]-p3[2])

        val a = d1[0]*d1[0] + d1[1]*d1[1] + d1[2]*d1[2]
        val b = d1[0]*d2[0] + d1[1]*d2[1] + d1[2]*d2[2]
        val c = d2[0]*d2[0] + d2[1]*d2[1] + d2[2]*d2[2]
        val d = d1[0]*r[0] + d1[1]*r[1] + d1[2]*r[2]
        val e = d2[0]*r[0] + d2[1]*r[1] + d2[2]*r[2]

        val denom = a*c - b*b
        val t = if (kotlin.math.abs(denom) > 0.0001f) (b*e - c*d) / denom else 0.5f

        return floatArrayOf(
            p1[0] + t * d1[0],
            p1[1] + t * d1[1],
            p1[2] + t * d1[2]
        )
    }

    private var _program: Int = createProgramGLES30(res, R.raw.cube_vert, R.raw.cube_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _mvpMatrixHandle: Int = 0
    private var _positionHandle: Int = 0
    private var _colorHandle: Int = 0

    private val _vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertices)
            position(0)
        }

    private val _colorBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(colors.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(colors)
            position(0)
        }

    fun draw(
        state: FigureState,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
    ) {
        Matrix.setIdentityM(_modelMatrix, 0)
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

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }
}
