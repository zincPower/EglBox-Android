package com.jiangpengyong.eglbox_core.filter

import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.logger.Logger

/**
 * @author jiang peng yong
 * @date 2024/7/10 13:09
 * @email 56002982@qq.com
 * @des 滤镜处理节点
 */
abstract class GLFilter : Comparable<GLFilter> {
    var id = ""
    var name = ""
    var order = 0

    protected var mContext: FilterContext? = null

    open fun init(context: FilterContext) {
        if (isInit()) {
            Logger.e(TAG, "GLFilter has been initialized.")
            return
        }
        this.mContext = context
        onInit(context)
    }

    open fun draw(image: ImageInOut) {
        if (!isInit()) {
            Logger.e(TAG, "GLFilter hasn't initialized.")
            return
        }
        onDraw(mContext!!, image)
    }

    open fun release() {
        if (!isInit()) return
        mContext?.let { onRelease(it) }
        mContext = null
    }

    open fun updateData(filterId: String, updateData: Bundle) {
        if (!isInit()) return
        if (filterId == id) onUpdateData(updateData)
    }

    open fun storeData(): FilterData? {
        if (!isInit()) return null
        val data = Bundle()
        onStoreData(data)
        return FilterData(id, name, order, data)
    }

    open fun restoreData(inputData: FilterData) {
        if (!isInit()) return
        // 恢复数据以名字是否匹配为主，主要以逻辑能扣接为主
        // id 不比对，由外部进行逻辑组合
        if (inputData.name != name) return
        onRestoreData(inputData.data)
    }

    open fun receiveMessage(filterId: String, message: Message) {
        if (!isInit()) return
        if (filterId == id) onReceiveMessage(message)
    }

    fun isInit(): Boolean = mContext != null

    protected abstract fun onInit(context: FilterContext)
    protected abstract fun onDraw(context: FilterContext, imageInOut: ImageInOut)
    protected abstract fun onRelease(context: FilterContext)
    protected abstract fun onUpdateData(updateData: Bundle)
    protected abstract fun onRestoreData(inputData: Bundle)
    protected abstract fun onStoreData(outputData: Bundle)
    protected abstract fun onReceiveMessage(message: Message)

    override fun compareTo(other: GLFilter): Int {
        return this.order - other.order
    }

    override fun toString(): String {
        return "[GLFilter id=$id name=$name order=$order isInit=${isInit()}]"
    }

    companion object {
        private const val TAG = "GLFilter"
    }
}