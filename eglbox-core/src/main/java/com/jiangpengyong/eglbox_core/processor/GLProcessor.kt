package com.jiangpengyong.eglbox_core.processor

import android.os.Bundle
import android.os.Message
import androidx.annotation.MainThread
import com.jiangpengyong.eglbox_core.GLThread
import com.jiangpengyong.eglbox_core.engine.DrawFrameListener
import com.jiangpengyong.eglbox_core.engine.FilterChainRenderer
import com.jiangpengyong.eglbox_core.engine.GLEngine
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterChain
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.MessageListener
import com.jiangpengyong.eglbox_core.gles.EGLBox
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.MessageType.REQUEST_RENDER

/**
 * @author: jiang peng yong
 * @date: 2024/7/23 12:55
 * @email: 56002982@qq.com
 * @desc: GL 处理器
 */
abstract class GLProcessor : MessageListener {
    private var mGLEngine: GLEngine? = null
    private var mGLRenderer: FilterChainRenderer? = null

    @MainThread
    fun launch() {
        if (mGLEngine != null) {
            Logger.e(TAG, "GLEngine has been initialized.")
            return
        }
        if (mGLRenderer != null) {
            Logger.e(TAG, "GLRenderer has been initialized.")
            return
        }
        mGLEngine = createGLEngine()
        val renderer = FilterChainRenderer(getRenderType()).apply { mGLRenderer = this }
        val filterChain = renderer.filterChain
        configFilterChain(filterChain)
        filterChain.addListener(this)
        onLaunch()
        mGLEngine?.start(renderer)
    }

    @MainThread
    fun destroy() {
        Logger.i(TAG, "GLProcessor destroy.")
        onDestroy()
        mGLRenderer?.filterChain?.removeListener(this)
        mGLRenderer = null
        mGLEngine?.setDrawFrameListener(null)
        mGLEngine?.stop()
        mGLEngine = null
    }

    @MainThread
    fun enqueueEvent(block: () -> Unit) {
        if (mGLEngine == null) {
            Logger.e(TAG, "GLClint isn't initialized. Please call launch function first. [enqueueEvent]")
            return
        }
        mGLEngine?.enqueueEvent(block)
    }

    @MainThread
    fun <T> invokeInGLEngine(timeout: Long, block: () -> T): T? {
        if (mGLEngine == null) {
            Logger.e(TAG, "GLClint isn't initialized. Please call launch function first. [invokeInGLEngine]")
            return null
        }
        return mGLEngine?.invokeInGLEngine(block, timeout)
    }

    @MainThread
    fun requestRender() {
        if (mGLEngine == null) {
            Logger.e(TAG, "GLClint isn't initialized. Please call launch function first. [requestRender]")
            return
        }
        mGLEngine?.requestRender()
    }

    @MainThread
    fun updateFilterData(filterId: String, data: Bundle) = enqueueEvent {
        mGLRenderer?.filterChain?.updateFilterData(filterId, data)
    }

    @MainThread
    fun addFilter(filterId: String, name: String, order: Int, filter: GLFilter) = enqueueEvent {
        mGLRenderer?.filterChain?.addFilter(filterId, name, order, filter)
    }

    @MainThread
    fun removeFilter(filterId: String) = enqueueEvent {
        mGLRenderer?.filterChain?.removeFilter(filterId)
    }

    @MainThread
    fun sendMessageToFilter(filterId: String, message: Message) = enqueueEvent {
        mGLRenderer?.filterChain?.sendMessageToFilter(filterId, message)
    }

    @GLThread
    override fun onReceiveMessage(filterId: String, message: Message) {
        when (message.what) {
            REQUEST_RENDER -> requestRender()
            else -> onReceiveMessageFromFilter(filterId, message)
        }
    }

    fun setDrawFrameListener(listener: DrawFrameListener) {
        mGLEngine?.setDrawFrameListener(listener)
    }

    fun getFilterChain() = mGLRenderer?.filterChain

    fun getMaxTextureSize(): Int? {
        return invokeInGLEngine(1000) {
            EGLBox.getMaxTextureSize()
        }
    }

    @MainThread
    protected abstract fun onLaunch()

    @MainThread
    protected abstract fun onDestroy()

    @MainThread
    protected abstract fun createGLEngine(): GLEngine

    @MainThread
    protected abstract fun getRenderType(): RenderType

    @MainThread
    protected abstract fun configFilterChain(filterChain: FilterChain)

    @GLThread
    protected abstract fun onReceiveMessageFromFilter(filterId: String, message: Message)

    companion object {
        private const val TAG = "GLProcessor"
        const val SOURCE_FILTER_ID = "source"
        const val SINK_FILTER_ID = "sink"
    }
}