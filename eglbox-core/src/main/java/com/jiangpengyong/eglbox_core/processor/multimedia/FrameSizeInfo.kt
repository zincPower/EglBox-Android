package com.jiangpengyong.eglbox_core.processor.multimedia

import android.util.Size
import com.jiangpengyong.eglbox_core.program.isValid

/**
 * @author jiang peng yong
 * @date 2024/11/1 12:58
 * @email 56002982@qq.com
 * @des
 */
data class FrameSizeInfo(
    val sourceSize: Size,
    val targetSize: Size,
) {
    fun isValid(): Boolean {
        return sourceSize.isValid() && targetSize.isValid()
    }
}