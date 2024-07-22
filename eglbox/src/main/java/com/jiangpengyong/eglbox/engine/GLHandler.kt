package com.jiangpengyong.eglbox.engine

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.jiangpengyong.eglbox.logger.Logger
import java.lang.ref.WeakReference

class GLHandler(looper: Looper, thread: GLThread) : Handler(looper) {
    private val mThread = WeakReference(thread)

    override fun handleMessage(msg: Message) {
        val thread = mThread.get()
        if (thread == null) {
            Logger.e(TAG, "Thread is null【handleMessage】. message=${msg.what}")
            return
        }
        when (msg.what) {
            MSG_REQUEST_RENDER -> thread.handleRequestRender()
            MSG_RELEASE -> Looper.myLooper()?.quit()
        }
    }

    fun sendRequestRenderMessage() {
        val message = obtainMessage()
        message.what = MSG_REQUEST_RENDER
        sendMessage(message)
    }

    fun sendRelease(){
        val message = obtainMessage()
        message.what = MSG_RELEASE
        sendMessage(message)
    }

    companion object {
        private const val TAG = "GLHandler"
        private const val MSG_REQUEST_RENDER = 1
        private const val MSG_RELEASE = 2
    }
}