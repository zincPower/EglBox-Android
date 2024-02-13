package com.jiangpengyong.eglbox.filter

import com.jiangpengyong.eglbox.GLThread

/**
 * @author jiang peng yong
 * @date 2024/2/11 21:55
 * @email 56002982@qq.com
 * @des OpenGL 滤镜链
 */
interface GLFilter {
    companion object {
        const val NOT_INIT = -1
    }

    @GLThread
    fun init(context: FilterContext)

    fun updateSize(context: FilterContext)

    @GLThread
    fun render(context: FilterContext)

    @GLThread
    fun release()
}