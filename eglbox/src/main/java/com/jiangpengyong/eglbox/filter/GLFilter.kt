package com.jiangpengyong.eglbox.filter

import android.os.Bundle
import com.jiangpengyong.eglbox.logger.Logger

/**
 * @author jiang peng yong
 * @date 2024/2/11 21:55
 * @email 56002982@qq.com
 * @des OpenGL 滤镜链
 */
abstract class GLFilter {
    private val TAG = "GLFilter"
    protected var mContext: FilterContext? = null

    fun init(context: FilterContext) {
        if (isInit()) {
            Logger.e(TAG, "GLFilter has been initialized.")
            return
        }
        this.mContext = context
        onInit()
    }

    fun draw(imageInOut: ImageInOut) {
        if (!isInit()) {
            Logger.e(TAG, "GLFilter hasn't initialized.")
            return
        }
        onDraw(mContext!!, imageInOut)
    }

    fun release() {
        if (!isInit()) return
        onRelease()
        mContext = null
    }

    fun updateData(inputData: Bundle) {
        if (!isInit()) return
        onUpdateData(inputData)
    }

    fun restoreData(restoreData: Bundle) {
        if (!isInit()) return
        onRestoreData(restoreData)
    }

    fun saveData(saveData: Bundle) {
        if (!isInit()) return
        onSaveData(saveData)
    }

    fun isInit(): Boolean = mContext != null

    protected abstract fun onInit()
    protected abstract fun onDraw(context: FilterContext, imageInOut: ImageInOut)
    protected abstract fun onRelease()
    protected abstract fun onUpdateData(inputData: Bundle)
    protected abstract fun onRestoreData(restoreData: Bundle)
    protected abstract fun onSaveData(saveData: Bundle)
}