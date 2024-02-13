package com.jiangpengyong.eglbox.utils

import android.opengl.GLES30
import com.jiangpengyong.eglbox.logger.Logger

/**
 * @author jiang peng yong
 * @date 2024/2/9 13:41
 * @email 56002982@qq.com
 * @des GL 检查错误工具
 */
class GLErrorUtil {

    /**
     * 输出 gl 目前已存在的错误
     * @param optionName 操作名称，用于日志输出
     */
    fun checkError(optionName: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Logger.e("$optionName: GL Error 0x${Integer.toHexString(error)}")
        }
    }

}