package com.jiangpengyong.eglbox_core.processor.offscreen

import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.filter.SinkFilter

class OffscreenSinkFilter: SinkFilter() {
    override fun onInit() {}

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {}

    override fun onRelease() {}

    override fun onUpdateData(updateData: Bundle) {}

    override fun onRestoreData(inputData: Bundle) {}

    override fun onStoreData(outputData: Bundle) {}

    override fun onReceiveMessage(message: Message) {}
}