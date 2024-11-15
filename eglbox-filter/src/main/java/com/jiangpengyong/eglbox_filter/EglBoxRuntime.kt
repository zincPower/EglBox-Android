package com.jiangpengyong.eglbox_filter

import android.annotation.SuppressLint
import android.content.Context

/**
 * @author jiang peng yong
 * @date 2024/10/27 18:36
 * @email 56002982@qq.com
 * @des EglBox Filter
 */
@SuppressLint("StaticFieldLeak")
object EglBoxRuntime {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context
    }
}