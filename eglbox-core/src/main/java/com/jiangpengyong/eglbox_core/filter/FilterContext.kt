package com.jiangpengyong.eglbox_core.filter

import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.egl.EGL
import com.jiangpengyong.eglbox_core.egl.EglSurface
import com.jiangpengyong.eglbox_core.egl.PBufferSurface
import com.jiangpengyong.eglbox_core.egl.WindowSurface
import com.jiangpengyong.eglbox_core.gles.GLCachePool
import com.jiangpengyong.eglbox_core.gles.GLFrameBuffer
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.program.Texture2DProgram

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
    var displaySize = Size(0, 0)

    // 单次渲染数据，再一次渲染后会清空
    val renderData = HashMap<String, Any>()

    // 滤镜链上下文数据，生命周期和滤镜链一样
    val contextData = HashMap<String, Any>()

    // 2D 图像渲染
    val texture2DProgram = Texture2DProgram(Target.TEXTURE_2D)

    // 设备角度
    var deviceOrientation = Orientation.Orientation_0

    /**
     * EGL 环境
     * 可以创建通过 [EGL.createWindow] 创建上屏的 [WindowSurface]
     * 可以创建通过 [EGL.createPBuffer] 创建上屏的 [PBufferSurface]
     */
    var egl: EGL? = null
        private set

    var surface: EglSurface? = null
        private set

    fun init(egl: EGL, surface: EglSurface, listener: MessageListener) {
        this.egl = egl
        this.surface = surface
        mListener = listener
        texture2DProgram.init()
    }

    fun release() {
        mCachePool.release()
        mListener = null
        displaySize = Size(0, 0)
        renderData.clear()
        contextData.clear()
        texture2DProgram.release()
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