package com.jiangpengyong.eglbox.egl

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.util.Log
import android.view.Surface
import com.jiangpengyong.eglbox.gles.EGLBox
import com.jiangpengyong.eglbox.logger.Logger

data class GLVersion(val major: Int, val minor: Int) {
    override fun toString(): String {
        return "[ GLVersion major=${major} minor=${minor} ]"
    }
}

class EGLState(
    display: EGLDisplay?,
    config: EGLConfig?,
    context: EGLContext?,
    val glVersion: GLVersion,
    val eglVersion: GLVersion
) {
    var display: EGLDisplay? = display
        private set
    var config: EGLConfig? = config
        private set
    var context: EGLContext? = context
        private set

    fun isValid(): Boolean {
        return display != null && config != null && context != null
    }

    fun reset() {
        display = null
        config = null
        context = null
    }

    override fun toString(): String {
        return "[ EGLState display=${display} config=${config} context=${context} glVersion=${glVersion} eglVersion=${eglVersion} ]"
    }
}

class EGL {
    companion object {
        private const val TAG = "EGL"
    }

    private var mState: EGLState? = null
    private var mDrawSurface: EglSurface? = null
    private var mReadSurface: EglSurface? = null

    fun init(shareContext: EGLContext = EGL14.EGL_NO_CONTEXT) {
        if (isInit()) {
            Logger.e(TAG, "EGL has been initialized. state=${mState}")
            return
        }

        // 1、获取窗口系统
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) {
            Logger.e(TAG, "EglGetDisplay failure.【EGL_NO_DISPLAY】")
            EGLBox.checkError(TAG)
            return
        } else {
            Logger.i(TAG, "EglGetDisplay success.")
        }

        // 2、初始化 egl
        val eglVersion = IntArray(2)
        val initializedResult = EGL14.eglInitialize(display, eglVersion, 0, eglVersion, 1)
        if (!initializedResult) {
            Logger.e(TAG, "EglInitialize failure. result=${initializedResult}")
            EGLBox.checkError(TAG)
            EGL14.eglTerminate(display)
            return
        } else {
            Logger.i(TAG, "EglInitialize success. majorVersion=${eglVersion[0]}, minorVersion=${eglVersion[1]}")
        }

        // 3、确定可用表面配置
        val (context, config, glVersion) = createContext(display, shareContext)
        if (context == null || config == null) {
            Logger.e(TAG, "Create context failure. glVersion=${glVersion}")
            EGL14.eglTerminate(display)
            return
        } else {
            Logger.i(TAG, "Create context success. glVersion=${glVersion}")
        }

        val glVer = GLVersion(glVersion, 0)
        val eglVer = GLVersion(eglVersion[0], eglVersion[1])
        mState = EGLState(display, config, context, glVer, eglVer)
        Logger.i(TAG, "Create EGL success. state=${mState}")
    }

    fun release() {
        if (!isInit()) return
        mState?.display?.let { eglDisplay ->
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            mState?.context?.let { eglContext ->
                EGL14.eglDestroyContext(eglDisplay, eglContext)
            }
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)
        }
        mDrawSurface = null
        mReadSurface = null
        mState?.reset()
        mState = null
        Log.i(TAG, "Release EGL.")
    }

    fun isInit(): Boolean {
        return mState?.isValid() ?: false
    }

    fun createWindow(window: Surface): WindowSurface? {
        val state = mState
        return if (state == null) {
            Logger.i(TAG, "State is null. Please call init method first.【createWindow】")
            null
        } else {
            return WindowSurface(state, window, 0, 0)
        }
    }

    fun createPBuffer(width: Int, height: Int): PBufferSurface? {
        val state = mState
        return if (state == null) {
            Logger.i(TAG, "State is null. Please call init method first.【createPBuffer】")
            null
        } else {
            return PBufferSurface(state, width, height)
        }
    }

    fun makeCurrent(surface: EglSurface) {
        makeCurrent(surface, surface)
    }

    fun makeCurrent(drawSurface: EglSurface, readSurface: EglSurface) {
        if (!isInit()) {
            Logger.e(TAG, "EGL isn't initialized. Please call init function first.")
            return
        }
        mDrawSurface = drawSurface
        mReadSurface = readSurface
        val result = EGL14.eglMakeCurrent(
            mState?.display,
            drawSurface.surface,
            readSurface.surface,
            mState?.context
        );
        if (!result) {
            Logger.e(TAG, "EglMakeCurrent failure.【makeCurrent】");
            EGLBox.checkError(TAG)
        }
    }

    fun makeNothingCurrent() {
        mDrawSurface = null
        mReadSurface = null
        val result = EGL14.eglMakeCurrent(
            mState?.display,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        if (!result) {
            Logger.e(TAG, "EglMakeCurrent failure.【makeNothingCurrent】")
            EGLBox.checkError(TAG)
        }
    }

    private fun createContext(display: EGLDisplay, shareContext: EGLContext): Triple<EGLContext?, EGLConfig?, Int> {
        var glVersion = 3
        var config = getEGLConfig(display, glVersion)
        if (config == null) {
            glVersion = 2
            config = getEGLConfig(display, glVersion)
        }
        if (config == null) {
            return Triple(null, null, glVersion)
        }
        val attribList = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, glVersion,
            EGL14.EGL_NONE
        )
        val context = EGL14.eglCreateContext(display, config, shareContext, attribList, 0)
        return if (context == EGL14.EGL_NO_CONTEXT) {
            Logger.e(TAG, "EglCreateContext failure.【EGL_NO_CONTEXT】")
            EGLBox.checkError(TAG)
            Triple(null, null, glVersion)
        } else {
            Triple(context, config, glVersion)
        }
    }

    private fun getEGLConfig(display: EGLDisplay, glVersion: Int): EGLConfig? {
        val renderType: Int = if (glVersion == 3) {
            EGLExt.EGL_OPENGL_ES3_BIT_KHR
        } else {
            EGL14.EGL_OPENGL_ES2_BIT
        }
        val attribList = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, renderType,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        val result = EGL14.eglChooseConfig(display, attribList, 0, configs, 0, 1, numConfigs, 1)
        return if (result) {
            Logger.i(TAG, "EglChooseConfig success. renderType=${renderType}, rgba format=8888")
            configs[0]
        } else {
            Logger.e(TAG, "EglChooseConfig failure. renderType=${renderType}, rgba format=8888")
            EGLBox.checkError(TAG)
            null
        }
    }
}