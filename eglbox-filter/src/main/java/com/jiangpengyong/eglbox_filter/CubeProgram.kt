package com.jiangpengyong.eglbox_filter

import android.graphics.Color
import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import java.nio.FloatBuffer

/**
 * @author jiang peng yong
 * @date 2024/6/15 19:22
 * @email 56002982@qq.com
 * @des 绘制立方体
 */
class CubeProgram : GLProgram() {
    private val sideLength = 1F
    private val halfSideLength = sideLength / 2F
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            /**
             *        ᐱ +y
             *  1-----│-----0
             *  │     │     │
             *  │     │     │
             * -│----原点----│->+x
             *  │     │     │
             *  │     │     │
             *  2-----│-----3
             *        │
             */
            0F, 0F, 0F,
            halfSideLength, halfSideLength, 0F,     // 0
            -halfSideLength, halfSideLength, 0F,    // 1
            -halfSideLength, -halfSideLength, 0F,   // 2
            halfSideLength, -halfSideLength, 0F,    // 3
            halfSideLength, halfSideLength, 0F,     // 0
        )
    )
    private lateinit var mColorBuffer: FloatBuffer

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private val mVertexCount = 6

    private var mMatrix = GLMatrix()

    private var mFaceMatrix = ModelMatrix()

    init {
        calculateColor("#4E8CC3", "#FFFFFF")
    }

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    fun setColor(cornerColor: String, centerColor: String) {
        calculateColor(cornerColor, centerColor)
    }

    private fun calculateColor(cornerColor: String, centerColor: String) {
        val realCornerColor = try {
            Color.parseColor(cornerColor)
        } catch (e: Exception) {
            Logger.e(TAG, "SetColor failure. Corner color isn't a valid color.")
            return
        }
        val realCenterColor = try {
            Color.parseColor(centerColor)
        } catch (e: Exception) {
            Logger.e(TAG, "SetColor failure. Center color isn't a valid color.")
            return
        }

        val colors = FloatArray(mVertexCount * 4)
        colors[0] = Color.red(realCenterColor) / 255F
        colors[1] = Color.green(realCenterColor) / 255F
        colors[2] = Color.blue(realCenterColor) / 255F
        colors[3] = Color.alpha(realCenterColor) / 255F

        val cornerRed = Color.red(realCornerColor) / 255F
        val cornerGreen = Color.green(realCornerColor) / 255F
        val cornerBlue = Color.blue(realCornerColor) / 255F
        val cornerAlpha = Color.alpha(realCornerColor) / 255F
        for (i in 1 until mVertexCount) {
            colors[i * 4 + 0] = cornerRed
            colors[i * 4 + 1] = cornerGreen
            colors[i * 4 + 2] = cornerBlue
            colors[i * 4 + 3] = cornerAlpha
        }
        mColorBuffer = allocateFloatBuffer(colors)
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
        mColorHandle = getAttribLocation("aColor")
    }

    override fun onDraw() {
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mColorHandle)

        // 后乘规则
        mFaceMatrix.reset()
        mFaceMatrix.translate(0F, 0F, halfSideLength)
        drawFace(mFaceMatrix)

        mFaceMatrix.reset()
        mFaceMatrix.translate(0F, -halfSideLength, 0F)
        mFaceMatrix.rotate(90F, 1F, 0F, 0F)
        drawFace(mFaceMatrix)

        mFaceMatrix.reset()
        mFaceMatrix.translate(0F, 0F, -halfSideLength)
        mFaceMatrix.rotate(180F, 1F, 0F, 0F)
        drawFace(mFaceMatrix)

        mFaceMatrix.reset()
        mFaceMatrix.translate(0F, halfSideLength, 0F)
        mFaceMatrix.rotate(270F, 1F, 0F, 0F)
        drawFace(mFaceMatrix)

        mFaceMatrix.reset()
        mFaceMatrix.translate(halfSideLength, 0F, 0F)
        mFaceMatrix.rotate(90F, 0F, 1F, 0F)
        drawFace(mFaceMatrix)

        mFaceMatrix.reset()
        mFaceMatrix.translate(-halfSideLength, 0F, 0F)
        mFaceMatrix.rotate(270F, 0F, 1F, 0F)
        drawFace(mFaceMatrix)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
    }

    private fun drawFace(matrix: GLMatrix) {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, (mMatrix * matrix).matrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mVertexCount)
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

    companion object {
        private const val TAG = "CubeProgram"
    }
}