package com.jiangpengyong.eglbox.program

import android.graphics.Color
import android.opengl.GLES20
import android.util.Size
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.gles.GLProgram
import com.jiangpengyong.eglbox.logger.Logger
import com.jiangpengyong.eglbox.utils.GLMatrix
import com.jiangpengyong.eglbox.utils.allocateFloatBuffer
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/6/15 14:35
 * @email 56002982@qq.com
 * @des 绘制五角星
 */
class StarProgram : GLProgram() {
    private val mOuterRadius = 1F
    private val mInnerRadius = mOuterRadius * 0.382F
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            0F, 0F, 0F,
            mOuterRadius * sin(0.toRadians()).toFloat(), mOuterRadius * cos(0.toRadians()).toFloat(), 0F,
            mInnerRadius * sin(36.toRadians()).toFloat(), mInnerRadius * cos(36.toRadians()).toFloat(), 0F,
            mOuterRadius * sin(72.toRadians()).toFloat(), mOuterRadius * cos(72.toRadians()).toFloat(), 0F,
            mInnerRadius * sin(108.toRadians()).toFloat(), mInnerRadius * cos(108.toRadians()).toFloat(), 0F,
            mOuterRadius * sin(144.toRadians()).toFloat(), mOuterRadius * cos(144.toRadians()).toFloat(), 0F,
            mInnerRadius * sin(180.toRadians()).toFloat(), mInnerRadius * cos(180.toRadians()).toFloat(), 0F,
            mOuterRadius * sin(216.toRadians()).toFloat(), mOuterRadius * cos(216.toRadians()).toFloat(), 0F,
            mInnerRadius * sin(252.toRadians()).toFloat(), mInnerRadius * cos(252.toRadians()).toFloat(), 0F,
            mOuterRadius * sin(288.toRadians()).toFloat(), mOuterRadius * cos(288.toRadians()).toFloat(), 0F,
            mInnerRadius * sin(324.toRadians()).toFloat(), mInnerRadius * cos(324.toRadians()).toFloat(), 0F,
            mOuterRadius * sin(0.toRadians()).toFloat(), mOuterRadius * cos(0.toRadians()).toFloat(), 0F,
        )
    )
    private var mColorBuffer = allocateFloatBuffer(
        floatArrayOf(
            1F, 1F, 1F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
        )
    )

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private val mVertexCount = 12
    private var mMatrix: GLMatrix = GLMatrix()

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    fun setColor(cornerColor: String, centerColor: String) {
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
        for (i in 1 until 12) {
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
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mVertexCount)
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

    private fun Int.toRadians(): Double {
        return Math.toRadians(this.toDouble())
    }

    companion object {
        private const val TAG = "StarProgram"
    }
}
