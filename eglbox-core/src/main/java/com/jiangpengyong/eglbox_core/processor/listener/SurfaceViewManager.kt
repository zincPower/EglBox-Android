package com.jiangpengyong.eglbox_core.processor.listener

import android.view.Surface
import com.jiangpengyong.eglbox_core.logger.Logger

object SurfaceViewManager {
    interface Listener {
        fun onCreated(window: Any, width: Int, height: Int)
        fun onChanged(window: Any, width: Int, height: Int)
        fun onDestroy(window: Any)
    }

    const val TAG = "SurfaceViewManager"
    private val mMap = HashMap<String, SurfaceView>()
    private val mLock = Object()

    fun surfaceCreated(id: String, nativeWindow: Any, width: Int, height: Int) = synchronized(mLock) {
        Logger.i(TAG, "surfaceCreated id=${id}, nativeWindow=${nativeWindow}, size=${width}x${height}, map=${mMap.size}")
        var surfaceView = mMap[id]
        if (surfaceView == null) {
            surfaceView = SurfaceView()
            mMap[id] = surfaceView
        }
        surfaceView.onCreated(nativeWindow, width, height)
    }

    fun surfaceSizeChanged(id: String, nativeWindow: Any, width: Int, height: Int) = synchronized(mLock) {
        Logger.i(TAG, "surfaceChanged id=${id}, nativeWindow=${nativeWindow}, size=${width}x${height}, map=${mMap.size}")
        var surfaceView = mMap[id]
        if (surfaceView == null) {
            Logger.e(TAG, "SurfaceChanged invalid data. id=${id}, nativeWindow=${nativeWindow}, size=${width}x${height}")
            surfaceView = SurfaceView()
            mMap[id] = surfaceView
        }
        surfaceView.onChanged(nativeWindow, width, height)
    }

    fun surfaceDestroy(id: String, nativeWindow: Any) = synchronized(mLock) {
        Logger.i(TAG, "surfaceDestroy id=${id}, nativeWindow=${nativeWindow}, map=${mMap.size}")
        val surfaceView = mMap[id]
        if (surfaceView == null) {
            Logger.e(TAG, "SurfaceDestroy invalid data. id=${id}, nativeWindow=${nativeWindow}")
            return
        }
        surfaceView.onDestroy(nativeWindow)
        maybeRemoveSurfaceView(id)
    }

    fun registerListener(id: String, listener: Listener) = synchronized(mLock) {
        Logger.i(TAG, "registerListener id=${id}, listener=${listener}")
        var surfaceView = mMap[id]
        if (surfaceView == null) {
            surfaceView = SurfaceView()
            mMap[id] = surfaceView
        }
        surfaceView.addListener(listener)
    }

    fun removeListener(id: String, listener: Listener) = synchronized(mLock) {
        Logger.i(TAG, "removeListener id=${id}, listener=${listener}")
        val surfaceView = mMap[id]
        if (surfaceView == null) {
            Logger.e(TAG, "RemoveListener invalid data. id=${id}, listener=${listener}")
            return
        }
        surfaceView.removeListener(listener)
        maybeRemoveSurfaceView(id)
    }

    private fun maybeRemoveSurfaceView(id: String) {
        Logger.i(TAG, "maybeRemoveSurfaceView id=${id}")
        val surfaceView = mMap[id] ?: return
        // 如果已经是销毁，并且没有监听了就进行移除
        if ((surfaceView.state == SurfaceView.State.Destroy) && (surfaceView.getListenerSize() == 0)) {
            Logger.i(TAG, "maybeRemoveSurfaceView real remove id=${id}")
            mMap.remove(id)
        }
    }
}