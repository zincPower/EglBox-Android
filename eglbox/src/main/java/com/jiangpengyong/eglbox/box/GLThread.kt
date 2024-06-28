//package com.jiangpengyong.eglbox.box
//
//import android.os.Looper
//import android.util.Log
//import android.view.Surface
//import com.jiangpengyong.eglbox.EGL
//import com.jiangpengyong.eglbox.egl.EglSurface
//import com.jiangpengyong.eglbox.egl.EglSurfaceType
//import com.jiangpengyong.eglbox.egl.SurfaceType
//import com.jiangpengyong.eglbox.egl.WindowSurface
//import com.jiangpengyong.eglbox.logger.Logger
//
//class GLThread(val config: GLClientConfig) : Thread(TAG) {
//
//    private var mEGL: EGL? = null
//    private var mHandler: GLHandler? = null
//    private var mRenderer: GLRenderer? = null
//    private var mEglSurface: EglSurface? = null
//
//    private var mIsCalledOnEglCreated = false
//
//    // 用于限制多次初始化
//    private var mIsRunning = false
//
//    // 用于控制线程已经初始化好
//    private var mIsThreadReady = false
//    private val mReadyLock = Object()
//
//    override fun run() {
//        Log.i(TAG, "------------------------ Enter GLThread. Start GL init logic. ------------------------")
//        Log.i(TAG, "GLThread id=${currentThread()}")
//
//        Log.i(TAG, "------------------------ Init EGL env ------------------------")
//        mEGL = EGL()
//        mEGL?.init()
//        if (mEGL?.isInit() != true) {
//            Log.e(TAG, "EGL init failure.")
//            mEGL = null
//            quitLoop()
//            return
//        }
//
//        Looper.prepare()
//        val looper = Looper.myLooper()
//        if (looper == null) {
//            Log.e(TAG, "Looper is null.")
//            mEGL = null
//            quitLoop()
//            return
//        }
//        mHandler = GLHandler(looper, this)
//
//        synchronized(mReadyLock) {
//            mIsThreadReady = true
//            mReadyLock.notify()
//        }
//
//        if (config.surfaceType == EglSurfaceType.PBuffer) {
//            Logger.i(TAG, "------------------------ Create PBuffer ------------------------");
//            createPBuffer(config.width, config.height)
//            if (mEglSurface == null) {
//                Log.e(TAG, "Create PBuffer failure.")
//                mEGL?.makeNothingCurrent()
//                mEGL?.release()
//                mEGL = null
//
//                mHandler?.removeCallbacksAndMessages(null)
//                mHandler = null
//                quitLoop()
//                return
//            }
//        }
//
//        Log.i(TAG, "------------------------ 进入事件循环 ------------------------")
//        Looper.loop()
//        Log.i(TAG, "------------------------ 退出事件循环 ------------------------")
//
//        Log.i(TAG, "------------------------ 开始释放资源 ------------------------")
//        if (mIsCalledOnEglCreated) {
//            Log.i(TAG, "释放 renderer");
//            mRenderer?.onEGLDestroy()
//            mRenderer = null
//            mIsCalledOnEglCreated = false
//        }
//
//        mEGL?.makeNothingCurrent()
//
//        Log.i(TAG, "释放 EglSurface")
//        mEglSurface?.release()
//        mEglSurface = null
//
//        Log.i(TAG, "释放 EGL")
//        mEGL?.release()
//        mEGL = null
//
//        Log.i(TAG, "释放 Handler")
//        mHandler?.removeCallbacksAndMessages(null)
//        mHandler = null
//
//        quitLoop()
//        Log.i(TAG, "------------------------ 退出 GLThread 线程 ------------------------")
//    }
//
//    private fun quitLoop() = synchronized(mReadyLock) {
//        Log.i(TAG, "Loop quit.")
//        mIsRunning = false
//        mIsThreadReady = false
//        mReadyLock.notify()
//    }
//
//    fun handleRequestRender() {
//        val egl = mEGL
//        if (egl == null) {
//            Log.e(TAG, "EGL is null【handleRequestRender】.")
//            return
//        }
//        val renderer = mRenderer
//        if (renderer == null) {
//            Log.e(TAG, "Renderer is null【handleRequestRender】.")
//            return
//        }
////        val config = mConfig
////        if (!config) {
////            Log::e(TAG, "Config is null【handleRequestRender】.")
////            return
////        }
//        val eglSurface = mEglSurface
//        if (eglSurface == null) {
//            Log.e(TAG, "EglSurface is null【handleRequestRender】.")
//            return
//        }
//        config.drawFrameListener?.onStartDrawFrame()
//        egl.makeCurrent(eglSurface)
//        renderer.onDrawFrame()
//        if (config.surfaceType == EglSurfaceType.Window) {
//            (eglSurface as? WindowSurface)?.swapBuffer()
//        }
//        config.drawFrameListener?.onFinishDrawFrame()
//    }
//
//    fun handleWindowCreated(window: Surface) {
//
//    }
//
//    fun handleWindowChangeSize(window: Surface, width: Int, height: Int) {
//
//    }
//
//    fun handleWindowDestroy(window: Surface) {
//
//    }
//
//    fun waitUntilReady() = synchronized(mReadyLock) {
//        while (!mIsThreadReady) {
//            mReadyLock.wait()
//        }
//    }
//
//    private fun createPBuffer(width: Int, height: Int) {
//        val egl = mEGL
//        if (egl == null) {
//            Log.e(TAG, "EGL is null【createPBuffer】.")
//            return
//        }
//        val renderer = mRenderer
//        if (renderer == null) {
//            Logger.e(TAG, "Renderer is null【createPBuffer】.")
//            return
//        }
//        if (width <= 0 || height <= 0) {
//            Logger.e(TAG, "PBuffer size is invalid【createPBuffer】. width=${width}, height=${height}")
//            return
//        }
//        mEglSurface?.release()
//        mEglSurface = null
//        mEglSurface = egl.createPBuffer(width, height)
//        mEglSurface?.let { egl.makeCurrent(it) }
//        if (!mIsCalledOnEglCreated) {
//            renderer.onEGLCreated(egl, mHandler)
//            mIsCalledOnEglCreated = true
//        }
//        mEglSurface?.let { renderer.onSurfaceSizeChanged(it, width, height) }
//    }
//
//    companion object {
//        private const val TAG = "GLThread"
//    }
//}