package com.jiangpengyong.eglbox_core.engine

import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.annotation.MainThread
import com.jiangpengyong.eglbox_core.egl.EglSurfaceType
import com.jiangpengyong.eglbox_core.egl.WindowSurface
import com.jiangpengyong.eglbox_core.logger.Logger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author: jiang peng yong
 * @date: 2023/9/25 23:04
 * @email: 56002982@qq.com
 * @desc: GL 引擎
 * 通过静态方法 [createWindowType] 和 [createPBufferType] 创建相应的 [GLEngine]
 * [createWindowType] 会创建一个上屏的环境，调用者需要在后续通过以下方法通知 Window 的变化：
 * 1、[notifyWindowCreated] 通知 Window 创建
 * 2、[notifyWindowSizeChanged] 通知 Window 尺寸变动
 * 3、[notifyWindowDestroy] 通知 Window 销毁
 *
 * [createPBufferType] 会创建一个离屏的环境，如果需要上屏，调用者后续可以在 egl 环境内，通过 [com.jiangpengyong.eglbox_core.egl.EGL.createWindow]
 * 创建相应的 [WindowSurface] 进行上屏预览。
 *
 * 获取 [GLEngine] 后需要调用 [GLEngine.start] 方法进行启动，同时传入 [GLRenderer] 对象，内部会在适当的时候调用 [GLRenderer] 的方法，
 * 具体可见 [GLRenderer]
 *
 * 调用 [GLEngine.stop] 方法可以停止 [GLEngine] 的运行，内部会安全的退出 GL 线程，并回调每个环节的回收方法，退出完成后可以再次启动该引擎。
 *
 * 通过 [setDrawFrameListener] 设置监听，可以接收到每次开始绘制 [DrawFrameListener.onStartDrawFrame] 和结束绘制 [DrawFrameListener.onFinishDrawFrame] 的回调。
 *
 * 如果引擎已经启动，通过 [requestRender] 方法可以驱动内部进行一次绘制。
 *
 * 通过 [enqueueEvent] 添加闭包到 EGL 线程内运行。
 *
 * 通过 [getLooper] 获取 EGL 线程的 [Looper] ，然后创建相应的 [Handler] ，[Handler] 的处理操作则在 EGL 环境中，可以解耦外部操作。
 */
class GLEngine private constructor(config: GLEngineConfig) {
    companion object {
        const val TAG = "GLEngine"

        fun createWindowType(): GLEngine {
            val config = GLEngineConfig(EglSurfaceType.Window, 0, 0)
            return GLEngine(config)
        }

        fun createPBufferType(width: Int = 16, height: Int = 16): GLEngine {
            val config = GLEngineConfig(EglSurfaceType.PBuffer, width, height)
            return GLEngine(config)
        }
    }

    private var mGLThread: GLThread? = null
    private val mConfig: GLEngineConfig = config

    fun start(renderer: GLRenderer) {
        val thread = mGLThread
        if (thread != null && thread.isRunning()) {
            Logger.e(TAG, "GLEngine has been launched.")
            return
        }
        Logger.i(TAG, "GLEngine launch.")
        mGLThread = GLThread(mConfig)
            .also {
                it.setRenderer(renderer)
                it.start()
                it.waitUntilReady()
            }
    }

    fun stop() {
        Logger.i(TAG, "GLEngine release.")
        mGLThread?.quit()
        mGLThread = null
    }

    @MainThread
    fun requestRender() {
        mGLThread?.requestRender()
    }

    @MainThread
    fun setDrawFrameListener(listener: DrawFrameListener?) {
        mConfig.drawFrameListener = listener
    }

    @MainThread
    fun enqueueEvent(block: () -> Unit): Boolean {
        return mGLThread?.enqueueEvent(block) ?: false
    }

    @MainThread
    fun <T> invokeInGLEngine(block: () -> T, timeout: Long): T? {
        var result: T? = null
        try {
            val latch = CountDownLatch(1)
            val res = enqueueEvent {
                try {
                    result = block()
                } catch (e: Exception) {
                    Logger.e(TAG, "Call invokeInGLEngine failure【Inner】. $e")
                }
            }
            if (res) {
                latch.await(timeout, TimeUnit.MILLISECONDS)
            } else {
                Logger.i(TAG, "Call invokeInGLEngine failure【enqueueEvent】.")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Call invokeInGLEngine failure【Outer】. $e")
            result = null
        }
        return result
    }

    fun getLooper(): Looper? = mGLThread?.getLooper()

    fun getGLThread():GLThread? = mGLThread

    fun notifyWindowCreated(window: Any?, width: Int, height: Int) {
        mGLThread?.handleWindowCreated(window, width, height)
    }

    fun notifyWindowSizeChanged(window: Any?, width: Int, height: Int) {
        mGLThread?.handleWindowSizeChanged(window, width, height)
    }

    fun notifyWindowDestroy(window: Any?) {
        mGLThread?.handleWindowDestroy(window)
    }
}

