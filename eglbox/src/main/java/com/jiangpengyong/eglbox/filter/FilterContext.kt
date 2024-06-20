package com.jiangpengyong.eglbox.filter

import android.util.Size
import com.jiangpengyong.eglbox.gles.GLCachePool
import com.jiangpengyong.eglbox.gles.GLFrameBuffer
import com.jiangpengyong.eglbox.gles.GLTexture

/**
 * @author jiang peng yong
 * @date 2024/2/11 22:34
 * @email 56002982@qq.com
 * @des 滤镜上下文
 */
class FilterContext {
    private val cachePool = GLCachePool()

    var displaySize: Size = Size(0, 0)

    fun recycle(fbo: GLFrameBuffer) {
        cachePool.recycle(fbo)
    }

    fun recycle(texture: GLTexture) {
        cachePool.recycle(texture)
    }

    fun getTexFBO(width: Int, height: Int): GLFrameBuffer {
        return cachePool.getTexFBO(width, height)
    }

    fun getFBO(): GLFrameBuffer {
        return cachePool.getFBO()
    }

    fun getTexture(width: Int, height: Int): GLTexture {
        return cachePool.getTexture(width, height)
    }
}