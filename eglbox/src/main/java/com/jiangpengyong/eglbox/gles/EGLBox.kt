package com.jiangpengyong.eglbox.gles

import android.opengl.GLES20
import com.jiangpengyong.eglbox.logger.Logger

object EGLBox {
    private val TAG = "EGLBox"

    fun getCurrentProgram(): Int {
        val currentProgram = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, currentProgram, 0)
        return currentProgram[0]
    }

    fun getCurrentFBO(): Int {
        val currentFBO = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, currentFBO, 0)
        return currentFBO[0]
    }

    fun getCurrentTexture(): Int {
        val currentTexture = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, currentTexture, 0)
        return currentTexture[0]
    }

    fun checkError(optionName: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Logger.e(TAG, "$optionName: OpenGL Error 0x${Integer.toHexString(error)}")
        }
    }

    fun getMaxTextureSize(): Int {
        val maxTextureSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)
        return maxTextureSize[0]
    }
}