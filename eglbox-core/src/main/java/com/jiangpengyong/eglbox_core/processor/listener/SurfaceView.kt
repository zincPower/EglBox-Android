package com.jiangpengyong.eglbox_core.processor.listener

import android.view.Surface
import com.jiangpengyong.eglbox_core.logger.Logger

class SurfaceView : SurfaceViewManager.Listener {
    enum class State { Idle, Created, SizeChanged, Destroy }

    var state = State.Idle
        private set
    private var mWindow: Any? = null
    private var mWidth = 0
    private var mHeight = 0
    private var mListeners = HashSet<SurfaceViewManager.Listener>()

    fun getListenerSize() = mListeners.size

    override fun onCreated(window: Any, width: Int, height: Int) {
        Logger.i(TAG, "onCreated window=${window}, size=${width}x${height}")
        state = State.Created
        mWindow = window
        mWidth = width
        mHeight = height
        notifyListener()
    }

    override fun onChanged(window: Any, width: Int, height: Int) {
        Logger.i(TAG, "onChanged window=${window}, size=${width}x${height}")
        state = State.SizeChanged
        mWindow = window
        mWidth = width
        mHeight = height
        notifyListener()
    }

    override fun onDestroy(window: Any) {
        Logger.i(TAG, "onDestroy window=${window}")
        state = State.Destroy
        mWindow = window
        mWidth = 0
        mHeight = 0
        notifyListener()
    }

    fun addListener(listener: SurfaceViewManager.Listener) {
        mListeners.add(listener)
        notifyListener(true)
    }

    fun removeListener(listener: SurfaceViewManager.Listener) {
        mListeners.remove(listener)
    }

    private fun notifyListener(needCallCreatedCallback: Boolean = false) {
        if (mListeners.isEmpty()) return
        for (listener in mListeners) {
            when (state) {
                State.Idle -> {} // nothing to do，等待数据进入后进行推送
                State.Created -> {
                    listener.onCreated(mWindow ?: return, mWidth, mHeight)
                }

                State.SizeChanged -> {
                    val window = mWindow ?: return
                    if (needCallCreatedCallback) {
                        listener.onCreated(window, mWidth, mHeight)
                    }
                    listener.onChanged(window, mWidth, mHeight)
                }

                State.Destroy -> {
                    listener.onDestroy(mWindow ?: return)
                }
            }
        }
    }

    companion object {
        const val TAG = "SurfaceView"
    }
}