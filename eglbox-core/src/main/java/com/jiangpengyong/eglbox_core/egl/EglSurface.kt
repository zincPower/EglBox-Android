package com.jiangpengyong.eglbox_core.egl

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.view.Surface
import com.jiangpengyong.eglbox_core.logger.Logger

enum class EglSurfaceType { Window, PBuffer }

open class EglSurface(
    state: EGLState,
    surfaceType: EglSurfaceType,
    width: Int,
    height: Int
) {
    protected var mState: EGLState = state
    var surfaceType: EglSurfaceType = surfaceType
        private set
    var surface: EGLSurface? = null
        protected set

    var width: Int = width
        private set
    var height: Int = height
        private set

    fun updateSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun release() {
        Logger.i(TAG, "Release EglSurface.");
        if (!mState.isValid()) return
        if (surface != null) EGL14.eglDestroySurface(mState.display, surface)
        surface = null
    }

    companion object {
        private const val TAG = "EglSurface"
    }
}

class WindowSurface(
    state: EGLState,
    window: Any,
    width: Int,
    height: Int
) : EglSurface(state, EglSurfaceType.Window, width, height) {
    private val TAG = "WindowSurface"

    init {
        val attribList = intArrayOf(EGL14.EGL_NONE)
        surface = EGL14.eglCreateWindowSurface(
            state.display,
            state.config,
            window,
            attribList,
            0
        )
        Logger.i(TAG, "Create window surface.")
    }

    fun swapBuffer() {
        if (!mState.isValid()) {
            Logger.e(TAG, "EGLState is invalid.")
            return;
        }
        EGL14.eglSwapBuffers(mState.display, surface)
    }
}

class PBufferSurface(
    state: EGLState,
    width: Int,
    height: Int
) : EglSurface(state, EglSurfaceType.PBuffer, width, height) {
    private val TAG = "PBufferSurface"

    init {
        val attribList = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )
        surface = EGL14.eglCreatePbufferSurface(mState.display, mState.config, attribList, 0)
        Logger.i(TAG, "Create pbuffer surface.")
    }

}