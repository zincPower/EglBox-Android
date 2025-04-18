package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.logger.Logger

object EglBox {
    private const val TAG = "EglBox"

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

    /**
     * 查询当前深度附件
     */
    fun getCurrentDepthInfo(): DepthInfo {
        val currentDepthType = IntArray(1)
        GLES20.glGetFramebufferAttachmentParameteriv(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE,
            currentDepthType,
            0
        )
        return when (DepthType.parse(currentDepthType[0])) {
            DepthType.None -> {
                DepthInfo(DepthType.None, 0)
            }

            DepthType.RenderBuffer -> {
                val currentRenderBuffer = IntArray(1)
                GLES20.glGetFramebufferAttachmentParameteriv(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME,
                    currentRenderBuffer,
                    0
                )
                DepthInfo(DepthType.RenderBuffer, currentRenderBuffer[0])
            }

            DepthType.Texture -> {
                val currentTexture = IntArray(1)
                GLES20.glGetFramebufferAttachmentParameteriv(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME,
                    currentTexture,
                    0
                )
                DepthInfo(DepthType.Texture, currentTexture[0])
            }
        }
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

enum class DepthType(val value: Int) {
    None(GLES20.GL_NONE),
    RenderBuffer(GLES20.GL_RENDERBUFFER),
    Texture(GLES20.GL_TEXTURE);

    companion object {
        fun parse(value: Int): DepthType {
            for (item in values()) {
                if (item.value == value) return item
            }
            return None
        }
    }
}

/**
 * @author jiang peng yong
 * @date 2024/11/4 13:10
 * @email 56002982@qq.com
 * @des 深度信息
 */
data class DepthInfo(
    val depthType: DepthType,
    val id: Int,
)