package com.jiangpengyong.eglbox.gles

/**
 * @author jiang peng yong
 * @date 2024/6/9 15:57
 * @email 56002982@qq.com
 * @des GL 对象基类
 */
interface GLObject {
    fun init()
    fun release()
    fun isInit(): Boolean
}