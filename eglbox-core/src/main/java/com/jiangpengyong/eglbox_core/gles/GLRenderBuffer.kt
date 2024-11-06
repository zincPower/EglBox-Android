package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import android.util.Size
import com.jiangpengyong.eglbox_core.logger.Logger

/**
 * @author jiang peng yong
 * @date 2024/11/4 12:43
 * @email 56002982@qq.com
 * @des 渲染
 */
class GLRenderBuffer : GLObject {
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
        Logger.i(TAG, "Create framebuffer success. id=$id")
    }

    override fun release() {
        if (!isInit()) return
        Logger.i(TAG, "Release GLRenderBuffer. id=${id}")
        val array = IntArray(1)
        GLES20.glDeleteRenderbuffers(1, array, 0)
    }

    fun setData(size: Size) {
        if (!isInit()) {
            Logger.e(TAG, "GLRenderBuffer isn't initialized【readPixels】. id=$id")
            return
        }
        GLES20.glRenderbufferStorage(
            GLES20.GL_RENDERBUFFER,
            GLES20.GL_DEPTH_COMPONENT16,
            size.width,
            size.height
        )
    }

    fun bind() {
        GLES20.glFramebufferRenderbuffer(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_RENDERBUFFER,
            id
        )
    }

    fun unbind() {
        val currentDepthInfo = EGLBox.getCurrentDepthInfo()
        if (currentDepthInfo.depthType == DepthType.RenderBuffer && currentDepthInfo.id == id) {
            GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER,
                0
            )
        }
    }

    companion object {
        const val TAG = "GLRenderBuffer"
    }
}