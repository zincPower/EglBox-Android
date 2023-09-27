package com.jiangpengyong.eglbox

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import com.jiangpengyong.eglbox.logger.Logger

data class EGLState(var display: EGLDisplay, var config: EGLConfig, var context: EGLContext?)

class EGL {

    fun init(): Boolean {
        val display = getDisplay() ?: return false

        eglInitialize(display)

        // 6. 创建Context
        val contextAttr = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,      // 使用版本为 2
            EGL14.EGL_NONE
        )
        mEglContext = EGL14.eglCreateContext(
            mEglDisplay,
            mEglConfig,
            EGL14.EGL_NO_CONTEXT,                       // 不共享
            contextAttr,
            0
        )
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            val error = EGL14.eglGetError()
            if (error == EGL14.EGL_BAD_CONFIG) {
                loge("Context create failure because of the bad config.")
            } else {
                loge("Context create failure.")
            }
            return false
        } else {
            logi("EglContext create success.")
        }
        return true
    }

    private fun getDisplay(): EGLDisplay? {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        return if (display == EGL14.EGL_NO_DISPLAY) {
            Logger.e("【EGL】Unable to connect window.")
            null
        } else {
            Logger.i("【EGL】Connect to window success.")
            display
        }
    }

    private fun eglInitialize(display: EGLDisplay): Boolean {
        val version = IntArray(2)
        val initRes = EGL14.eglInitialize(display, version, 0, version, 1)
        return if (initRes) {
            Logger.i("【EGL】Initialize EGL success. Version: ${version[0]}.${version[1]}.")
            true
        } else {
            val errorMsg = when (EGL14.eglGetError()) {
                EGL14.EGL_BAD_DISPLAY -> "EGL_BAD_DISPLAY"
                EGL14.EGL_NOT_INITIALIZED -> "EGL_NOT_INITIALIZED"
                else -> "UN_KNOW"
            }
            Logger.e("【EGL】Initialize EGL failure. Msg: $errorMsg.")
            false
        }
    }

    private fun chooseConfig(display: EGLDisplay, renderableType: Int): EGLConfig? {
        // 获取表面配置
        val attributeList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,                      // 颜色缓冲区 r 分量位数（单位：bits）- argb
            EGL14.EGL_GREEN_SIZE, 8,                    // 颜色缓冲区 g 分量位数（单位：bits）- argb
            EGL14.EGL_BLUE_SIZE, 8,                     // 颜色缓冲区 b 分量位数（单位：bits）- argb
            EGL14.EGL_ALPHA_SIZE, 8,                    // 颜色缓冲区 a 分量位数（单位：bits）- argb
//            EGL14.EGL_STENCIL_SIZE, 8,           // 模板缓冲区位数
//            EGL14.EGL_DEPTH_SIZE, 16,            // 深度缓冲区位数
            EGL14.EGL_RENDERABLE_TYPE, renderableType,  // 指定渲染 api 版本
            EGL14.EGL_NONE                              // EGL10.EGL_NONE 为结尾符
        )

        // 从系统中获取对应属性的配置
        val numConfig = IntArray(1)
        val configs: Array<EGLConfig?> = arrayOfNulls(1)
        val chooseResult = EGL14.eglChooseConfig(
            display,                            // 窗口
            attributeList,                      // 配置属性
            0,                // 配置属性存储偏移量
            configs,                            // 获取的配置属性
            0,                    // 获取的配置属性偏移量
            1,              // 获取配置属性量
            numConfig,                  // 想要获取的配置数量
            0          // 想要获取的配置数量的偏移量
        )

        return if (chooseResult) {
            Logger.i("【EGL】EglChooseConfig obtain success.")
            configs[0]
        } else {
            val errorMsg = when (EGL14.eglGetError()) {
                EGL14.EGL_NOT_INITIALIZED -> "EGL_NOT_INITIALIZED"
                EGL14.EGL_BAD_PARAMETER -> "EGL_BAD_PARAMETER"
                else -> "UN_KNOW"
            }
            Logger.e("【EGL】EglChooseConfig obtain failed. Msg: $errorMsg.")
            null
        }
    }

}