package com.jiangpengyong.sample.view

import android.graphics.Color
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_core.utils.allocateByteBuffer
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class StarFilter : GLFilter() {
    private val mStarProgram = StarProgram()
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()
    private var mPreviewSize = Size(0, 0)

    override fun onInit() {
        mStarProgram.init()
        mViewMatrix.setLookAtM(
            0F, 0F, 5F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
        mModelMatrix.scale(0.8F, 0.8F, 1F)
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        imageInOut.texture?.let { texture ->
            updateProjectionMatrix(Size(texture.width, texture.height))
            val fbo = context.getTexFBO(texture.width, texture.height)
            fbo.use {
                context.texture2DProgram.reset()
                context.texture2DProgram.setTexture(texture)
                context.texture2DProgram.draw()
                drawStar()
            }
            fbo.unbindTexture()?.let { imageInOut.out(it) }
        }
    }

    override fun onRelease() {
        mStarProgram.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    private fun drawStar() {
        mStarProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
        mStarProgram.draw()
    }

    // 为了方便观察，使用正交投影
    private fun updateProjectionMatrix(size: Size) {
        if (mPreviewSize.width != size.width || mPreviewSize.height != size.height) {
            if (size.width > size.height) {
                val ratio = size.width.toFloat() / size.height.toFloat()
                mProjectMatrix.setOrthoM(
                    -ratio, ratio,
                    -1F, 1F,
                    2F, 20F
                )
            } else {
                val ratio = size.height.toFloat() / size.width.toFloat()
                mProjectMatrix.setOrthoM(
                    -1F, 1F,
                    -ratio, ratio,
                    2F, 20F
                )
            }
            mPreviewSize = size
        }
    }
}

class StarProgram : GLProgram() {
    // 外圆半径
    private val mOuterRadius = 1F

    // 内圆半径
    private val mInnerRadius = mOuterRadius * 0.382F

    // 【增加此处】索引点
    private val mIndexBuffer = allocateByteBuffer(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1))
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
        )
    )
    private var mColorBuffer = allocateFloatBuffer(
        floatArrayOf(
            1F, 1F, 1F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
            1F, 0F, 0F, 1F,
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

        val count = 11
        val colors = FloatArray(count * 4)
        colors[0] = Color.red(realCenterColor) / 255F
        colors[1] = Color.green(realCenterColor) / 255F
        colors[2] = Color.blue(realCenterColor) / 255F
        colors[3] = Color.alpha(realCenterColor) / 255F

        val cornerRed = Color.red(realCornerColor) / 255F
        val cornerGreen = Color.green(realCornerColor) / 255F
        val cornerBlue = Color.blue(realCornerColor) / 255F
        val cornerAlpha = Color.alpha(realCornerColor) / 255F
        for (i in 1 until count) {
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
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mVertexCount, GLES20.GL_UNSIGNED_BYTE, mIndexBuffer)
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

    private fun Int.toRadians(): Double {
        return Math.toRadians(this.toDouble())
    }

    companion object {
        private const val TAG = "StarProgram"
    }
}

private fun Int.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}