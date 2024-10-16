package com.jiangpengyong.eglbox_core.processor.image

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class TaskPool {
    private val mTaskMap = HashMap<Int, ImageProcessTask>()
    private val mIsRunning = AtomicBoolean(true)
    private val mLock = Object()

    fun isRunning() = mIsRunning

    fun getSize(): Int = synchronized(mLock) {
        return mTaskMap.size
    }

    fun saveTask(task: ImageProcessTask) = synchronized(mLock) {
        if (!mIsRunning.get()) {
            Log.i(TAG, "TaskPool is quited.")
            return@synchronized
        }
        Log.i(TAG, "TaskPool add a task. task=${task}")
        mTaskMap[task.id] = task
    }

    fun getTask(taskId: Int, block: () -> Unit): ImageProcessTask? = synchronized(mLock) {
        val task = mTaskMap.remove(taskId)
        Log.i(TAG, "Task pool get a new task. task=${task}")
        block()
        return task
    }

    fun removeTask(taskId: Int) = synchronized(mLock) {
        Log.i(TAG, "TaskPool remove task. id=${taskId}")
        mTaskMap.remove(taskId)
    }

    fun removeAllTask() = synchronized(mLock) {
        Log.i(TAG, "TaskPool remove all tasks. Task pool size=${mTaskMap.size}")
        mTaskMap.clear()
    }

    fun quit() {
        mIsRunning.set(false)
    }

    companion object {
        private const val TAG = "TaskPool"
    }
}