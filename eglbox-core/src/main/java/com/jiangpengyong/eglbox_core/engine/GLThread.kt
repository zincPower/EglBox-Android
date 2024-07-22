package com.jiangpengyong.eglbox_core.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import com.jiangpengyong.eglbox_core.GLThread
import com.jiangpengyong.eglbox_core.egl.EGL
import com.jiangpengyong.eglbox_core.egl.EglSurface
import com.jiangpengyong.eglbox_core.egl.EglSurfaceType
import com.jiangpengyong.eglbox_core.egl.WindowSurface
import com.jiangpengyong.eglbox_core.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author: jiang peng yong
 * @date: 2023/9/25 23:04
 * @email: 56002982@qq.com
 * @desc: GL 线程，负责启动一个线程，并在该线程附加一个 [EGL] 环境
 *
 * 一、创建 [GLThread] 对象，需要提供 [GLEngineConfig] 配置，[GLThread] 启动后会根据 [GLEngineConfig] 配置的参数进行初始化 EGL 环境。
 *
 * 二、调用 [start] 启动 [GLThread] 前，必须通过 [setRenderer] 方法进行设置 [GLRenderer] 渲染回调对象，
 * 后续 EGL 的生命周期和渲染驱动会回调到 [GLRenderer] 相应方法：
 * 1、[GLRenderer.onEGLCreated] EGL 创建成功时回调
 * 2、[GLRenderer.onSurfaceSizeChanged] EGL 的 [EglSurface] 创建成功时回调。[GLRenderer.onEGLCreated] 回调后，
 * 如果能够创建 [EglSurface] ，则会立马创建 [EglSurface] 并且调用该方法。否则会在满足条件时，创建 [EglSurface] 后调用该方法。
 * 如果 [EglSurface] 尺寸有大小的变动也会调用此方法
 * 3、[GLRenderer.onDrawFrame] 在调用 [GLThread.requestRender] 方法之后会在 EGL 线程中调用 [GLRenderer.onDrawFrame] 方法，
 * 内部可以进行 GL 渲染操作
 * 4、[GLRenderer.onEGLDestroy] EGL 销毁前会调用该方法，调用此方法意味着 [GLThread] 已经退出 [Looper] 循环处理，线程将终止运行，
 * 需要在该方法中进行回收相应自愿。
 *
 * 调用 [start] 进行启动 [GLThread] 线程，调用前必须要保证通过 [setRenderer] 设置渲染回调对象，而且该线程没有在运行状态，
 * 否则 [GLThread] 不会正常启动，但不会抛异常，会有日志提示。
 *
 * 三、调用 [quit] 进行退出线程，但 [GLThread] 并不会立马退出，[quit] 会向 [Looper] 压入退出 [Message]，直到运行到该 [Message] 才会退出线程，
 * 这是一种安全的做法，但不是同步即时生效的。
 *
 * 四、调用 [waitUntilReady] 方法会阻塞当前线程，直到 GL 线程初始化 EGL 完成
 *
 * 五、调用 [isRunning] 可以获取线程是否还在运行
 *
 * 六、调用 [requestRender] 驱动 GL 渲染，会向 GL 线程压渲染任务，后续调用 [GLRenderer.onDrawFrame] 驱动渲染
 *
 * 七、如果传入 [GLEngineConfig] 配置是 [EglSurfaceType.Window] 类型，则需要在后续传入 [Surface] ，并相应的调用以下方法：
 * - [handleWindowCreated] ：[Surface] 创建时，调用该方法
 * - [handleWindowSizeChanged] ：[Surface] 尺寸变动时调用该方法
 * - [handleWindowDestroy] ：[Surface] 销毁时调用该方法
 * 调用后，[GLThread] 会负责维护该 [Surface] 创建的 [EglSurface] ，并附加在当前的 EGL 环境中。
 *
 * 八、[enqueueEvent] 向 GL 线程压入闭包，闭包的内容会在 GL 线程中执行
 *
 * 九、[getLooper] 获取 GL 线程的 [Looper] ，可以用于创建 [Handler]
 */
class GLThread(val config: GLEngineConfig) : Thread(TAG) {
    private var mEGL: EGL? = null
    private var mHandler: GLHandler? = null
    private var mRenderer: GLRenderer? = null
    private var mEglSurface: EglSurface? = null
    private var mLooper: Looper? = null

    // 是否已经调用了 EGLCreated 回调
    private var mIsCalledOnEglCreated = false

    // 是否已经运行
    private var mIsRunning = AtomicBoolean(false)

    // 用于控制线程已经初始化好
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
        Log.i(TAG, "Quit GLThread. Running status=${mIsRunning}")
        if (mIsRunning.get()) mHandler?.sendRelease()
    }

    fun waitUntilReady() = synchronized(mReadyLock) {
        while (!mIsThreadReady) {
            mReadyLock.wait()
        }
    }

    fun requestRender() {
        this.mHandler?.sendRequestRenderMessage()
    }

    fun handleWindowCreated(window: Surface?) = synchronized(mWindowLock) {
        mWindowControlFinish = false
        val result = mHandler?.post {
            val egl = mEGL
            if (egl == null) {
                Log.e(TAG, "EGL is null【handleWindowCreated】.")
                notifyWindowControlFinish()
                return@post
            }
            val renderer = mRenderer
            if (renderer == null) {
                Log.e(TAG, "Renderer is null【handleWindowCreated】.")
                notifyWindowControlFinish()
                return@post
            }
            if (window == null) {
                Log.e(TAG, "Param is invalid【handleWindowCreated】.")
                notifyWindowControlFinish()
                return@post
            }
            mEglSurface?.release()
            mEglSurface = null
            mEglSurface = egl.createWindow(window)
            mEglSurface?.let { egl.makeCurrent(it) }
            if (!mIsCalledOnEglCreated) {
                renderer.onEGLCreated(egl)
                mIsCalledOnEglCreated = true
            }

            notifyWindowControlFinish()
        } ?: false
        waitWindowControlFinish(result)
    }

    fun handleWindowSizeChanged(window: Surface?, width: Int, height: Int) = synchronized(mWindowLock) {
        mWindowControlFinish = false
        val result = mHandler?.post {
            val renderer = mRenderer
            if (renderer == null) {
                Log.e(TAG, "Renderer is null【handleWindowChangeSize】.")
                notifyWindowControlFinish()
                return@post
            }
            if (window == null || width <= 0 || height <= 0) {
                Log.e(TAG, "Param is invalid【handleWindowChangeSize】.")
                notifyWindowControlFinish()
                return@post
            }
            val eglSurface = mEglSurface
            if (eglSurface == null) {
                Log.e(TAG, "Surface is null【handleWindowChangeSize】.")
                notifyWindowControlFinish()
                return@post
            }
            val windowSurface = eglSurface as? WindowSurface
            windowSurface?.updateSize(width, height)
            renderer.onSurfaceSizeChanged(eglSurface)
            notifyWindowControlFinish()
        } ?: false
        waitWindowControlFinish(result)
    }

    fun handleWindowDestroy(window: Surface?) = synchronized(mWindowLock) {
        mWindowControlFinish = false
        val result = mHandler?.post {
            val egl = mEGL
            if (egl == null) {
                Log.e(TAG, "EGL is null【handleWindowDestroy】.")
                notifyWindowControlFinish()
                return@post
            }
            val renderer = mRenderer
            if (renderer == null) {
                Log.e(TAG, "Renderer is null【handleWindowDestroy】.")
                notifyWindowControlFinish()
                return@post
            }
            egl.makeNothingCurrent()
            mEglSurface?.release()
            mEglSurface = null
            notifyWindowControlFinish()
        } ?: false
        waitWindowControlFinish(result)
    }

    fun enqueueEvent(block: () -> Unit): Boolean {
        return mHandler?.post(block) ?: false
    }

    fun getLooper(): Looper? = mLooper

    @GLThread
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

    private fun notifyWindowControlFinish() = synchronized(mWindowLock) {
        mWindowControlFinish = true
        mWindowLock.notify()
    }

    private fun waitWindowControlFinish(isNeedWait: Boolean) = synchronized(mWindowLock) {
        if (isNeedWait) {
            while (!mWindowControlFinish) {
                mWindowLock.wait()
            }
        } else {
            mWindowControlFinish = true
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

        Log.i(TAG, "------------------------ loop start ------------------------")
        Looper.loop()
        Log.i(TAG, "------------------------ loop finish  ------------------------")

        Log.i(TAG, "------------------------ release resource ------------------------")
        if (mIsCalledOnEglCreated) {
            Log.i(TAG, "Release renderer.")
            mRenderer?.onEGLDestroy()
            mRenderer = null
            mIsCalledOnEglCreated = false
        }

        mEGL?.makeNothingCurrent()

        Log.i(TAG, "Release EglSurface.")
        mEglSurface?.release()
        mEglSurface = null

        Log.i(TAG, "Release EGL.")
        mEGL?.release()
        mEGL = null

        Log.i(TAG, "Release Handler.")
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null

        quitLoop()
        Log.i(TAG, "------------------------ quit GLThread ------------------------")
    }

    private fun quitLoop() = synchronized(mReadyLock) {
        Log.i(TAG, "Loop quit.")
        mIsRunning.set(false)
        mIsThreadReady = false
        mReadyLock.notify()
    }

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
            renderer.onEGLCreated(egl)
            mIsCalledOnEglCreated = true
        }
        mEglSurface?.let { renderer.onSurfaceSizeChanged(it) }
    }

    companion object {
        private const val TAG = "GLThread"
    }
}