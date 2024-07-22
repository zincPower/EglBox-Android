package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.logger.Logger
import java.nio.IntBuffer


/**
 * @author: jiang peng yong
 * @date: 2023/9/6 16:24
 * @email: 56002982@qq.com
 * @desc: framebuffer object
 */
class GLFrameBuffer : GLObject {
    var id = 0
        private set

    private var mTexture: GLTexture? = null

    override fun init() {
        if (isInit()) {
            Logger.e(TAG, "GLFrameBuffer has been initialized. id=${id}")
            return
        }
        val array = IntArray(1)
        GLES20.glGenFramebuffers(1, array, 0)
        id = array[0]
        Logger.i(TAG, "Create framebuffer success. id=$id")
    }

    override fun release() {
        if (!isInit()) return
        Logger.i(TAG, "Release GLFrameBuffer. id=${id}, texture=${mTexture}")

        val texture = unbindTexture()
        texture?.release()

        val currentFBO = EGLBox.getCurrentFBO()
        if (currentFBO == id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }
        GLES20.glDeleteFramebuffers(1, intArrayOf(id), 0)
        id = 0
    }

    override fun isInit(): Boolean = (id != 0)

    fun bindTexture(
        texture: GLTexture,
        attachment: Int = GLES20.GL_COLOR_ATTACHMENT0,
        block: (() -> Unit)? = null
    ) {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【bindTexture】. id=${id}")
            return
        }
        if (!texture.isInit()) {
            Logger.e(TAG, "Texture isn't initialized【bindTexture】. id=${id}, texture=${texture}")
            return;
        }
        mTexture = texture
        use {
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                attachment,
                texture.target.value,
                texture.id,
                0
            )
            val frameBufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (frameBufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Logger.e(TAG, "Frame buffer bind texture failure. status=$frameBufferStatus")
            }
            block?.invoke()
        }
    }

    fun unbindTexture(attachment: Int = GLES20.GL_COLOR_ATTACHMENT0): GLTexture? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【unbindTexture】. id=${id}")
            return null
        }
        val beforeFBO = EGLBox.getCurrentFBO()
        if (beforeFBO != id) {
            bind()
        }
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            attachment,
            GLES20.GL_TEXTURE_2D,
            0,
            0
        )
        if (beforeFBO != id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, beforeFBO)
        }
        val tempTexture = mTexture
        mTexture = null
        return tempTexture
    }

    fun bind() {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【bind】. id=${id}")
            return
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, id)
    }

    fun unbind() {
        val currentFBO = EGLBox.getCurrentFBO()
        if (currentFBO == id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }
    }

    fun use(block: () -> Unit) {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【use(block)】. id=${id}")
            return
        }
        val tex = mTexture
        if (tex == null) {
            Logger.e(TAG, "Texture is nullptr. id=$id")
            return
        }
        val beforeFBO = EGLBox.getCurrentFBO()
        if (beforeFBO != id) {
            bind()
        }
        GLES20.glViewport(0, 0, tex.width, tex.height)
        block()
        if (beforeFBO != id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, beforeFBO)
        }
    }

    fun readPixels(): ImageInfo? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【readPixels】. id=$id")
            return null
        }
        val width = mTexture?.width ?: 0
        val height = mTexture?.height ?: 0
        if (mTexture == null || mTexture?.isInit() != true || width <= 0 || height <= 0) {
            Logger.e(TAG, "Texture isn't initialized【readPixels】. id=${id}, texture=${mTexture}")
            return null
        }
        val pixelSize = width * height * 4
        val pixels = IntBuffer.allocate(pixelSize)
        pixels.position(0)
        use {
            GLES20.glReadPixels(
                0, 0,
                width, height,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                pixels
            )
        }
        return ImageInfo(width, height, pixels)
    }

    fun haveTexture(): Boolean {
        return mTexture != null
    }

    override fun toString(): String {
        return "[ GLFrameBuffer id=${id}, texture=${mTexture} ] "
    }

    companion object{
        private const val TAG = "GLFrameBuffer"
    }
}