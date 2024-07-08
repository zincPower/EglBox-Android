package com.jiangpengyong.eglbox.box

import com.jiangpengyong.eglbox.egl.EGL
import com.jiangpengyong.eglbox.egl.EglSurface

interface GLRenderer {
    fun onEGLCreated(egl: EGL, handler: GLHandler?)

    fun onSurfaceSizeChanged(surface: EglSurface, width: Int, height: Int)

    fun onDrawFrame()

    fun onEGLDestroy()
}