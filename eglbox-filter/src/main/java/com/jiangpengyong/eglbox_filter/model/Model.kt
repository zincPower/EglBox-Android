package com.jiangpengyong.eglbox_filter.model

/**
 * @author jiang peng yong
 * @date 2024/11/25 21:57
 * @email 56002982@qq.com
 * @des 模型数据
 */
abstract class Model {
    private var mModelData: ModelData? = null

    fun create(): ModelData = mModelData ?: onCreate().apply { mModelData = this }

    abstract fun onCreate(): ModelData
}