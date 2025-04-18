package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Size
import com.jiangpengyong.eglbox_core.logger.Logger

/**
 * @author jiang peng yong
 * @date 2024/11/4 12:43
 * @email 56002982@qq.com
 * @des 渲染
 */
class GLRenderBuffer(
    val internalFormat: Int = GLES20.GL_DEPTH_COMPONENT16,
) : GLObject {
    var id = 0
        private set

    override fun isInit(): Boolean = (id != 0)

    override fun init() {
        if (isInit()) {
            Logger.e(TAG, "GLRenderBuffer has been initialized. id=${id}")
            return
        }
        val array = IntArray(1)
        GLES20.glGenRenderbuffers(1, array, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, array[0])
        id = array[0]
        Logger.i(TAG, "Create GLRenderBuffer success. id=$id")
    }

    override fun release() {
        if (!isInit()) return
        Logger.i(TAG, "Release GLRenderBuffer. id=${id}")
        val array = intArrayOf(id)
        GLES20.glDeleteRenderbuffers(1, array, 0)
        id = 0
    }

    fun setSize(size: Size) {
        if (!isInit()) {
            Logger.e(TAG, "GLRenderBuffer isn't initialized【setSize】. id=$id")
            return
        }
        GLES20.glRenderbufferStorage(
            GLES20.GL_RENDERBUFFER,
            internalFormat,
            size.width,
            size.height
        )
    }

    fun bind() {
        if (!isInit()) {
            Logger.e(TAG, "GLRenderBuffer isn't initialized【bind】. id=$id")
            return
        }
        when (internalFormat) {
            GLES20.GL_DEPTH_COMPONENT16, GLES30.GL_DEPTH_COMPONENT24, GLES30.GL_DEPTH_COMPONENT32F -> {
                GLES20.glFramebufferRenderbuffer(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER,
                    id
                )
            }

            GLES20.GL_STENCIL_INDEX, GLES20.GL_STENCIL_INDEX8 -> {
                GLES20.glFramebufferRenderbuffer(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_STENCIL_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER,
                    id
                )
            }

            GLES30.GL_DEPTH24_STENCIL8, GLES30.GL_DEPTH32F_STENCIL8 -> {
                GLES20.glFramebufferRenderbuffer(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER,
                    id
                )
                GLES20.glFramebufferRenderbuffer(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_STENCIL_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER,
                    id
                )
            }
        }
    }

    fun unbind() {
        if (!isInit()) {
            Logger.e(TAG, "GLRenderBuffer isn't initialized【unbind】. id=$id")
            return
        }
        val currentDepthInfo = EglBox.getCurrentDepthInfo()
        if (currentDepthInfo.id == id) return
        if (internalFormat == GLES20.GL_DEPTH_COMPONENT16
            || internalFormat == GLES30.GL_DEPTH_COMPONENT24
            || internalFormat == GLES30.GL_DEPTH_COMPONENT32F
            || internalFormat == GLES30.GL_DEPTH24_STENCIL8
            || internalFormat == GLES30.GL_DEPTH32F_STENCIL8
        ) {
            GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER,
                0
            )
        }
        if (internalFormat == GLES20.GL_STENCIL_INDEX8
            || internalFormat == GLES30.GL_DEPTH24_STENCIL8
            || internalFormat == GLES30.GL_DEPTH32F_STENCIL8
        ) {
            GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_STENCIL_ATTACHMENT,
                GLES20.GL_RENDERBUFFER,
                0
            )
        }
    }

    override fun toString(): String {
        return "[ GLRenderBuffer id=${id} internalFormat=${internalFormat.toString(16)} ]"
    }

    companion object {
        const val TAG = "GLRenderBuffer"
    }
}