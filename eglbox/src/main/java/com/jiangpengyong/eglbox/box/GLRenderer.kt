package com.jiangpengyong.eglbox.box

import com.jiangpengyong.eglbox.GLThread
import com.jiangpengyong.eglbox.egl.EGL
import com.jiangpengyong.eglbox.egl.EglSurface

/**
 * @author: jiang peng yong
 * @date: 2024/7/16 12:51
 * @email: 56002982@qq.com
 * @desc: GL渲染器
 */
interface GLRenderer {
    @GLThread
    fun onEGLCreated(egl: EGL)

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

    override fun onEGLCreated(egl: EGL) {
        filterChain.init(egl)
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