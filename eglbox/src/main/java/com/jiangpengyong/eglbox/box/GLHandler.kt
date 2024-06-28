package com.jiangpengyong.eglbox.box

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
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
        // TODO 后续再加上同步 window
        when (msg.what) {
            MSG_REQUEST_RENDER -> thread.handleRequestRender()
            MSG_WINDOW_CREATED -> thread.handleWindowCreated(msg.data, msg.arg1, msg.arg2)
            MSG_WINDOW_CHANGE_SIZE -> thread.handleWindowChangeSize(msg.data, msg.arg1, msg.arg2)
            MSG_WINDOW_DESTROY -> thread.handleWindowDestroy(msg.data)
        }
    }

    fun sendRequestRenderMessage() {
        val message = obtainMessage()
        message.what = MSG_REQUEST_RENDER
        sendMessage(message)
    }

    fun sendWindowCreatedMessage(window: Surface) {
        val message = obtainMessage()
        message.what = MSG_WINDOW_CREATED
        // 不需要释放 window ，让系统进行自行回收
        message.obj = window
        sendMessage(message)
    }

    fun sendWindowChangeSizeMessage(window: Surface, width: Int, height: Int) {
        val message = obtainMessage()
        message.what = MSG_WINDOW_CHANGE_SIZE
        message.arg1 = width
        message.arg2 = height
        // 不需要释放 window ，让系统进行自行回收
        message.obj = window
        sendMessage(message)
    }

    fun sendWindowDestroyMessage(window: Surface) {
        val message = obtainMessage()
        message.what = MSG_WINDOW_DESTROY
        // 不需要释放 window ，让系统进行自行回收
        message.obj = window
        sendMessage(message)
    }

    companion object {
        private const val TAG = "GLHandler"
        private const val MSG_REQUEST_RENDER = 1
        private const val MSG_WINDOW_CREATED = 2
        private const val MSG_WINDOW_CHANGE_SIZE = 3
        private const val MSG_WINDOW_DESTROY = 4
    }
}