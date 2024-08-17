package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadShader

/**
 * @author jiang peng yong
 * @date 2024/2/9 14:46
 * @email 56002982@qq.com
 * @des GL 程序
 */
abstract class GLProgram : GLObject {
    var id: Int = 0
        private set

    private var mVertexShader: Int = 0
    private var mFragmentShader: Int = 0

    override fun init() {
        if (isInit()) {
            Logger.e(TAG, "Program has been initialized. 【init】id=$id")
            return
        }
        createProgram()
        if (id != 0) onInit()
    }

    fun draw() {
        if (isInit()) {
            GLES20.glUseProgram(id)
            onDraw()
            GLES20.glUseProgram(0)
        } else {
            Logger.e(TAG, "Program hasn't initialized. 【draw】")
        }
    }

    override fun release() {
        if (isInit()) {
            onRelease()
            val currentProgram = EGLBox.getCurrentProgram()
            if (currentProgram == id) GLES20.glUseProgram(0)
            releaseResource()
        }
    }

    override fun isInit(): Boolean = (id != 0)

    protected abstract fun onInit()
    protected abstract fun onDraw()
    protected abstract fun onRelease()
    protected abstract fun getVertexShaderSource(): String
    protected abstract fun getFragmentShaderSource(): String

    fun bind() {
        if (!isInit()) {
            Logger.e(TAG, "Program id is invalid. Please call create method first.")
            return
        }
        GLES20.glUseProgram(id)
    }

    fun unbind() {
        GLES20.glUseProgram(0)
    }

    protected fun getUniformLocation(uniformName: String): Int {
        if (!isInit()) {
            Logger.e(TAG, "Program isn't initialized. Please call init function first. uniformName=$uniformName")
            return 0
        }
        return GLES20.glGetUniformLocation(id, uniformName)
    }

    protected fun getAttribLocation(attributeName: String): Int {
        if (!isInit()) {
            Logger.e(TAG, "Program isn't initialized. Please call init function first. attributeName=$attributeName")
            return 0
        }
        return GLES20.glGetAttribLocation(id, attributeName)
    }

    private fun createProgram() {
        val vertexShaderSource = getVertexShaderSource()
        if (vertexShaderSource.isEmpty()) {
            Logger.e(TAG, "VertexShaderSource is empty.")
            return
        }
        val fragmentShaderSource = getFragmentShaderSource()
        if (fragmentShaderSource.isEmpty()) {
            Logger.e(TAG, "FragmentShaderSource is empty.")
            return
        }

        // 加载顶点着色器
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        if (mVertexShader == 0) {
            Logger.e(TAG, "Vertex shader load failure. ")
            releaseResource()
            return
        }
        // 加载片元着色器
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        if (mFragmentShader == 0) {
            Logger.e(TAG, "Fragment shader load failure. VertexShader=$mVertexShader")
            releaseResource()
            return
        }

        // 创建程序
        id = GLES20.glCreateProgram()
        if (id == 0) {
            Logger.e(TAG, "Create program failure.")
            releaseResource()
        } else {  // 若程序创建成功则向程序中加入顶点着色器与片元着色器
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
                Logger.e(TAG, "Link program failure. id=$id, errorLog= \n ${GLES20.glGetProgramInfoLog(id)}")
                releaseResource()
            } else {
                Logger.i(TAG, "Create program success. id=$id")
            }
        }
    }

    private fun releaseResource() {
        Logger.i(TAG, "Release program. ProgramId=$id, VertexShaderId=$mVertexShader, FragmentShaderId=$mFragmentShader")
        if (mVertexShader != 0) {
            if (id != 0) GLES20.glDetachShader(id, mVertexShader)
            GLES20.glDeleteShader(mVertexShader)
            mVertexShader = 0
        }
        if (mFragmentShader != 0) {
            if (id != 0) GLES20.glDetachShader(id, mFragmentShader)
            GLES20.glDeleteShader(mFragmentShader)
            mFragmentShader = 0
        }
        if (id != 0) {
            GLES20.glDeleteProgram(id)
            id = 0
        }
    }

    companion object{
        private const val TAG = "GLProgram"
    }
}