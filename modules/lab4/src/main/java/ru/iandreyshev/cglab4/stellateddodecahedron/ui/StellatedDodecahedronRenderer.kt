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
 * Большой звёздчатый додекаэдр (Great Stellated Dodecahedron)
 * Kepler-Poinsot polyhedron {5/2, 3}
 * Third stellation of the icosahedron.
 * Construction: 20 three-sided pyramids (spikes) over the faces of a regular icosahedron,
 * each spike apex lies on the ray from origin through the icosahedron face center at
 * distance phi^2 from the origin.
 * - 12 pentagrammic faces, 20 spike tips, 30 edges
 * - 60 triangles for rendering (3 per icosahedron face × 20 faces)
 */
class StellatedDodecahedronRenderer(res: Resources) {

    private val phi = ((1.0 + sqrt(5.0)) / 2.0).toFloat()

    private val vertices: FloatArray
    private val colors: FloatArray
    private val vertexCount: Int

    init {
        val (verts, cols) = generateGreatStellatedDodecahedron()
        vertices = verts
        colors = cols
        vertexCount = vertices.size / COORDS_PER_VERTEX
    }

    private fun generateGreatStellatedDodecahedron(): Pair<FloatArray, FloatArray> {
        // The great stellated dodecahedron is the third stellation of the icosahedron.
        // Construction: take a regular icosahedron, then over each of its 20 triangular
        // faces build a tall three-sided pyramid whose apex is a vertex of the outer
        // dodecahedron. The result has 12 pentagrammic faces, 20 spike tips, 60 triangles.
        //
        // Step 1: Define the 12 vertices of the base icosahedron (normalized to unit sphere).
        // Step 2: Define the 20 triangular faces of the icosahedron.
        // Step 3: For each face, find the spike apex — the point of a regular dodecahedron
        //         that lies directly above this face. The spike apex is at distance phi^2
        //         from the origin (when icosahedron has circumradius 1).
        // Step 4: Build 3 triangles per face (spike apex + each edge of the icosahedron face).

        // 12 vertices of the regular icosahedron
        val icoVertices = arrayOf(
            floatArrayOf( 0f,  1f,  phi),   // 0
            floatArrayOf( 0f,  1f, -phi),   // 1
            floatArrayOf( 0f, -1f,  phi),   // 2
            floatArrayOf( 0f, -1f, -phi),   // 3
            floatArrayOf( 1f,  phi,  0f),   // 4
            floatArrayOf( 1f, -phi,  0f),   // 5
            floatArrayOf(-1f,  phi,  0f),   // 6
            floatArrayOf(-1f, -phi,  0f),   // 7
            floatArrayOf( phi,  0f,  1f),   // 8
            floatArrayOf( phi,  0f, -1f),   // 9
            floatArrayOf(-phi,  0f,  1f),   // 10
            floatArrayOf(-phi,  0f, -1f),   // 11
        )

        // Normalize icosahedron vertices to unit sphere
        val icoNorm = icoVertices.map { v ->
            val len = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
            floatArrayOf(v[0] / len, v[1] / len, v[2] / len)
        }

        // 20 triangular faces of icosahedron
        val icoFaces = arrayOf(
            intArrayOf(0, 2, 8),
            intArrayOf(0, 8, 4),
            intArrayOf(0, 4, 6),
            intArrayOf(0, 6, 10),
            intArrayOf(0, 10, 2),
            intArrayOf(1, 3, 11),
            intArrayOf(1, 11, 6),
            intArrayOf(1, 6, 4),
            intArrayOf(1, 4, 9),
            intArrayOf(1, 9, 3),
            intArrayOf(2, 5, 8),
            intArrayOf(2, 7, 5),
            intArrayOf(2, 10, 7),
            intArrayOf(3, 9, 5),
            intArrayOf(3, 5, 7),
            intArrayOf(3, 7, 11),
            intArrayOf(4, 8, 9),
            intArrayOf(5, 9, 8),
            intArrayOf(6, 11, 10),
            intArrayOf(7, 10, 11),
        )

        // For each icosahedron face, compute the spike apex.
        // The apex lies along the outward face normal at a specific distance.
        // For the great stellated dodecahedron, the spike tips are at the vertices
        // of a dodecahedron scaled by phi^2 relative to the icosahedron circumradius.
        // Spike height above the face center = phi^2 * circumradius_ico measured from origin
        // along the face normal direction.
        //
        // More precisely: the apex = faceCenter normalized * phi^2
        // (since each spike tip of the great stellated dodecahedron lies on the ray
        // from the origin through the face center of the icosahedron, at distance phi^2).
        val spikeRadius = phi * phi  // phi^2 ≈ 2.618

        // Colors — assign by face group (5 faces around top, 5 around equator-top, etc.)
        val faceColorPalette = arrayOf(
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
            floatArrayOf(0.9f, 0.4f, 0.1f, 1.0f),   // Burnt orange
            floatArrayOf(0.4f, 0.1f, 0.9f, 1.0f),   // Indigo
            floatArrayOf(0.1f, 0.9f, 0.4f, 1.0f),   // Emerald
            floatArrayOf(0.9f, 0.9f, 0.5f, 1.0f),   // Light gold
            floatArrayOf(0.5f, 0.9f, 0.9f, 1.0f),   // Light teal
            floatArrayOf(0.9f, 0.5f, 0.9f, 1.0f),   // Light magenta
            floatArrayOf(0.7f, 0.3f, 0.3f, 1.0f),   // Dark red
            floatArrayOf(0.3f, 0.7f, 0.3f, 1.0f),   // Forest green
        )

        val faceVertices = mutableListOf<Float>()
        val faceColors = mutableListOf<Float>()

        for ((faceIndex, face) in icoFaces.withIndex()) {
            val v0 = icoNorm[face[0]]
            val v1 = icoNorm[face[1]]
            val v2 = icoNorm[face[2]]

            // Face center direction (not normalized yet)
            val cx = (v0[0] + v1[0] + v2[0]) / 3f
            val cy = (v0[1] + v1[1] + v2[1]) / 3f
            val cz = (v0[2] + v1[2] + v2[2]) / 3f
            val cLen = sqrt(cx * cx + cy * cy + cz * cz)

            // Spike apex: along face center direction at distance spikeRadius
            val apex = floatArrayOf(
                cx / cLen * spikeRadius,
                cy / cLen * spikeRadius,
                cz / cLen * spikeRadius
            )

            val color = faceColorPalette[faceIndex % faceColorPalette.size]

            // 3 triangles: apex with each edge of the icosahedron face
            val faceTriVerts = arrayOf(v0, v1, v2)
            for (i in 0 until 3) {
                val a = apex
                val b = faceTriVerts[i]
                val c = faceTriVerts[(i + 1) % 3]

                // Ensure outward-facing normal
                val ab = floatArrayOf(b[0] - a[0], b[1] - a[1], b[2] - a[2])
                val ac = floatArrayOf(c[0] - a[0], c[1] - a[1], c[2] - a[2])
                val normal = cross(ab, ac)

                // Triangle center
                val tcx = (a[0] + b[0] + c[0]) / 3f
                val tcy = (a[1] + b[1] + c[1]) / 3f
                val tcz = (a[2] + b[2] + c[2]) / 3f

                val dot = normal[0] * tcx + normal[1] * tcy + normal[2] * tcz

                if (dot >= 0) {
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(b.toList())
                    faceVertices.addAll(c.toList())
                } else {
                    faceVertices.addAll(a.toList())
                    faceVertices.addAll(c.toList())
                    faceVertices.addAll(b.toList())
                }
                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
                faceColors.addAll(color.toList())
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

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDisableVertexAttribArray(_positionHandle)
        GLES30.glDisableVertexAttribArray(_colorHandle)
    }
}
