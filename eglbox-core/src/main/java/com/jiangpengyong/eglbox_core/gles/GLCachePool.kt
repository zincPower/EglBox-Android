package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.logger.Logger

/**
 * @author jiang peng yong
 * @date 2024/11/6 13:11
 * @email 56002982@qq.com
 * @des GL 缓存对象
 */
class GLCachePool(
    val fboCacheSize: Int = 3,
    val textureCacheSize: Int = 3,
    val depthTextureCacheSize: Int = 2
) {
    private val mFBOCache = ArrayList<GLFrameBuffer>(fboCacheSize)
    private val mTextureCache = ArrayList<GLTexture>(textureCacheSize)
    private val mDepthTextureCache = ArrayList<GLTexture>(depthTextureCacheSize)

    fun getTexFBO(width: Int, height: Int, depthType: DepthType): GLFrameBuffer {
        val fbo = getFBO()
        when (depthType) {
            DepthType.None -> {
                val colorTexture = getColorTexture(width, height)
                fbo.bindTexture(colorTexture, block = {
                    GLES20.glClearColor(0F, 0F, 0F, 1F)
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                })
            }

            DepthType.RenderBuffer -> {

            }

            DepthType.Texture -> {
                val colorTexture = getColorTexture(width, height)
                val depthTexture = getDepthTexture(width, height)
                fbo.bindTexture(colorTexture, depthTexture, block = {
                    GLES20.glClearColor(0F, 0F, 0F, 1F)
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                })
            }
        }
        return fbo
    }

    fun getFBO(): GLFrameBuffer {
        return if (mFBOCache.isEmpty()) {
            GLFrameBuffer().apply { init() }
        } else {
            var fbo = mFBOCache.removeAt(0)
            if (!fbo.isInit()) {
                fbo = GLFrameBuffer().apply { init() }
            }
            fbo
        }
    }

    fun getColorTexture(width: Int, height: Int): GLTexture {
        var texture: GLTexture? = null
        val iterator = mTextureCache.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.width == width && item.height == height) {
                texture = item
                iterator.remove()
                break
            }
        }
        if (texture == null) {
            texture = GLTexture.createColorTexture().apply {
                init()
                setData(width, height)
            }
        }
        return texture
    }

    fun getDepthTexture(width: Int, height: Int): GLTexture {
        var texture: GLTexture? = null
        val iterator = mDepthTextureCache.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.width == width && item.height == height) {
                texture = item
                iterator.remove()
                break
            }
        }
        if (texture == null) {
            texture = GLTexture.createDepthTexture().apply {
                init()
                setData(width, height)
            }
        }
        return texture
    }

    fun recycle(fbo: GLFrameBuffer) {
        if (!fbo.isInit()) return
        fbo.unbindAllColorTextures()?.forEach { recycle(it) }
        fbo.unbindTexture(GLES20.GL_DEPTH_ATTACHMENT)?.let { recycle(it) }
        fbo.unbindTexture(GLES20.GL_STENCIL_ATTACHMENT)?.let { recycle(it) }
        // TODO 可以考虑增加回收 render buffer
        fbo.unbindDepthRenderBuffer()?.release()
        fbo.unbindStencilRenderBuffer()?.release()
        mFBOCache.add(0, fbo)
        if (mFBOCache.size > fboCacheSize) {
            val lastFBO = mFBOCache.removeLastOrNull()
            lastFBO?.release()
        }
    }

    fun recycle(texture: GLTexture) {
        if (!texture.isInit()) return
        // 不回收 oes 纹理
        if (texture.target == Target.EXTERNAL_OES) {
            texture.release()
            return
        }
        val textureCache: ArrayList<GLTexture>
        val cacheSize: Int
        if (texture.format == GLES20.GL_DEPTH_COMPONENT) {    // 深度纹理
            textureCache = mDepthTextureCache
            cacheSize = depthTextureCacheSize
        } else {    // 其余为颜色纹理 TODO 需要细化不同格式？
            textureCache = mTextureCache
            cacheSize = textureCacheSize
        }

        for (item in textureCache) {
            if (item.id == texture.id) {
                Logger.e(TAG, "Texture exist in cache. id=${item.id}")
                return
            }
        }
        textureCache.add(0, texture)
        if (textureCache.size > cacheSize) {
            val lastTexture = textureCache.removeLastOrNull()
            lastTexture?.release()
        }
    }

    fun release() {
        Logger.i(TAG, "GLCachePool release. ${this}")
        for (item in mFBOCache) {
            item.release()
        }
        mFBOCache.clear()
        for (item in mTextureCache) {
            item.release()
        }
        mTextureCache.clear()
        for (item in mDepthTextureCache) {
            item.release()
        }
        mDepthTextureCache.clear()
    }

    override fun toString(): String {
        return "[ GLCachePool mFBOCache=${mFBOCache.size}, " +
                "mTextureCache=${mTextureCache.size}, " +
                "mDepthTextureCache=${mDepthTextureCache.size}, " +
                "fboCacheSize=${fboCacheSize}, " +
                "textureCacheSize=${textureCacheSize}, " +
                "depthTextureCacheSize=${depthTextureCacheSize} ]"
    }

    companion object {
        const val TAG = "GLCachePool"
    }
}