package ru.iandreyshev.cglab3.asteroids.ui.openGL.objects

import android.content.res.Resources
import android.opengl.GLES30
import android.opengl.Matrix
import ru.iandreyshev.cglab3.R
import ru.iandreyshev.cglab3.asteroids.domain.AstConst
import ru.iandreyshev.cglab3.asteroids.domain.EnemyState
import ru.iandreyshev.cglab3.asteroids.ui.AstColors
import ru.iandreyshev.core.createProgramGLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val VERTICES = floatArrayOf(
    AstConst.Enemy.RADIUS, 1f,
    2f, -2f,
    -1f, -AstConst.Enemy.RADIUS,
    -1f, -1f,
    -AstConst.Enemy.RADIUS, 0f,
    -AstConst.Enemy.RADIUS, 2f,
    1f, AstConst.Enemy.RADIUS,
    1f, 2f,
    AstConst.Enemy.RADIUS, 1f
)
private const val COORDS_PER_VERTEX = 2
private const val MS_TO_CHANGE_COLOR = 1000f

class EnemyRenderer(res: Resources) {
    private var _program = createProgramGLES30(res, R.raw.enemy_vert, R.raw.enemy_frag)

    private val _modelMatrix = FloatArray(16)
    private val _viewModelMatrix = FloatArray(16)
    private val _mvpMatrix = FloatArray(16)

    private var _positionHandle: Int = 0
    private var _mvpMatrixHandle: Int = 0
    private var _colorHandle: Int = 0

    private var _lastUpdateColorTime = System.currentTimeMillis()
    private val _defaultColor = floatArrayOf(AstColors.enemy.red, AstColors.enemy.green, AstColors.enemy.blue, 1.0f)
    private val _customColor = _defaultColor.copyOf()

    private var _vertexBuffer =
        ByteBuffer.allocateDirect(VERTICES.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(VERTICES)
                position(0)
            }
        }

    fun draw(
        state: EnemyState,
        time: Long,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        val color = getColor(state, time)
        val scale = when (state.level) {
            EnemyState.Level.REGULAR -> AstConst.Enemy.SCALE_REGULAR
            EnemyState.Level.BOSS -> AstConst.Enemy.SCALE_BOSS
        }

        Matrix.setIdentityM(_modelMatrix, 0)
        Matrix.translateM(_modelMatrix, 0, state.position.x, state.position.y, 0f)
        Matrix.rotateM(_modelMatrix, 0, state.rotation, 0f, 0f, 1f)
        Matrix.scaleM(_modelMatrix, 0, scale, scale, 0f)

        Matrix.setIdentityM(_viewModelMatrix, 0)
        Matrix.multiplyMM(_viewModelMatrix, 0, viewMatrix, 0, _modelMatrix, 0)
        Matrix.multiplyMM(_mvpMatrix, 0, projectionMatrix, 0, _viewModelMatrix, 0)

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
            false,                                      // normalized
            COORDS_PER_VERTEX * Float.SIZE_BYTES,       // шаг в буффере
            _vertexBuffer                               // буффер координат
        )

        // get handle to fragment shader's vColor member
        _colorHandle = GLES30.glGetUniformLocation(_program, "vColor")
        // Set color for drawing the triangle
        GLES30.glUniform4fv(_colorHandle, 1, color, 0)

        _mvpMatrixHandle = GLES30.glGetUniformLocation(_program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, _mvpMatrix, 0)
        GLES30.glLineWidth(3f)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, VERTICES.size / COORDS_PER_VERTEX)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(_positionHandle)
    }

    private fun getColor(state: EnemyState, time: Long): FloatArray {
        val timeChangeMS = time - _lastUpdateColorTime
        var colorShift = 0f

        if (timeChangeMS < MS_TO_CHANGE_COLOR) {
            colorShift = timeChangeMS / MS_TO_CHANGE_COLOR
        } else {
            _lastUpdateColorTime = time
        }

        return when (state.level) {
            EnemyState.Level.REGULAR -> _defaultColor
            EnemyState.Level.BOSS -> {
                val radians = Math.toRadians((360 * colorShift).toDouble())
                _customColor[0] = abs(cos(radians).toFloat())
                _customColor[1] = abs(sin(radians).toFloat())
                _customColor[2] = colorShift
                _customColor
            }
        }
    }
}
