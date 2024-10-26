package com.jiangpengyong.sample.view

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer

/**
 * @author jiang peng yong
 * @date 2024/10/24 08:14
 * @email 56002982@qq.com
 * @des 三角形滤镜
 */
class TriangleFilter : GLFilter() {
    private val mTriangleProgram = TriangleProgram()

    override fun onInit() {
        mTriangleProgram.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        imageInOut.texture?.let { texture ->
            val fbo = context.getTexFBO(texture.width, texture.height)
            fbo.use {
                context.texture2DProgram.reset()
                context.texture2DProgram.setTexture(texture)
                context.texture2DProgram.draw()
                mTriangleProgram.draw()
            }
            imageInOut.out(fbo)
        }
    }

    override fun onRelease() {
        mTriangleProgram.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object{
        const val TAG = "TriangleFilter"
    }
}

/**
 * @author jiang peng yong
 * @date 2024/6/15 13:05
 * @email 56002982@qq.com
 * @des 绘制三角形程序
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
            1F, 0F, 0F, 1F,
            0F, 1F, 0F, 1F,
            0F, 0F, 1F, 1F
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