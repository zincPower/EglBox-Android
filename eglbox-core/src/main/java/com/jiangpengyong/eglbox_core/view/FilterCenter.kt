package com.jiangpengyong.eglbox_core.view

import com.jiangpengyong.eglbox_core.filter.GLFilter

/**
 * @author: jiang peng yong
 * @date: 2024/8/16 13:10
 * @email: 56002982@qq.com
 * @desc: 滤镜注册中心
 */
object FilterCenter {
    private val filterMap = HashMap<String, Class<out GLFilter>>()

    fun <T : GLFilter> registerFilter(name: String, clazz: Class<T>) = synchronized(this) {
        filterMap.put(name, clazz)
    }

    fun createFilter(name: String): GLFilter? = synchronized(this) {
        return filterMap[name]?.newInstance()
    }
}