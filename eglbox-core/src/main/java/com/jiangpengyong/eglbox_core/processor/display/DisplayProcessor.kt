package com.jiangpengyong.eglbox_core.processor.display

import android.graphics.Bitmap
import android.os.Message
import com.jiangpengyong.eglbox_core.engine.GLEngine
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterChain
import com.jiangpengyong.eglbox_core.filter.FilterData
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.GLFilterGroup
import com.jiangpengyong.eglbox_core.processor.GLProcessor
import com.jiangpengyong.eglbox_core.processor.MessageType
import com.jiangpengyong.eglbox_core.processor.listener.PreviewSurfaceListener
import com.jiangpengyong.eglbox_core.processor.listener.SurfaceViewManager

/**
 * @author: jiang peng yong
 * @date: 2024/8/6 13:00
 * @email: 56002982@qq.com
 * @desc: 上屏处理器
 */
class DisplayProcessor : GLProcessor() {
    enum class FilterType(val id: String) { Process("process"), Decoration("decoration") }

    private val mProcessFilters = GLFilterGroup()
    private val mDecorateFilters = GLFilterGroup()

    private var mPreviewSurfaceId: String? = null
    private var mPreviewSurfaceListener: PreviewSurfaceListener? = null
    private var mListener: Listener? = null

    fun setListener(listener: Listener) = synchronized(this) {
        this.mListener = listener
    }

    fun removeListener() = synchronized(this) {
        this.mListener = null
    }

    fun addFilter(filterType: FilterType, id: String, name: String, order: Int, filter: GLFilter) {
        enqueueEvent {
            filter.id = id
            filter.name = name
            filter.order = order
            when (filterType) {
                FilterType.Process -> mProcessFilters.addFilter(filter)
                FilterType.Decoration -> mDecorateFilters.addFilter(filter)
            }
        }
    }

    fun getFilterData(filterType: FilterType, callback: (FilterData?) -> Unit) {
        enqueueEvent {
            val filterData = when (filterType) {
                FilterType.Process -> mProcessFilters.storeData()
                FilterType.Decoration -> mDecorateFilters.storeData()
            }
            callback(filterData)
        }
    }

    fun setPreviewSurfaceId(id: String) {
        var listener = mPreviewSurfaceListener
        if (listener == null) {
            listener = PreviewSurfaceListener(this)
            mPreviewSurfaceListener = listener
        }
        val beforeId = mPreviewSurfaceId
        if (!beforeId.isNullOrEmpty()) {
            SurfaceViewManager.removeListener(beforeId, listener)
        }
        mPreviewSurfaceId = id
        SurfaceViewManager.registerListener(id, listener)
    }

    fun setImage(bitmap: Bitmap, isAutoRelease: Boolean) {
        val message = Message.obtain()
        message.what = MessageType.DISPLAY_SET_IMAGE
        message.obj = bitmap
        message.arg1 = if (isAutoRelease) 1 else 0
        sendMessageToFilter(SOURCE_FILTER_ID, message)
    }

    fun setBlank() {
        val message = Message.obtain()
        message.what = MessageType.DISPLAY_SET_BLANK
        sendMessageToFilter(SOURCE_FILTER_ID, message)
    }

    override fun onLaunch() {}

    override fun onDestroy() {}

    override fun createGLEngine(): GLEngine = GLEngine.createPBufferType()

    override fun getRenderType(): RenderType = RenderType.OnScreen

    override fun configFilterChain(filterChain: FilterChain) {
        filterChain.setSourceFilter(SOURCE_FILTER_ID, DisplaySourceFilter())
        filterChain.addFilter(FilterType.Process.id, FilterType.Process.id, 0, mProcessFilters)
        filterChain.addFilter(FilterType.Decoration.id, FilterType.Decoration.id, 0, mDecorateFilters)
        filterChain.setSinkFilter(SINK_FILTER_ID, DisplaySinkFilter())
    }

    override fun onReceiveMessageFromFilter(filterId: String, message: Message) {
        synchronized(this) {
            this.mListener?.onReceiveMessageFromFilter(filterId, message)
        }
    }

    interface Listener {
        fun onReceiveMessageFromFilter(filterId: String, message: Message)
    }

    companion object {
        const val TAG = "DisplayProcessor"
    }
}