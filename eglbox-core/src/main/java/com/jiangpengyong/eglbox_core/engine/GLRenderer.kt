package com.jiangpengyong.eglbox_core.engine

import android.os.Handler
import com.jiangpengyong.eglbox_core.GLThread
import com.jiangpengyong.eglbox_core.egl.EGL
import com.jiangpengyong.eglbox_core.egl.EglSurface
import com.jiangpengyong.eglbox_core.engine.RenderType.OffScreen
import com.jiangpengyong.eglbox_core.engine.RenderType.OnScreen
import com.jiangpengyong.eglbox_core.filter.FilterChain

/**
 * @author: jiang peng yong
 * @date: 2024/7/16 12:51
 * @email: 56002982@qq.com
 * @desc: GL渲染器
 */
interface GLRenderer {
    @GLThread
    fun onEGLCreated(egl: EGL, surface: EglSurface, glHandler: GLHandler)

    @GLThread
    fun onSurfaceSizeChanged(surface: EglSurface)

    @GLThread
    fun onDrawFrame()

    @GLThread
    fun onEGLDestroy()
}

/**
 * @author: jiang peng yong
 * @date: 2024/7/16 12:53
 * @email: 56002982@qq.com
 * @desc: 滤镜渲染类型
 * [OnScreen] 上屏
 * [OffScreen] 离屏
 */
enum class RenderType { OnScreen, OffScreen }

/**
 * @author: jiang peng yong
 * @date: 2024/7/16 12:53
 * @email: 56002982@qq.com
 * @desc: 滤镜链渲染器
 */
class FilterChainRenderer(renderType: RenderType) : GLRenderer {
    val filterChain = FilterChain(renderType)

    override fun onEGLCreated(egl: EGL, surface: EglSurface, glHandler: GLHandler) {
        filterChain.init(egl, surface, glHandler)
    }

    override fun onSurfaceSizeChanged(surface: EglSurface) {
        filterChain.notifySurfaceSizeChanged(surface)
    }

    override fun onDrawFrame() {
        filterChain.drawFrame()
    }

    override fun onEGLDestroy() {
        filterChain.release()
    }

    companion object {
        const val TAG = "FilterChainRenderer"
    }
}