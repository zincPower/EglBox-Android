package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import android.opengl.GLES30
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
    private var mStencilTexture: GLTexture? = null

    private var mDepthRenderBuffer: GLRenderBuffer? = null
    private var mStencilRenderBuffer: GLRenderBuffer? = null
    private var mDepthAndStencilRenderBuffer: GLRenderBuffer? = null

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

        unbindAllColorTextures()?.forEach { it.release() }
        unbindTexture(GLES20.GL_DEPTH_ATTACHMENT)?.release()
        unbindTexture(GLES20.GL_STENCIL_ATTACHMENT)?.release()
        unbindDepthRenderBuffer()?.release()
        unbindStencilRenderBuffer()?.release()

        val currentFBO = EglBox.getCurrentFBO()
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
        when (attachment) {
            GLES20.GL_DEPTH_ATTACHMENT -> {
                if (checkDepthAttachmentExist()) {
                    Logger.e(TAG, "GLFrameBuffer has bound depth on attachment. id=${id} depthTexture=${mDepthTexture} depthRenderBuffer=${mDepthRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer} ")
                    return
                }
                bindDepthTexture(texture, block)
            }

            GLES20.GL_STENCIL_ATTACHMENT -> {
                if (checkStencilAttachmentExist()) {
                    Logger.e(TAG, "GLFrameBuffer has bound stencil on attachment. id=${id} stencilTexture=${mStencilTexture} stencilRenderBuffer=${mStencilRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer}")
                    return
                }
                bindStencilTexture(texture, block)
            }

            else -> {
                if (checkColorAttachmentExist(attachment)) {
                    Log.e(TAG, "GLFrameBuffer has bound color texture on attachment. id=${id} attachment=${attachment.toString(16)}")
                    return
                }
                bindColorTexture(texture, attachment, block)
            }
        }
    }

    fun bindRenderBuffer(renderBuffer: GLRenderBuffer, block: (() -> Unit)? = null) {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【bindRenderBuffer】. id=${id}")
            return
        }
        if (!renderBuffer.isInit()) {
            Logger.e(TAG, "Render buffer isn't initialized【bindRenderBuffer】. id=${id} renderBuffer=${renderBuffer}")
            return
        }
        when (renderBuffer.internalFormat) {
            GLES20.GL_DEPTH_COMPONENT16, GLES30.GL_DEPTH_COMPONENT24, GLES30.GL_DEPTH_COMPONENT32F -> {
                if (checkDepthAttachmentExist()) {
                    Logger.e(TAG, "GLFrameBuffer has bound depth on attachment. id=${id} depthTexture=${mDepthTexture} depthRenderBuffer=${mDepthRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer} ")
                    return
                }
                bindDepthRenderBuffer(renderBuffer, block)
            }

            GLES20.GL_STENCIL_INDEX8 -> {
                if (checkStencilAttachmentExist()) {
                    Logger.e(TAG, "GLFrameBuffer has bound stencil on attachment. id=${id} stencilTexture=${mStencilTexture} stencilRenderBuffer=${mStencilRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer}")
                    return
                }
                bindStencilRenderBuffer(renderBuffer, block)
            }

            GLES30.GL_DEPTH24_STENCIL8, GLES30.GL_DEPTH32F_STENCIL8 -> {
                if (checkDepthAttachmentExist()) {
                    Logger.e(TAG, "GLFrameBuffer has bound depth on attachment. id=${id} depthTexture=${mDepthTexture} depthRenderBuffer=${mDepthRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer} ")
                    return
                }
                if (checkStencilAttachmentExist()) {
                    Logger.e(TAG, "GLFrameBuffer has bound stencil on attachment. id=${id} stencilTexture=${mStencilTexture} stencilRenderBuffer=${mStencilRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer}")
                    return
                }
                bindDepthAndStencilRenderBuffer(renderBuffer, block)
            }
        }
    }

    fun bindTexture(
        colorTexture: GLTexture,
        depthTexture: GLTexture,
        colorAttachment: Int = GLES20.GL_COLOR_ATTACHMENT0,
        block: (() -> Unit)? = null
    ) {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【bindTexture】. id=${id}")
            return
        }
        if (!colorTexture.isInit()) {
            Logger.e(TAG, "Color texture isn't initialized【bindTexture】. id=${id}, texture=${colorTexture}")
            return
        }
        if (!depthTexture.isInit()) {
            Logger.e(TAG, "Depth texture isn't initialized【bindTexture】. id=${id}, texture=${depthTexture}")
            return
        }
        if (checkColorAttachmentExist(colorAttachment)) {
            Log.e(TAG, "GLFrameBuffer has bound color texture on attachment. id=${id} attachment=${colorAttachment.toString(16)}")
            return
        }
        if (checkDepthAttachmentExist()) {
            Logger.e(TAG, "GLFrameBuffer has bound depth texture on attachment. id=${id} depthTexture=${mDepthTexture} depthRenderBuffer=${mDepthAndStencilRenderBuffer}")
            return
        }
        bindColorTexture(colorTexture, colorAttachment)
        bindDepthTexture(depthTexture, block)
    }

    fun bindTexture(
        colorTexture: GLTexture,
        depthAndStencilRenderBuffer: GLRenderBuffer,
        colorAttachment: Int = GLES20.GL_COLOR_ATTACHMENT0,
        block: (() -> Unit)? = null
    ) {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【bindTexture】. id=${id}")
            return
        }
        if (!colorTexture.isInit()) {
            Logger.e(TAG, "Color texture isn't initialized【bindTexture】. id=${id}, color texture=${colorTexture}")
            return
        }
        if (!depthAndStencilRenderBuffer.isInit()) {
            Logger.e(TAG, "Depth and stencil render buffer isn't initialized【bindTexture】. id=${id}, depthAndStencilRenderBuffer=${depthAndStencilRenderBuffer}")
            return
        }
        if (depthAndStencilRenderBuffer.internalFormat != GLES30.GL_DEPTH24_STENCIL8 && depthAndStencilRenderBuffer.internalFormat != GLES30.GL_DEPTH32F_STENCIL8) {
            Logger.e(TAG, "RenderBuffer internal format is invalid for depth and stencil buffer. id=${id}, depthAndStencilRenderBuffer=${depthAndStencilRenderBuffer}")
            return
        }
        if (checkColorAttachmentExist(colorAttachment)) {
            Log.e(TAG, "GLFrameBuffer has bound color texture on attachment. id=${id} attachment=${colorAttachment.toString(16)}")
            return
        }
        if (checkDepthAttachmentExist()) {
            Logger.e(TAG, "GLFrameBuffer has bound depth on attachment. id=${id} depthTexture=${mDepthTexture} depthRenderBuffer=${mDepthRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer} ")
            return
        }
        if (checkStencilAttachmentExist()) {
            Logger.e(TAG, "GLFrameBuffer has bound stencil on attachment. id=${id} stencilTexture=${mStencilTexture} stencilRenderBuffer=${mStencilRenderBuffer} depthAndStencilRenderBuffer=${mDepthAndStencilRenderBuffer}")
            return
        }
        mDepthAndStencilRenderBuffer = depthAndStencilRenderBuffer
        bindColorTexture(colorTexture, colorAttachment)
        use {
            depthAndStencilRenderBuffer.bind()
            block?.invoke()
        }
    }

    fun unbindTexture(attachment: Int = GLES20.GL_COLOR_ATTACHMENT0): GLTexture? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【unbindTexture】. id=${id}")
            return null
        }
        val beforeFBO = EglBox.getCurrentFBO()
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
        return when (attachment) {
            GLES20.GL_DEPTH_ATTACHMENT -> {
                val texture = mDepthTexture
                mDepthTexture = null
                texture
            }

            GLES20.GL_STENCIL_ATTACHMENT -> {
                val texture = mStencilTexture
                mStencilTexture = null
                texture
            }

            else -> {
                mColorTextures.remove(attachment)
            }
        }
    }

    fun unbindAllColorTextures(): List<GLTexture>? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【unbindAllColorTextures】. id=${id}")
            return null
        }
        val beforeFBO = EglBox.getCurrentFBO()
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

    fun unbindDepthRenderBuffer(): GLRenderBuffer? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【unbindDepthRenderBuffer】. id=${id}")
            return null
        }
        val beforeFBO = EglBox.getCurrentFBO()
        if (beforeFBO != id) bind()
        // 如果 render buffer 是 GL_DEPTH24_STENCIL8 or GL_DEPTH32F_STENCIL8 类型，则会同时解绑深度和模板
        val renderBuffer = mDepthRenderBuffer ?: mDepthAndStencilRenderBuffer
        renderBuffer?.unbind()
        if (beforeFBO != id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, beforeFBO)
        }
        mDepthRenderBuffer = null
        mDepthAndStencilRenderBuffer = null
        return renderBuffer
    }

    fun unbindStencilRenderBuffer(): GLRenderBuffer? {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【unbindDepthRenderBuffer】. id=${id}")
            return null
        }
        val beforeFBO = EglBox.getCurrentFBO()
        if (beforeFBO != id) bind()
        // 如果 render buffer 是 GL_DEPTH24_STENCIL8 or GL_DEPTH32F_STENCIL8 类型，则会同时解绑深度和模板
        val renderBuffer = mStencilRenderBuffer ?: mDepthAndStencilRenderBuffer
        renderBuffer?.unbind()
        if (beforeFBO != id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, beforeFBO)
        }
        mStencilRenderBuffer = null
        mDepthAndStencilRenderBuffer = null
        return renderBuffer
    }

    fun bind() {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【bind】. id=${id}")
            return
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, id)
    }

    fun unbind() {
        val currentFBO = EglBox.getCurrentFBO()
        if (currentFBO == id) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }
    }

    fun use(block: () -> Unit) {
        if (!isInit()) {
            Logger.e(TAG, "GLFrameBuffer isn't initialized【use(block)】. id=${id}")
            return
        }
        val beforeFBO = EglBox.getCurrentFBO()
        if (beforeFBO != id) {
            bind()
        }
        // 绑定的多个纹理应该是相同尺寸的，所以获取第一个做尺寸
        val texture = mColorTextures.values.firstOrNull() ?: mDepthTexture ?: mStencilTexture
        if (texture != null && texture.width != 0 && texture.height != 0) {
            GLES20.glViewport(0, 0, texture.width, texture.height)
        }
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

    private fun checkColorAttachmentExist(attachment: Int): Boolean = this.mColorTextures[attachment] != null
    private fun checkDepthAttachmentExist(): Boolean = this.mDepthTexture != null || this.mDepthAndStencilRenderBuffer != null || this.mDepthRenderBuffer != null
    private fun checkStencilAttachmentExist(): Boolean = this.mStencilTexture != null || this.mDepthAndStencilRenderBuffer != null || this.mStencilRenderBuffer != null

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
                Logger.e(TAG, "Frame buffer bind texture failure. status=0x${frameBufferStatus.toString(16)}")
            }
            block?.invoke()
        }
    }

    private fun bindDepthTexture(
        texture: GLTexture,
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
                GLES20.GL_DEPTH_ATTACHMENT,
                texture.target.value,
                texture.id,
                0
            )
            val frameBufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (frameBufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Logger.e(TAG, "Frame buffer bind depth texture failure. status=0x${frameBufferStatus.toString(16)}")
            }
            block?.invoke()
        }
    }

    private fun bindStencilTexture(
        texture: GLTexture,
        block: (() -> Unit)? = null
    ) {
        if (texture.target == Target.EXTERNAL_OES) {
            Log.e(TAG, "Can't use EXTERNAL_OES type texture bind on stencil attachment.")
            return
        }
        this.mStencilTexture = texture
        use {
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_STENCIL_ATTACHMENT,
                texture.target.value,
                texture.id,
                0
            )
            val frameBufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (frameBufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Logger.e(TAG, "Frame buffer bind stencil texture failure. status=$frameBufferStatus")
            }
            block?.invoke()
        }
    }

    private fun bindDepthRenderBuffer(renderBuffer: GLRenderBuffer, block: (() -> Unit)?) {
        if (renderBuffer.internalFormat != GLES20.GL_DEPTH_COMPONENT16 && renderBuffer.internalFormat != GLES30.GL_DEPTH_COMPONENT24 && renderBuffer.internalFormat != GLES30.GL_DEPTH_COMPONENT32F) {
            Log.e(TAG, "Only RenderBuffer internal format is GL_DEPTH_COMPONENT16 or GL_DEPTH_COMPONENT24 or GL_DEPTH_COMPONENT32F can be bound to Depth attachment.")
            return
        }
        this.mDepthRenderBuffer = renderBuffer
        use {
            renderBuffer.bind()
            val frameBufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (frameBufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Logger.e(TAG, "Frame buffer bind depth render buffer failure. status=$frameBufferStatus")
            }
            block?.invoke()
        }
    }

    private fun bindStencilRenderBuffer(renderBuffer: GLRenderBuffer, block: (() -> Unit)?) {
        if (renderBuffer.internalFormat != GLES20.GL_STENCIL_INDEX8) {
            Log.e(TAG, "Only RenderBuffer internal format is GL_STENCIL_INDEX or GL_STENCIL_INDEX8 can bound on Stencil attachment.")
            return
        }
        this.mStencilRenderBuffer = renderBuffer
        use {
            renderBuffer.bind()
            val frameBufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (frameBufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Logger.e(TAG, "Frame buffer bind stencil render buffer failure. status=0x${frameBufferStatus.toString(16)}")
            }
            block?.invoke()
        }
    }

    private fun bindDepthAndStencilRenderBuffer(renderBuffer: GLRenderBuffer, block: (() -> Unit)?) {
        if (renderBuffer.internalFormat != GLES30.GL_DEPTH24_STENCIL8 && renderBuffer.internalFormat != GLES30.GL_DEPTH32F_STENCIL8) {
            Log.e(TAG, "Only RenderBuffer internal format is GL_DEPTH24_STENCIL8 or GL_DEPTH32F_STENCIL8 can bound on Depth and Stencil attachment.")
            return
        }
        this.mDepthAndStencilRenderBuffer = renderBuffer
        use {
            renderBuffer.bind()
            val frameBufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (frameBufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Logger.e(TAG, "Frame buffer bind depth and stencil render buffer failure. status=${frameBufferStatus.toString(16).uppercase()}")
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