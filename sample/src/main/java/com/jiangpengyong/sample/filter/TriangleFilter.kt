package com.jiangpengyong.sample.filter

import android.opengl.GLES20
import android.opengl.GLES30
import com.jiangpengyong.eglbox.GLProgram
import com.jiangpengyong.eglbox.allocateFloatBuffer
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.GLFilter.Companion.NOT_INIT
import com.jiangpengyong.eglbox.utils.MVPMatrix
import java.nio.FloatBuffer

/**
 * @author jiang peng yong
 * @date 2024/2/11 12:36
 * @email 56002982@qq.com
 * @des 三角形的效果
 */
class TriangleFilter : GLFilter {

    private val mProgram = GLProgram()
    private val mMVPMatrix = MVPMatrix()

    private var mMVPMatrixHandle = NOT_INIT
    private var mPositionHandle = NOT_INIT
    private var mColorHandle = NOT_INIT

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mColorBuffer: FloatBuffer

    private val mVertexCount = 3

    override fun init(context: FilterContext) {
        mProgram.create(vertex, fragment)
        if (!mProgram.isInit()) return
        mMVPMatrixHandle = mProgram.getUniformLocation("uMVPMatrix")
        mPositionHandle = mProgram.getAttribLocation("aPosition")
        mColorHandle = mProgram.getAttribLocation("aColor")
        mMVPMatrix.reset()
        mVertexBuffer = allocateFloatBuffer(
            floatArrayOf(
                -0.5F, 0.0F, 0.0F,
                0.5F, 0.0F, 0.0F,
                0.0F, -0.5F, 0.0F
            )
        )
        mColorBuffer = allocateFloatBuffer(
            floatArrayOf(
                1F, 0F, 0F, 0F,
                0F, 1F, 0F, 0F,
                0F, 0F, 1F, 0F
            )
        )
        mMVPMatrix.setLookAtM(
            0F, 0F, 10F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    override fun updateSize(context: FilterContext) {
        if (!context.isValidSize()) return
        val ratio = context.width.toFloat() / context.height
        if (ratio > 1.0) {      // 横图
            mMVPMatrix.setFrustumM(
                -ratio, ratio,
                -1F, 1F,
                5F, 10F
            )
        } else {                // 方图、竖图
            mMVPMatrix.setFrustumM(
                -1F, 1F,
                -ratio, ratio,
                5F, 10F
            )
        }
    }

    override fun render(context: FilterContext) {
        mProgram.bind()
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.getMVPMatrix(), 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
        mProgram.unbind()
    }

    override fun release() {
        mProgram.release()
        mVertexBuffer.clear()
        mColorBuffer.clear()
        mMVPMatrixHandle = NOT_INIT
        mPositionHandle = NOT_INIT
        mColorHandle = NOT_INIT
    }

    companion object {
        // 三角形的顶点着色器
        val vertex = """
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

        // 三角形的片元着色器
        val fragment = """
            #version 300 es
            precision mediump float;
            in vec4 vColor;
            out vec4 fragColor;
            void main() {
                fragColor = vColor;
            }
        """.trimIndent()
    }
}