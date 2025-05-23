package com.jiangpengyong.sample.utils

import android.content.res.Resources

object SizeUtils {
    fun dp2px(dpValue: Float): Float {
        val scale = Resources.getSystem().displayMetrics.density
        return dpValue * scale + 0.5f
    }
}