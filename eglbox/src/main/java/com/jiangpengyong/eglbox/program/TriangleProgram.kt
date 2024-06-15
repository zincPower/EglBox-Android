package com.jiangpengyong.eglbox.program

import android.opengl.GLES20
import com.jiangpengyong.eglbox.gles.GLProgram
import com.jiangpengyong.eglbox.utils.allocateFloatBuffer
import com.jiangpengyong.eglbox.utils.ModelMatrix

/**
 * @author jiang peng yong
 * @date 2024/6/15 13:05
 * @email 56002982@qq.com
 * @des 绘制三角形
 *    第一个点     第二个点
 *      红色       绿色
 *       ***********
 *        *********
 *         **原点**
 *          *****
 *           ***
 *            *
 *         第三个点
 *          蓝色
 */
class TriangleProgram : GLProgram() {
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            -0.5F, 0.5F, 0.0F,
            0.5F, 0.5F, 0.0F,
            0.0F, -0.5F, 0.0F
        )
    )
    private val mColorBuffer = allocateFloatBuffer(
        floatArrayOf(
            1F, 0F, 0F, 0F,
            0F, 1F, 0F, 0F,
            0F, 0F, 1F, 0F
        )
    )

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private val mVertexCount = 3

    private val mMatrix = ModelMatrix()

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
        mColorHandle = getAttribLocation("aColor")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
    }

    override fun onRelease() {
        mVertexBuffer.clear()
        mColorBuffer.clear()
        mMVPMatrixHandle = 0
        mPositionHandle = 0
        mColorHandle = 0
    }

    override fun getVertexShaderSource(): String = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        in vec3 aPosition;
        in vec4 aColor;
        out vec4 vColor;
        void main() {
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            vColor = aColor;
        }
    """.trimIndent()

    override fun getFragmentShaderSource(): String = """
        #version 300 es
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        void main() {
            fragColor = vColor;
        }
    """.trimIndent()
}