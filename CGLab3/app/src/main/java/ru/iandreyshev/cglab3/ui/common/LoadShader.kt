package ru.iandreyshev.cglab3.ui.common

import android.opengl.GLES30

fun loadShaderGLES30(type: Int, shaderCode: String): Int {
    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    return GLES30.glCreateShader(type).also { shader ->
        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
    }
}
