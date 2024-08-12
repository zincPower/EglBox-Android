package com.jiangpengyong.eglbox_core.processor.display

import android.os.Message
import com.jiangpengyong.eglbox_core.engine.GLEngine
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterChain
import com.jiangpengyong.eglbox_core.filter.GLFilterGroup
import com.jiangpengyong.eglbox_core.processor.GLProcessor
import com.jiangpengyong.eglbox_core.processor.listener.PreviewSurfaceListener

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

    private val mPreviewSurfaceListener: PreviewSurfaceListener? = null

    override fun onLaunch() {}

    override fun onDestroy() {

    }

    override fun createGLEngine(): GLEngine = GLEngine.createPBufferType()

    override fun getRenderType(): RenderType = RenderType.OnScreen

    override fun configFilterChain(filterChain: FilterChain) {

    }

    override fun onReceiveMessageFromFilter(filterId: String, message: Message) {

    }

    companion object {
        const val TAG = "DisplayProcessor"
    }
}