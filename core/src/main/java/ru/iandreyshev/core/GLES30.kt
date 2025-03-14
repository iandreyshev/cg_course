package ru.iandreyshev.core

import android.content.res.Resources
import android.opengl.GLES30
import androidx.annotation.RawRes

fun createProgramGLES30(
    resources: Resources,
    @RawRes vertRes: Int,
    @RawRes fragRes: Int
) = GLES30.glCreateProgram().also {
    val vertShader = resources.loadShader(GLES30.GL_VERTEX_SHADER, vertRes)
    GLES30.glAttachShader(it, vertShader)

    // add the fragment shader to program
    val fragShader = resources.loadShader(GLES30.GL_FRAGMENT_SHADER, fragRes)
    GLES30.glAttachShader(it, fragShader)

    // creates OpenGL ES program executables
    GLES30.glLinkProgram(it)
}
