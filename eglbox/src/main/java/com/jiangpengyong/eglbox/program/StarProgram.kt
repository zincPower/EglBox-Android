package com.jiangpengyong.eglbox.program

import android.graphics.Color
import android.opengl.GLES20
import com.jiangpengyong.eglbox.gles.GLProgram
import com.jiangpengyong.eglbox.utils.GLMatrix
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.allocateFloatBuffer
import java.lang.Math.toRadians
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
    private val radius = 1F
    private val ratio = radius * 0.382F
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            radius * sin(0.toRadians()).toFloat(), radius * cos(0.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(36.toRadians()).toFloat(), ratio * cos(36.toRadians()).toFloat(), 0F,

            ratio * sin(36.toRadians()).toFloat(), ratio * cos(36.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(72.toRadians()).toFloat(), radius * cos(72.toRadians()).toFloat(), 0F,

            radius * sin(72.toRadians()).toFloat(), radius * cos(72.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(108.toRadians()).toFloat(), ratio * cos(108.toRadians()).toFloat(), 0F,

            ratio * sin(108.toRadians()).toFloat(), ratio * cos(108.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(144.toRadians()).toFloat(), radius * cos(144.toRadians()).toFloat(), 0F,

            radius * sin(144.toRadians()).toFloat(), radius * cos(144.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(180.toRadians()).toFloat(), ratio * cos(180.toRadians()).toFloat(), 0F,

            ratio * sin(180.toRadians()).toFloat(), ratio * cos(180.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(216.toRadians()).toFloat(), radius * cos(216.toRadians()).toFloat(), 0F,

            radius * sin(216.toRadians()).toFloat(), radius * cos(216.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(252.toRadians()).toFloat(), ratio * cos(252.toRadians()).toFloat(), 0F,

            ratio * sin(252.toRadians()).toFloat(), ratio * cos(252.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(288.toRadians()).toFloat(), radius * cos(288.toRadians()).toFloat(), 0F,

            radius * sin(288.toRadians()).toFloat(), radius * cos(288.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(324.toRadians()).toFloat(), ratio * cos(324.toRadians()).toFloat(), 0F,

            ratio * sin(324.toRadians()).toFloat(), ratio * cos(324.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(0.toRadians()).toFloat(), radius * cos(0.toRadians()).toFloat(), 0F,
        )
    )
    private var mColorBuffer = allocateFloatBuffer(
        floatArrayOf(
            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,
        )
    )

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private val mVertexCount = 30
    private var mMatrix: GLMatrix = GLMatrix()

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    fun setColor(red: Float, green: Float, blue: Float, alpha: Float) {
        val colors = FloatArray(mVertexCount * 4)
        for (i in 0 until (mVertexCount / 3)) {
            colors[i * 12 + 0] = red
            colors[i * 12 + 1] = green
            colors[i * 12 + 2] = blue
            colors[i * 12 + 3] = alpha

            colors[i * 12 + 4] = max(red + 0.2F, 0F)
            colors[i * 12 + 5] = max(green + 0.2F, 0F)
            colors[i * 12 + 6] = max(blue + 0.2F, 0F)
            colors[i * 12 + 7] = alpha

            colors[i * 12 + 8] = red
            colors[i * 12 + 9] = green
            colors[i * 12 + 10] = blue
            colors[i * 12 + 11] = alpha
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

fun Double.toRadians(): Double {
    return Math.toRadians(this)
}

fun Float.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}

fun Int.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}