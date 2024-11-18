package com.jiangpengyong.sample.h_blend

import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:48
 * @email 56002982@qq.com
 * @des 混合滤镜
 */
class BlendFilter: GLFilter() {
    val mSceneFilter = BlendSceneFilter()

    override fun onInit(context: FilterContext) {
        mContext?.let { mSceneFilter.init(it) }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {

    }

    override fun onRelease(context: FilterContext) {
    }

    override fun onUpdateData(updateData: Bundle) {
    }

    override fun onRestoreData(inputData: Bundle) {
    }

    override fun onStoreData(outputData: Bundle) {
    }

    override fun onReceiveMessage(message: Message) {
    }

}

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:46
 * @email 56002982@qq.com
 * @des 混合场景滤镜
 */
class BlendSceneFilter: GLFilter() {
    override fun onInit(context: FilterContext) {
        TODO("Not yet implemented")
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        TODO("Not yet implemented")
    }

    override fun onRelease(context: FilterContext) {
        TODO("Not yet implemented")
    }

    override fun onUpdateData(updateData: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onRestoreData(inputData: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onStoreData(outputData: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onReceiveMessage(message: Message) {
        TODO("Not yet implemented")
    }
}