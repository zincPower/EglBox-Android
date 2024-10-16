package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.logger.Logger

class GLCachePool(
    val fboCacheSize: Int = 3,
    val textureCacheSize: Int = 3
) {
    private val TAG = "GLCachePool"

    private val mFBOCache = ArrayList<GLFrameBuffer>(fboCacheSize)
    private val mTextureCache = ArrayList<GLTexture>(textureCacheSize)

    fun getTexFBO(width: Int, height: Int): GLFrameBuffer {
        val fbo = getFBO()
        val texture = getTexture(width, height)
        fbo.bindTexture(texture, block = {
            GLES20.glClearColor(0F, 0F, 0F, 1F)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        })
        return fbo
    }

    fun getFBO(): GLFrameBuffer {
        return if (mFBOCache.isEmpty()) {
            GLFrameBuffer().apply { init() }
        } else {
            var fbo = mFBOCache.removeFirst()
            if (!fbo.isInit()) {
                fbo = GLFrameBuffer().apply { init() }
            }
            fbo
        }
    }

    fun getTexture(width: Int, height: Int): GLTexture {
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
            texture = GLTexture().apply {
                init()
                setData(width, height)
            }
        }
        return texture
    }

    fun recycle(fbo: GLFrameBuffer) {
        if (!fbo.isInit()) return
        if (fbo.haveTexture()) {
            val texture = fbo.unbindTexture()
            texture?.let { recycle(it) }
        }
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
        for (item in mTextureCache) {
            if (item.id == texture.id) {
                Logger.e(TAG, "Texture exist in cache. id=${item.id}")
                return;
            }
        }
        mTextureCache.add(0, texture)
        if (mTextureCache.size > textureCacheSize) {
            val lastTexture = mTextureCache.removeLastOrNull()
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
    }

    override fun toString(): String {
        return "[ GLCachePool mFBOCache=${mFBOCache.size}, " +
                "mTextureCache=${mTextureCache.size} " +
                "fboCacheSize=${fboCacheSize}, " +
                "textureCacheSize=${textureCacheSize} ]"
    }
}