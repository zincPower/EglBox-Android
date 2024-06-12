package com.jiangpengyong.eglbox.logger

import android.util.Log


/**
 * @author: jiang peng yong
 * @date: 2023/8/31 19:56
 * @email: 56002982@qq.com
 * @desc: 日志
 */
object Logger {
    private const val TAG = "EglBox"
    private var SHOW = true

    fun i(tag: String, msg: String) {
        if (!SHOW) return
        Log.i("【${TAG}-${tag}】", "【Thread: ${Thread.currentThread()}】 $msg")
    }

    fun d(tag: String, msg: String) {
        if (!SHOW) return
        Log.d("【${TAG}-${tag}】", "【Thread: ${Thread.currentThread()}】 $msg")
    }

    fun w(tag: String, msg: String) {
        if (!SHOW) return
        Log.w("【${TAG}-${tag}】", "【Thread: ${Thread.currentThread()}】 $msg")
    }

    fun e(tag: String, msg: String) {
        if (!SHOW) return
        Log.e("【${TAG}-${tag}】", "【Thread: ${Thread.currentThread()}】 $msg")
    }

    fun e(tag: String, msg: String, e: Throwable) {
        if (!SHOW) return
        Log.e("【${TAG}-${tag}】", "【Thread: ${Thread.currentThread()}】 $msg", e)
    }
}