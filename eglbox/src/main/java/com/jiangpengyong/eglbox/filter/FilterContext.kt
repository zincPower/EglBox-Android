package com.jiangpengyong.eglbox.filter

import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox.box.FilterChainListener
import com.jiangpengyong.eglbox.box.FilterChainListenerImpl
import com.jiangpengyong.eglbox.box.MessageListener
import com.jiangpengyong.eglbox.box.RenderType
import com.jiangpengyong.eglbox.egl.EGL
import com.jiangpengyong.eglbox.egl.PBufferSurface
import com.jiangpengyong.eglbox.egl.WindowSurface
import com.jiangpengyong.eglbox.gles.GLCachePool
import com.jiangpengyong.eglbox.gles.GLFrameBuffer
import com.jiangpengyong.eglbox.gles.GLTexture

/**
 * @author jiang peng yong
 * @date 2024/2/11 22:34
 * @email 56002982@qq.com
 * @des 滤镜上下文
 */
class FilterContext(val renderType: RenderType) {
    private val mCachePool = GLCachePool()
    private var mListener: MessageListener? = null

    /**
     * 展示的尺寸
     * 如果是上屏环境 [RenderType.OnScreen] ，则为最后上屏的控件尺寸
     * 如果是离屏环境 [RenderType.OffScreen] ，则为 PBuffer 的创建尺寸
     */
    var displaySize: Size = Size(0, 0)

    // 单次渲染数据，再一次渲染后会清空
    val renderData = HashMap<String, Any>()

    // 滤镜链上下文数据，生命周期和滤镜链一样
    val contextData = HashMap<String, Any>()

    /**
     * EGL 环境
     * 可以创建通过 [EGL.createWindow] 创建上屏的 [WindowSurface]
     * 可以创建通过 [EGL.createPBuffer] 创建上屏的 [PBufferSurface]
     */
    var egl: EGL? = null
        private set

    fun init(egl: EGL, listener: MessageListener) {
        this.egl = egl
        mListener = listener
    }

    fun release() {
        mCachePool.release()
        mListener = null
        displaySize = Size(0, 0)
        renderData.clear()
        contextData.clear()
        this.egl = null
    }

    fun recycle(fbo: GLFrameBuffer) {
        mCachePool.recycle(fbo)
    }

    fun recycle(texture: GLTexture) {
        mCachePool.recycle(texture)
    }

    fun getTexFBO(width: Int, height: Int): GLFrameBuffer {
        return mCachePool.getTexFBO(width, height)
    }

    fun getFBO(): GLFrameBuffer {
        return mCachePool.getFBO()
    }

    fun getTexture(width: Int, height: Int): GLTexture {
        return mCachePool.getTexture(width, height)
    }

    fun sendMessage(filterId: String, message: Message) {
        mListener?.onReceiveMessage(filterId, message)
    }
}