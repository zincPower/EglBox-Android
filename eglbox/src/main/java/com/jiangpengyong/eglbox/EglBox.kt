package com.jiangpengyong.eglbox

import android.opengl.GLES30
import com.jiangpengyong.eglbox.logger.Logger


/**
 * @author: jiang peng yong
 * @date: 2023/9/6 17:10
 * @email: 56002982@qq.com
 * @desc:
 */
object EglBox {

    fun checkGLError(opName: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Logger.e("$opName: GL Error 0x${Integer.toHexString(error)}")
        }
    }

}