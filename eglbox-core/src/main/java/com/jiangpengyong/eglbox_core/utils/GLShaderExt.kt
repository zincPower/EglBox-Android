package com.jiangpengyong.eglbox_core.utils

import android.content.res.Resources
import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.logger.Logger
import java.io.ByteArrayOutputStream

/**
 * @author jiang peng yong
 * @date 2024/2/9 14:46
 * @email 56002982@qq.com
 * @des 着色器工具
 */
object GLShaderExt {
    private const val TAG = "GLShaderExt"

    /**
     * 加载着色器
     * @param shaderType 着色器类型，可选值如下
     *                   顶点着色器 [GLES20.GL_VERTEX_SHADER]
     *                   片元着色器 [GLES20.GL_FRAGMENT_SHADER]
     * @param source 着色器代码
     * @return 着色器 id
     *         如果为 0 ，说明加载失败
     *         非 0 ，说明加载成功
     */
    fun loadShader(shaderType: Int, source: String): Int {
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
            if (compiled[0] != GLES20.GL_TRUE) {
                val shaderTip = if (shaderType == GLES20.GL_VERTEX_SHADER) "vertex shader" else if (shaderType == GLES20.GL_FRAGMENT_SHADER) "fragment shader" else "unknown shader"
                Logger.e(TAG, "Could not compile shader $shaderTip:\n ${GLES20.glGetShaderInfoLog(shader)}")
                // 异常情况，删除着色器
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    /**
     * 从 assets 的文件加载内容
     * @param resources 资源
     * @param filename 文件名
     * @return 文件内容
     */
    fun loadFromAssetsFile(resources: Resources, filename: String): String {
        var result: String? = null
        try {
            val inputStream = resources.assets.open(filename)
            val outputStream = ByteArrayOutputStream()
            var char: Int
            while ((inputStream.read().also { char = it }) != -1) {
                outputStream.write(char)
            }
            val byteArray = outputStream.toByteArray()
            outputStream.close()
            result = String(byteArray, Charsets.UTF_8)
            result = result.replace("\\r\\n", "\n")
        } catch (e: Exception) {
            Logger.e(TAG, "Load from assets file failure.", e)
        }
        return result ?: ""
    }

    /**
     * 从 assets 的文件加载着色器
     * @param resources 资源
     * @param shaderType 着色器类型，可选值如下
     *                   顶点着色器 [GLES20.GL_VERTEX_SHADER]
     *                   片元着色器 [GLES20.GL_FRAGMENT_SHADER]
     * @param filename 文件名
     * @return 着色器 id
     *         如果为 0 ，说明加载失败
     *         非 0 ，说明加载成功
     */
    fun loadShaderFromAssetsFile(
        resources: Resources,
        shaderType: Int,
        filename: String
    ): Int {
        return loadShader(shaderType, loadFromAssetsFile(resources, filename))
    }
}