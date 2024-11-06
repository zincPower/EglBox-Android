package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import android.util.Log
import com.jiangpengyong.eglbox_core.logger.Logger
import java.nio.ByteBuffer

/**
 * @author: jiang peng yong
 * @date: 2023/9/6 16:24
 * @email: 56002982@qq.com
 * @desc: framebuffer object
 */
class GLFrameBuffer : GLObject {
    var id = 0
        private set

    private var mColorTextures = mutableMapOf<Int, GLTexture>()
    private var mDepthTexture: GLTexture? = null
    private var mDepthRenderBuffer: GLRenderBuffer? = null

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
        Logger.i(
            TAG, "Release GLFrameBuffer. id=${id}, " +
                    "colorTexture=${mColorTextures.map { "{attachment=${it.key} texture=${it.value}}" }.joinToString()}}" +
                    "depthTexture=${mDepthTexture}"
        )

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
            return
        }
        if (attachment == GLES20.GL_DEPTH_ATTACHMENT) {
            bindDepthTexture(texture, attachment, block)
        } else {
            bindColorTexture(texture, attachment, block)
        }
    }

    fun bindTexture(
        colorTexture: GLTexture,
        depthTexture: GLTexture,
        colorAttachment: Int = GLES20.GL_COLOR_ATTACHMENT0,
        depthAttachment: Int = GLES20.GL_DEPTH_ATTACHMENT,
        block: (() -> Unit)? = null
    ) {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【bindTexture】. id=${id}")
            return
        }
        if (!colorTexture.isInit()) {
            Logger.e(TAG, "Texture isn't initialized【bindTexture】. id=${id}, texture=${colorTexture}")
            return
        }
        bindColorTexture(colorTexture, colorAttachment)
        bindDepthTexture(depthTexture, depthAttachment)
        block?.let { use { it.invoke() } }
    }

    fun unbindTexture(attachment: Int = GLES20.GL_COLOR_ATTACHMENT0): GLTexture? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【unbindTexture】. id=${id}")
            return null
        }
        val beforeFBO = EGLBox.getCurrentFBO()
        if (beforeFBO != id) bind()
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
        return if (attachment == GLES20.GL_DEPTH_ATTACHMENT) {
            val texture = mDepthTexture
            mDepthTexture = null
            texture
        } else {
            mColorTextures.remove(attachment)
        }
    }

    fun unbindAllColorTextures(): List<GLTexture>? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【unbindTexture】. id=${id}")
            return null
        }
        val beforeFBO = EGLBox.getCurrentFBO()
        if (beforeFBO != id) bind()
        for (item in mColorTextures) {
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                item.key,
                GLES20.GL_TEXTURE_2D,
                0,
                0
            )
        }
        if (beforeFBO != id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, beforeFBO)
        }
        return mColorTextures.values.toList().also {
            mColorTextures.clear()
        }
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
        // 绑定的多个纹理应该是相同尺寸的，所以获取第一个做尺寸
        val colorTexture = mColorTextures.values.firstOrNull()
        if (colorTexture == null) {
            Logger.e(TAG, "Texture is nullptr. id=$id")
            return
        }
        val beforeFBO = EGLBox.getCurrentFBO()
        if (beforeFBO != id) {
            bind()
        }
        GLES20.glViewport(0, 0, colorTexture.width, colorTexture.height)
        block()
        if (beforeFBO != id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, beforeFBO)
        }
    }

    fun readPixels(attachment: Int = GLES20.GL_COLOR_ATTACHMENT0): ImageInfo? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【readPixels】. id=$id")
            return null
        }
        val colorTexture = mColorTextures[attachment]
        val width = colorTexture?.width ?: 0
        val height = colorTexture?.height ?: 0
        if (colorTexture == null || !colorTexture.isInit() || width <= 0 || height <= 0) {
            Logger.e(TAG, "Texture isn't initialized【readPixels】. id=${id}, texture=${colorTexture}")
            return null
        }
        val pixelSize = width * height * 4
        val pixels = ByteBuffer.allocate(pixelSize)
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

    fun haveColorTexture(): Boolean {
        return mColorTextures.isNotEmpty()
    }

    fun haveDepthTexture(): Boolean {
        return mDepthTexture != null
    }

    private fun bindDepthTexture(
        texture: GLTexture,
        attachment: Int = GLES20.GL_DEPTH_ATTACHMENT,
        block: (() -> Unit)? = null
    ) {
        if (texture.target == Target.EXTERNAL_OES) {
            Log.e(TAG, "Can't use EXTERNAL_OES type texture bind on depth attachment.")
            return
        }
        this.mDepthTexture = texture
        use {
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                attachment,
                GLES20.GL_TEXTURE_2D,
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

    private fun bindColorTexture(
        texture: GLTexture,
        attachment: Int,
        block: (() -> Unit)? = null
    ) {
        this.mColorTextures[attachment] = texture
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

    override fun toString(): String {
        return "[ GLFrameBuffer id=${id}, " +
                "colorTexture=${mColorTextures.map { "{attachment=${it.key} texture=${it.value}}" }.joinToString()}}" +
                "depthTexture=${mDepthTexture} ]"
    }

    companion object {
        private const val TAG = "GLFrameBuffer"
    }
}