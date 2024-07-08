package com.jiangpengyong.eglbox.box

import android.os.Looper
import android.util.Log
import android.view.Surface
import com.jiangpengyong.eglbox.egl.EGL
import com.jiangpengyong.eglbox.egl.EglSurface
import com.jiangpengyong.eglbox.egl.EglSurfaceType
import com.jiangpengyong.eglbox.egl.WindowSurface
import com.jiangpengyong.eglbox.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author: jiang peng yong
 * @date: 2023/9/25 23:04
 * @email: 56002982@qq.com
 * @desc: GL 线程
 *
 */
class GLThread(val config: GLClientConfig) : Thread(TAG) {
    private var mEGL: EGL? = null
    private var mHandler: GLHandler? = null
    private var mRenderer: GLRenderer? = null
    private var mEglSurface: EglSurface? = null
    private var mLooper: Looper? = null

    private var mIsCalledOnEglCreated = false

    // 用于限制多次初始化
    private var mIsRunning = AtomicBoolean(false)

    // 用于控制线程已经初始化好 TODO
    private var mIsThreadReady = false
    private val mReadyLock = Object()

    private var mWindowLock = Object()
    private var mWindowControlFinish = true

    fun isRunning() = mIsRunning.get()

    fun setRenderer(renderer: GLRenderer) {
        mRenderer = renderer
    }

    override fun start() {
        if (mRenderer == null) {
            Log.e(TAG, "Renderer is null. Please call GLThread::setRenderer set renderer first.")
            return
        }
        if (mIsRunning.get()) {
            Log.e(TAG, "GLThread is running. Can't start again.")
            return
        } else {
            Log.i(TAG, "GLThread start.")
            mIsRunning.set(true)
            super.start()
        }
    }

    fun quit() {
        Log.i(TAG, "Quit GLThread. mIsRunning=${mIsRunning}")
        if (mIsRunning.get()) mHandler?.sendRelease()
    }

    fun waitUntilReady() = synchronized(mReadyLock) {
        // todo
        while (!mIsThreadReady) {
            mReadyLock.wait()
        }
    }

    override fun run() {
        Log.i(TAG, "------------------------ Enter GLThread. Start GL init logic. ------------------------")
        Log.i(TAG, "GLThread id=${currentThread()}")

        Log.i(TAG, "------------------------ Init EGL env ------------------------")
        mEGL = EGL()
        mEGL?.init()
        if (mEGL?.isInit() != true) {
            Log.e(TAG, "EGL init failure.")
            mEGL = null
            quitLoop()
            return
        }

        Looper.prepare()
        val looper = Looper.myLooper().apply { mLooper = this }
        if (looper == null) {
            Log.e(TAG, "Looper is null.")
            mEGL = null
            quitLoop()
            return
        }
        mHandler = GLHandler(looper, this)

        synchronized(mReadyLock) {
            mIsThreadReady = true
            mReadyLock.notify()
        }

        if (config.surfaceType == EglSurfaceType.PBuffer) {
            Logger.i(TAG, "------------------------ Create PBuffer ------------------------");
            createPBuffer(config.width, config.height)
            if (mEglSurface == null) {
                Log.e(TAG, "Create PBuffer failure.")
                mEGL?.makeNothingCurrent()
                mEGL?.release()
                mEGL = null

                mHandler?.removeCallbacksAndMessages(null)
                mHandler = null
                quitLoop()
                return
            }
        }

        Log.i(TAG, "------------------------ 进入事件循环 ------------------------")
        Looper.loop()
        Log.i(TAG, "------------------------ 退出事件循环 ------------------------")

        Log.i(TAG, "------------------------ 开始释放资源 ------------------------")
        if (mIsCalledOnEglCreated) {
            Log.i(TAG, "释放 renderer");
            mRenderer?.onEGLDestroy()
            mRenderer = null
            mIsCalledOnEglCreated = false
        }

        mEGL?.makeNothingCurrent()

        Log.i(TAG, "释放 EglSurface")
        mEglSurface?.release()
        mEglSurface = null

        Log.i(TAG, "释放 EGL")
        mEGL?.release()
        mEGL = null

        Log.i(TAG, "释放 Handler")
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null

        quitLoop()
        Log.i(TAG, "------------------------ 退出 GLThread 线程 ------------------------")
    }

    private fun quitLoop() = synchronized(mReadyLock) {
        Log.i(TAG, "Loop quit.")
        mIsRunning.set(false)
        mIsThreadReady = false
        mReadyLock.notify()
    }

    fun requestRender() {
        this.mHandler?.sendRequestRenderMessage()
    }

    fun handleRequestRender() {
        val egl = mEGL
        if (egl == null) {
            Log.e(TAG, "EGL is null【handleRequestRender】.")
            return
        }
        val renderer = mRenderer
        if (renderer == null) {
            Log.e(TAG, "Renderer is null【handleRequestRender】.")
            return
        }
        val eglSurface = mEglSurface
        if (eglSurface == null) {
            Log.e(TAG, "EglSurface is null【handleRequestRender】.")
            return
        }
        config.drawFrameListener?.onStartDrawFrame()
        egl.makeCurrent(eglSurface)
        renderer.onDrawFrame()
        if (config.surfaceType == EglSurfaceType.Window) {
            (eglSurface as? WindowSurface)?.swapBuffer()
        }
        config.drawFrameListener?.onFinishDrawFrame()
    }

    fun handleWindowCreated(window: Surface?) = synchronized(mWindowLock) {
        mWindowControlFinish = false
        val result = mHandler?.post {
            val egl = mEGL
            if (egl == null) {
                Log.e(TAG, "EGL is null【handleWindowCreated】.")
                return@post
            }
            val renderer = mRenderer
            if (renderer == null) {
                Log.e(TAG, "Renderer is null【handleWindowCreated】.")
                return@post
            }
            if (window == null) {
                Log.e(TAG, "Param is invalid【handleWindowCreated】.")
                return@post
            }
            mEglSurface?.release()
            mEglSurface = null
            mEglSurface = egl.createWindow(window)
            mEglSurface?.let { egl.makeCurrent(it) }
            if (!mIsCalledOnEglCreated) {
                renderer.onEGLCreated(egl, mHandler)
                mIsCalledOnEglCreated = true
            }

            mWindowControlFinish = true
            mWindowLock.notify()
        } ?: false
        if (result) {
            while (!mWindowControlFinish) {
                mWindowLock.wait()
            }
        } else {
            mWindowControlFinish = true
        }

    }

    // TODO 加锁
    fun handleWindowSizeChanged(window: Surface?, width: Int, height: Int) {
        mHandler?.post {
            val renderer = mRenderer
            if (renderer == null) {
                Log.e(TAG, "Renderer is null【handleWindowChangeSize】.")
                return@post
            }
            if (window == null || width <= 0 || height <= 0) {
                Log.e(TAG, "Param is invalid【handleWindowChangeSize】.")
                return@post
            }
            val eglSurface = mEglSurface
            if (eglSurface == null) {
                Log.e(TAG, "Surface is null【handleWindowChangeSize】.")
                return@post
            }
            val windowSurface = eglSurface as? WindowSurface
            windowSurface?.updateSize(width, height)
            renderer.onSurfaceSizeChanged(eglSurface, width, height)
        }
    }

    fun handleWindowDestroy(window: Surface?) {
        mHandler?.post {
            val egl = mEGL
            if (egl == null) {
                Log.e(TAG, "EGL is null【handleWindowDestroy】.")
                return@post
            }
            val renderer = mRenderer
            if (renderer == null) {
                Log.e(TAG, "Renderer is null【handleWindowDestroy】.")
                return@post
            }
            egl.makeNothingCurrent()
            mEglSurface?.release()
            mEglSurface = null
        }
    }

    fun enqueueEvent(block: () -> Unit): Boolean {
        return mHandler?.post(block) ?: false
    }

    fun getLooper(): Looper? = mLooper

    private fun createPBuffer(width: Int, height: Int) {
        val egl = mEGL
        if (egl == null) {
            Log.e(TAG, "EGL is null【createPBuffer】.")
            return
        }
        val renderer = mRenderer
        if (renderer == null) {
            Logger.e(TAG, "Renderer is null【createPBuffer】.")
            return
        }
        if (width <= 0 || height <= 0) {
            Logger.e(TAG, "PBuffer size is invalid【createPBuffer】. width=${width}, height=${height}")
            return
        }
        mEglSurface?.release()
        mEglSurface = null
        mEglSurface = egl.createPBuffer(width, height)
        mEglSurface?.let { egl.makeCurrent(it) }
        if (!mIsCalledOnEglCreated) {
            renderer.onEGLCreated(egl, mHandler)
            mIsCalledOnEglCreated = true
        }
        mEglSurface?.let { renderer.onSurfaceSizeChanged(it, width, height) }
    }

    companion object {
        private const val TAG = "GLThread"
    }
}