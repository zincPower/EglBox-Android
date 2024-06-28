package com.jiangpengyong.eglbox.box

import com.jiangpengyong.eglbox.egl.EglSurfaceType

enum class RenderType { OnScreen, OffScreen }

interface DrawFrameListener {
    fun onStartDrawFrame()
    fun onFinishDrawFrame()
}

data class GLClientConfig(
    val surfaceType: EglSurfaceType,
    val width: Int,
    val height: Int,
    val drawFrameListener: DrawFrameListener?
)