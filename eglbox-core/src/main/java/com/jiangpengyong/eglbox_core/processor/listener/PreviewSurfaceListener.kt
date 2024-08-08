package com.jiangpengyong.eglbox_core.processor.listener

import android.os.Message
import android.view.Surface
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.GLProcessor
import com.jiangpengyong.eglbox_core.processor.MessageType
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class PreviewSurfaceListener(processor: GLProcessor) {
    private val mProcessor = WeakReference(processor)
    private val mWindowLock = Object()
    private val mWindowControlFinish = AtomicBoolean(false)

    fun onCreated(window: Surface, width: Int, height: Int) {
        Logger.i(TAG, "onCreated window=${window}, width=${width}, height=${height}")
        val processor = mProcessor.get()
        if (processor == null) {
            Logger.e(TAG, "Processor is null.【onCreated】")
            return
        }
        synchronized(mWindowLock) {
            mWindowControlFinish.set(false)
            processor.enqueueEvent {
                synchronized(mWindowLock) {
                    val message = Message.obtain()
                    message.what = MessageType.SURFACE_CREATED
                    message.obj = window
                    message.arg1 = width
                    message.arg2 = height
                    processor.sendMessageToFilter(GLProcessor.SOURCE_FILTER_ID, message)
                    processor.sendMessageToFilter(GLProcessor.SINK_FILTER_ID, message)
                    mWindowControlFinish.set(true)
                    Logger.i(TAG, "onCreated 结束 inner")
                    mWindowLock.notifyAll()
                }
            }
            while (!mWindowControlFinish.get()) {
                mWindowLock.wait()
            }
            Logger.i(TAG, "onCreated 结束 outer")
        }
    }

    fun onChanged(window: Surface, width: Int, height: Int) {
        Logger.i(TAG, "onChanged window=${window}, width=${width}, height=${height}")
        val processor = mProcessor.get()
        if (processor == null) {
            Logger.e(TAG, "Processor is null.【onChanged】")
            return
        }
        synchronized(mWindowLock) {
            mWindowControlFinish.set(false)
            processor.enqueueEvent {
                synchronized(mWindowLock) {
                    val message = Message.obtain()
                    message.what = MessageType.SURFACE_CHANGED
                    message.obj = window
                    message.arg1 = width
                    message.arg2 = height
                    processor.sendMessageToFilter(GLProcessor.SOURCE_FILTER_ID, message)
                    processor.sendMessageToFilter(GLProcessor.SINK_FILTER_ID, message)
                    mWindowControlFinish.set(true)
                    Logger.i(TAG, "onChanged 结束 inner")
                    mWindowLock.notifyAll()
                }
            }
            while (!mWindowControlFinish.get()) {
                mWindowLock.wait()
            }
            Logger.i(TAG, "onChanged 结束 outer")
        }
    }

    fun onDestroy(window: Surface) {
        Logger.i(TAG, "onDestroy window=${window}")
        val processor = mProcessor.get()
        if (processor == null) {
            Logger.e(TAG, "Processor is null.【onDestroy】")
            return
        }
        synchronized(mWindowLock) {
            mWindowControlFinish.set(false)
            processor.enqueueEvent {
                synchronized(mWindowLock) {
                    val message = Message.obtain()
                    message.what = MessageType.SURFACE_DESTROY
                    message.obj = window
                    processor.sendMessageToFilter(GLProcessor.SOURCE_FILTER_ID, message)
                    processor.sendMessageToFilter(GLProcessor.SINK_FILTER_ID, message)
                    mWindowControlFinish.set(true)
                    Logger.i(TAG, "onDestroy 结束 inner")
                    mWindowLock.notifyAll()
                }
            }
            while (!mWindowControlFinish.get()) {
                mWindowLock.wait()
            }
            Logger.i(TAG, "onDestroy 结束 outer")
        }
    }

    companion object {
        const val TAG = "PreviewSurfaceListener"
    }
}