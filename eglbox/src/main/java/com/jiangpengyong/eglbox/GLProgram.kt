package com.jiangpengyong.eglbox

import android.opengl.GLES20
import com.jiangpengyong.eglbox.logger.Logger
import com.jiangpengyong.eglbox.utils.GLShaderUtil.loadShader

/**
 * @author jiang peng yong
 * @date 2024/2/9 14:46
 * @email 56002982@qq.com
 * @des GL 程序
 */
class GLProgram {

    companion object {
        private const val NOT_INIT = 0
    }

    var id: Int = NOT_INIT
        private set

    private var mVertexShader: Int = NOT_INIT
    private var mFragmentShader: Int = NOT_INIT

    fun create(vertexShaderSource: String, fragmentShaderSource: String) {
        if (isInit()) {
            Logger.e("Program had init.[$id]")
            return
        }
        if (vertexShaderSource.isEmpty()) {
            Logger.e("VertexShaderSource is empty")
            return
        }
        if (fragmentShaderSource.isEmpty()) {
            Logger.e("FragmentShaderSource is empty")
            return
        }

        // 加载顶点着色器
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        if (mVertexShader == NOT_INIT) {
            Logger.e("Vertex shader load failure.[$mVertexShader]")
            return
        }

        // 加载片元着色器
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        if (mFragmentShader == NOT_INIT) {
            Logger.e("Fragment shader load failure.[$mVertexShader]")
            GLES20.glDeleteShader(mVertexShader)
            mVertexShader = NOT_INIT
            return
        }

        // 创建程序
        id = GLES20.glCreateProgram()
        // 若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (id != NOT_INIT) {
            // 向程序中加入顶点着色器
            GLES20.glAttachShader(id, mVertexShader)
            // 向程序中加入片元着色器
            GLES20.glAttachShader(id, mFragmentShader)
            // 链接程序
            GLES20.glLinkProgram(id)
            // 存放链接成功 program 数量的数组
            val linkStatus = IntArray(1)
            // 获取 program 的链接情况
            GLES20.glGetProgramiv(id, GLES20.GL_LINK_STATUS, linkStatus, 0)
            // 若链接失败则报错并删除程序
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Logger.e("Could not link program: \n ${GLES20.glGetProgramInfoLog(id)}")
                GLES20.glDeleteProgram(id)
                id = NOT_INIT
            }
        }
        Logger.e("Create program. [$id]")
    }

    fun bind() {
        if (!isInit()) {
            Logger.e("Program id is invalid.Please call create method first. [$id]")
            return
        }
        GLES20.glUseProgram(id)
    }

    fun unbind() {
        GLES20.glUseProgram(0)
    }

    fun isInit(): Boolean = (id != NOT_INIT)

    fun release() {
        if (!isInit()) return
        unbind()
        if (mVertexShader != NOT_INIT) {
            GLES20.glDetachShader(id, mVertexShader)
            mVertexShader = NOT_INIT
        }
        if (mFragmentShader != NOT_INIT) {
            GLES20.glDetachShader(id, mFragmentShader)
            mFragmentShader = NOT_INIT
        }
        GLES20.glDeleteProgram(id)
        Logger.i("Release program. [$id]")

        id = NOT_INIT
    }

    fun getUniformLocation(uniformName: String): Int {
        if (!isInit()) {
            Logger.e("Program id is invalid.Please call createProgram function first. [$id]")
            return NOT_INIT
        }
        return GLES20.glGetUniformLocation(id, uniformName)
    }

    fun getAttribLocation(attributeName: String): Int {
        if (!isInit()) {
            Logger.e("Program id is invalid.Please call createProgram function first. [$id]")
            return NOT_INIT
        }
        return GLES20.glGetAttribLocation(id, attributeName)
    }
}