package com.jiangpengyong.eglbox

import android.opengl.GLES20
import com.jiangpengyong.eglbox.logger.Logger

/**
 * @author: jiang peng yong
 * @date: 2023/8/31 20:57
 * @email: 56002982@qq.com
 * @desc: 程序
 */
class Program {

    companion object {
        private const val NOT_INIT = -1
    }

    var id: Int = NOT_INIT
        private set
    private var mVertexShader: Int = NOT_INIT
    private var mFragmentShader: Int = NOT_INIT

    /**
     * 是否初始化
     * @return true：已经初始化
     *         false：未初始化
     */
    fun isInit(): Boolean = (id != NOT_INIT)

    /**
     * 创建程序
     * @param vertexShaderSource 顶点着色器
     * @param fragmentShaderSource 片元着色器
     */
    fun createProgram(
        vertexShaderSource: String,
        fragmentShaderSource: String
    ) {
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
            Logger.e("Vertex shader loader failure.[$mVertexShader]")
            return
        }

        // 加载片元着色器
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        if (mFragmentShader == NOT_INIT) {
            Logger.e("Fragment shader loader failure.[$mVertexShader]")
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
    }

    fun getUniformLocation(attributeName: String): Int {
        if (!isInit()) {
            Logger.e("Program id is invalid.Please call createProgram function first. [$id]")
            return NOT_INIT
        }
        return GLES20.glGetUniformLocation(id, attributeName)
    }

    fun getAttribLocation(attributeName: String): Int {
        if (!isInit()) {
            Logger.e("Program id is invalid.Please call createProgram function first. [$id]")
            return NOT_INIT
        }
        return GLES20.glGetAttribLocation(id, attributeName)
    }

    /**
     * 使用程序
     */
    fun useProgram() {
        if (!isInit()) {
            Logger.e("Program id is invalid.Please call createProgram function first. [$id]")
            return
        }
        GLES20.glUseProgram(id)
    }

    /**
     * 释放
     */
    fun release() {
        if (!isInit()) {
            return
        }

        if (mVertexShader != NOT_INIT) {
            GLES20.glDetachShader(id, mVertexShader)
            mVertexShader = NOT_INIT
        }
        if (mFragmentShader != NOT_INIT) {
            GLES20.glDetachShader(id, mFragmentShader)
            mFragmentShader = NOT_INIT
        }
        GLES20.glUseProgram(0)
        GLES20.glDeleteProgram(id)
        id = NOT_INIT
    }

    /**
     * 加载 shader 方法
     * @param shaderType shader 的类型 GLES20.GL_VERTEX_SHADER GLES20.GL_FRAGMENT_SHADER
     * @param source shader 的脚本字符串
     */
    private fun loadShader(
        shaderType: Int,
        source: String
    ): Int {
        // 创建一个新 shader
        var shader = GLES20.glCreateShader(shaderType)
        // 若创建成功则加载 shader
        if (shader != 0) {
            // 加载 shader 的源代码
            GLES20.glShaderSource(shader, source)
            // 编译 shader
            GLES20.glCompileShader(shader)
            // 存放编译成功 shader 数量的数组
            val compiled = IntArray(1)
            // 获取 shader 的编译情况
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            // 若编译失败则显示错误日志并删除此 shader
            if (compiled[0] == 0) {
                Logger.e("Could not compile shader $shaderType:\n ${GLES20.glGetShaderInfoLog(shader)}")
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

}