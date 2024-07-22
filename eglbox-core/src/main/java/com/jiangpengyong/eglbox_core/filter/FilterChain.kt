package com.jiangpengyong.eglbox_core.filter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.MainThread
import com.jiangpengyong.eglbox_core.GLThread
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.egl.EGL
import com.jiangpengyong.eglbox_core.egl.EglSurface
import com.jiangpengyong.eglbox_core.logger.Logger

/**
 * @author: jiang peng yong
 * @date: 2024/7/16 12:55
 * @email: 56002982@qq.com
 * @desc: 滤镜链
 */
class FilterChain(renderType: RenderType) {
    private var mIsInit = false

    private val mFilterSlot = GLFilterGroup().apply { id = "slot-filter-root" }
    private val mSourceFilter = GLFilterGroup().apply { id = "source-filter-group" }
    private val mProcessFilter = GLFilterGroup().apply { id = "process-filter-group" }
    private val mSinkFilter = GLFilterGroup().apply { id = "sink-filter-group" }

    private var mContext = FilterContext(renderType)

    private val mListener = FilterChainListenerImpl()

    @GLThread
    fun init(egl: EGL) {
        if (mIsInit) return
        Logger.i(TAG, "FilterChain init.")
        mIsInit = true;
        mContext.init(egl, mListener)
        mFilterSlot.addFilter(mSourceFilter)
        mFilterSlot.addFilter(mProcessFilter)
        mFilterSlot.addFilter(mSinkFilter)
        mFilterSlot.init(mContext)
    }

    @GLThread
    fun release() {
        mFilterSlot.release()
        mContext.release()
        mIsInit = false
    }

    @GLThread
    fun drawFrame() {
        if (!mIsInit) {
            Logger.e(TAG, "Filter chain hasn't initialized. Please call FilterChain::init function first.")
            return
        }
        if (mSourceFilter.size() <= 0) {
            Logger.e(TAG, "SourceFilter is null. Please call FilterChain::setSourceFilter first.")
            return
        }
        val sourceFilter = mSourceFilter[0] as? SourceFilter
        if (sourceFilter == null) {
            Logger.e(TAG, "SourceFilter isn't a SourceFilter.")
            return
        }
        val image = sourceFilter.getImage()
        if (!image.isValid()) {
            Logger.e(TAG, "ImageInOut is invalid.")
            return
        }
        image.setContext(mContext)
        mFilterSlot.draw(image)
        mContext.renderData.clear()
        image.texture?.let { mContext.recycle(it) }
        image.clear()
    }

    @GLThread
    fun notifySurfaceSizeChanged(surface: EglSurface) {

    }

    @GLThread
    fun setSourceFilter(filter: GLFilter, filterId: String) {
        mSourceFilter.removeAllFilters()
        filter.id = filterId
        filter.name = filterId
        filter.order = 0
        mSourceFilter.addFilter(filter)
    }

    fun addFilter(filter: GLFilter, id: String, name: String, order: Int = 0) {
        filter.id = id
        filter.name = name
        filter.order = order
        mProcessFilter.addFilter(filter)
    }

    @GLThread
    fun removeFilter(filterId: String) = mFilterSlot.removeFilter(filterId)

    @GLThread
    fun removeAllProcessFilters() = mProcessFilter.removeAllFilters()

    @GLThread
    fun removeAllFilters() = mFilterSlot.removeAllFilters()

    @GLThread
    fun setSinkFilter(filter: GLFilter, filterId: String) {
        mSinkFilter.removeAllFilters()
        mSinkFilter.id = filterId
        mSinkFilter.name = filterId
        mSinkFilter.order = 0
        mSinkFilter.addFilter(filter)
    }

    @GLThread
    fun setRenderData(renderData: HashMap<String, Any>) {
        mContext.renderData.clear()
        mContext.renderData.putAll(renderData)
    }

    @GLThread
    fun updateFilterData(filterId: String, data: Bundle) {
        mFilterSlot.updateData(filterId, data)
    }

    @MainThread
    fun addListener(listener: MessageListener) {
        this.mListener.addListener(listener)
    }

    @MainThread
    fun removeListener(listener: MessageListener) {
        this.mListener.removeListener(listener)
    }

    companion object {
        const val TAG = "FilterChain"
    }
}

interface MessageListener {
    fun onReceiveMessage(filterId: String, message: Message)
}

interface FilterChainListener : MessageListener

class FilterChainListenerImpl : FilterChainListener {
    private val mListeners = HashSet<MessageListener>()
    private val mMainHandler = Handler(Looper.getMainLooper())

    @MainThread
    fun addListener(listener: MessageListener) {
        mListeners.add(listener)
    }

    @MainThread
    fun removeListener(listener: MessageListener) {
        mListeners.remove(listener)
    }

    @MainThread
    fun removeAllListeners() {
        mListeners.clear()
    }

    @GLThread
    override fun onReceiveMessage(filterId: String, message: Message) {
        mMainHandler.post {
            for (listener in mListeners) {
                listener.onReceiveMessage(filterId, message)
            }
        }
    }
}