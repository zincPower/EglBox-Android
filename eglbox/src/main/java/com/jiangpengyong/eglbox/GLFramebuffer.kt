package com.jiangpengyong.eglbox

import android.opengl.GLES20
import com.jiangpengyong.eglbox.logger.Logger


/**
 * @author: jiang peng yong
 * @date: 2023/9/6 16:24
 * @email: 56002982@qq.com
 * @desc: framebuffer object
 */
class GLFramebuffer {

    companion object {
        private const val NOT_INIT = -1
    }

    var id = NOT_INIT
        private set

    fun isInit(): Boolean = (id != NOT_INIT)

    fun create() {
        val array = IntArray(1)
        GLES20.glGenFramebuffers(1, array, 0)
        id = array[0]
        Logger.i("Create framebuffer. [$id]")
    }

    fun bindTexture(
        texture: GLTexture,
        attachment: Int = GLES20.GL_COLOR_ATTACHMENT0,
    ): GLFramebuffer {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, id)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            attachment,
            texture.target.value,
            texture.id,
            0
        )
        return this
    }

//    fun bindRenderBuffer(
//        texture: GLTexture,
//        internalFormat: Int = GLES20.GL_DEPTH_COMPONENT,
//    ) {
//        GLES20.glBindFramebuffer(GLES20.GL_RENDERBUFFER, id)
//        GLES20.glRenderbufferStorage(
//            GLES20.GL_RENDERBUFFER,
//            internalFormat,
//            texture.width,
//            texture.height,
//        )
//    }

    fun use(block: () -> Unit) {
        bind()
        block()
        unbind()
    }

    fun bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, id)
    }

    fun unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun release() {
        val currentBindFBO = intArrayOf(1)
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, currentBindFBO, 0)
        if (currentBindFBO[0] == id) {
            unbind()
        }
        GLES20.glDeleteFramebuffers(1, intArrayOf(id), 0)
        Logger.i("Release framebuffer. [$id]")

        id = NOT_INIT
    }

}