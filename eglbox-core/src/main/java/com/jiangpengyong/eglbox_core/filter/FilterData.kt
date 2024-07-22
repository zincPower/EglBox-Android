package com.jiangpengyong.eglbox_core.filter

import android.os.Bundle

/**
 * @author jiang peng yong
 * @date 2024/7/11 13:12
 * @email 56002982@qq.com
 * @des 滤镜数据
 */
data class FilterData(
    val id: String,
    val name: String,
    val order: Int,
    val data: Bundle,
    val children: ArrayList<FilterData> = ArrayList()
)