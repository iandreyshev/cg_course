package ru.iandreyshev.cglab3.common

import android.content.res.Resources
import android.opengl.GLES30
import androidx.annotation.RawRes

fun Resources.loadShader(type: Int, @RawRes resId: Int): Int =
    this.openRawResource(resId).use { inputStream ->
        val shaderText = inputStream.bufferedReader().readText()
        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        GLES30.glCreateShader(type).also { shader ->
            // add the source code to the shader and compile it
            GLES30.glShaderSource(shader, shaderText)
            GLES30.glCompileShader(shader)
        }
    }
