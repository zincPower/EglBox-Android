package com.jiangpengyong.eglbox_core.filter

import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.logger.Logger

/**
 * @author jiang peng yong
 * @date 2024/7/10 13:09
 * @email 56002982@qq.com
 * @des 滤镜组
 */
class GLFilterGroup : GLFilter() {
    private val mFilters = mutableListOf<GLFilter>()

    override fun init(context: FilterContext) {
        super.init(context)
        for (item in mFilters) {
            item.init(context)
        }
    }

    override fun draw(image: ImageInOut) {
        if (!isInit()) return
        super.draw(image)
        for (item in mFilters) {
            item.draw(image)
        }
    }

    override fun release() {
        if (!isInit()) return
        for (item in mFilters) {
            item.release()
        }
        mFilters.clear()
        super.release()
    }

    override fun updateData(filterId: String, updateData: Bundle) {
        if (!isInit()) return
        super.updateData(filterId, updateData)
        for (item in mFilters) {
            item.updateData(filterId, updateData)
        }
    }

    override fun storeData(): FilterData? {
        if (!isInit()) return null
        val data = super.storeData()
        if (data == null) {
            Logger.e(TAG, "Filter data is nullptr.")
            return null
        }
        for (item in mFilters) {
            val itemData = item.storeData()
            if (itemData != null) data.children.add(itemData)
        }
        return data
    }

    override fun restoreData(inputData: FilterData) {
        if (!isInit()) return
        // 先恢复自身
        super.restoreData(inputData)
        // 恢复子滤镜
        // 子滤镜的长度和恢复数据必须长度一样，因为有可能相同名称的滤镜在一个滤镜组中添加了多个
        // 滤镜 id 在不同的滤镜链中可能不一样，所以不能进行作为唯一标识
        // 所以按照滤镜数据的组装顺序进行数据恢复，使用原则则交由外部进行
        val childData = inputData.children
        if (childData.size != mFilters.size) {
            Logger.e(TAG, "FilterData 和 Filter 的长度不同，滤镜链有问题。ChildData size=${childData.size}, Filter size=${mFilters.size}")
            return
        }
        mFilters.forEachIndexed { index, item ->
            item.restoreData(childData[index])
        }
    }

    override fun receiveMessage(filterId: String, message: Message) {
        if (!isInit()) return
        if (filterId == id) super.receiveMessage(filterId, message)
        for (item in mFilters) {
            item.receiveMessage(filterId, message)
        }
    }

    fun addFilter(filter: GLFilter) {
        Logger.i(TAG, "FilterGroup id=${id} addFilter filter=${filter}")
        if (isInit()) filter.init(mContext!!)
        mFilters.add(filter)
        mFilters.sort()
    }

    fun removeFilter(filterId: String) {
        Logger.i(TAG, "FilterGroup id=${id}, start removeFilter id=${filterId}")
        val iterator = mFilters.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.id == filterId) {
                Logger.i(TAG, "FilterGroup id=${id}, found and removeFilter id=${id}")
                iterator.remove()
                item.release()
            } else {
                if (item is GLFilterGroup) {
                    item.removeFilter(filterId)
                }
            }
        }
        Logger.i(TAG, "FilterGroup id=${id}, finish removeFilter id=${filterId}")
    }

    fun removeAllFilters() {
        for (item in mFilters) {
            item.release()
        }
        mFilters.clear()
    }

    fun size() = mFilters.size

    operator fun get(index: Int): GLFilter? {
        return if (index >= mFilters.size) {
            null
        } else {
            mFilters[index]
        }
    }

    override fun onInit(context: FilterContext) {
        Logger.i(TAG, "onInit")
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
//        Logger.i(TAG, "onDraw")
    }

    override fun onRelease(context: FilterContext) {
        Logger.i(TAG, "onRelease")
    }

    override fun onUpdateData(updateData: Bundle) {
        Logger.i(TAG, "onUpdateData")
    }

    override fun onRestoreData(inputData: Bundle) {
        Logger.i(TAG, "onRestoreData")
    }

    override fun onStoreData(outputData: Bundle) {
        Logger.i(TAG, "onStoreData")
    }

    override fun onReceiveMessage(message: Message) {
        Logger.i(TAG, "onReceiveMessage message=$message")
    }

    override fun toString(): String {
        return "[GLFilterGroup id=$id name=$name order=$order isInit=${isInit()} child=${mFilters.size}]"
    }

    companion object {
        private const val TAG = "GLFilterGroup"
    }
}