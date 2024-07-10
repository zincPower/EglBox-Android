package com.jiangpengyong.eglbox.box

import com.jiangpengyong.eglbox.egl.EglSurfaceType

interface DrawFrameListener {
    fun onStartDrawFrame()
    fun onFinishDrawFrame()
}

data class GLEngineConfig(
    val surfaceType: EglSurfaceType,
    val width: Int,
    val height: Int,
) {
    var drawFrameListener: DrawFrameListener? = null
        set(value) = synchronized(this) {
            field = value
        }
        get() = synchronized(this) {
            field
        }
}