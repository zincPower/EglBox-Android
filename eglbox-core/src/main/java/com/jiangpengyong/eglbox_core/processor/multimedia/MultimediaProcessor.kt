package com.jiangpengyong.eglbox_core.processor.multimedia

import android.os.Message
import com.jiangpengyong.eglbox_core.engine.GLEngine
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterChain
import com.jiangpengyong.eglbox_core.processor.GLProcessor

class MultimediaProcessor : GLProcessor() {
    override fun onLaunch() {}
    override fun onDestroy() {}

    override fun createGLEngine(): GLEngine = GLEngine.createPBufferType()

    override fun getRenderType(): RenderType = RenderType.OnScreen

    override fun configFilterChain(filterChain: FilterChain) {

    }

    override fun onReceiveMessageFromFilter(filterId: String, message: Message) {

    }
}