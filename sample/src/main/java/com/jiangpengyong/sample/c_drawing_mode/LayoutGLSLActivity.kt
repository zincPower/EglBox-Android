package com.jiangpengyong.sample.c_drawing_mode

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectionMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/7/14 12:01
 * @email 56002982@qq.com
 * @des glDrawElements 方式绘制
 */
class LayoutGLSLActivity : AppCompatActivity() {
    private lateinit var mRenderView: RenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRenderView = RenderView(this)
        setContentView(mRenderView)
    }

    override fun onResume() {
        super.onResume()
        mRenderView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mRenderView.onPause()
    }

    class RenderView(context: Context?) : GLSurfaceView(context) {
        private val mRenderer = Renderer()

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilter = StarFilter()
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                // 开启卷绕
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                // 按顺时针卷绕
                GLES20.glFrontFace(GLES20.GL_CW)
                mFilter.init(mContext)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.previewSize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mFilter.draw(mImage)
            }
        }
    }

    class StarFilter : GLFilter() {
        private val mProjectMatrix = ProjectionMatrix()
        private val mViewMatrix = ViewMatrix()
        private val mModelMatrix = ModelMatrix()

        private val mStarProgram = StarProgram()
        private var mPreviewSize = Size(0, 0)

        override fun onInit(context: FilterContext) {
            mStarProgram.init()
            mViewMatrix.setLookAtM(
                0F, 0F, 5F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            updateProjectMatrix(context)
            drawStar()
        }

        override fun onRelease(context: FilterContext) {
            mStarProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {}
        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}

        private fun drawStar() {
            // 设置颜色
            mStarProgram.setColor("#F37B6A", "#FFFFFF")
            mStarProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
            mStarProgram.draw()
        }

        private fun updateProjectMatrix(context: FilterContext) {
            val previewSize = context.previewSize
            if (mPreviewSize.width != previewSize.width || mPreviewSize.height != previewSize.height) {
                val ratio = previewSize.width.toFloat() / previewSize.height.toFloat()
                if (previewSize.width > previewSize.height) {
                    mProjectMatrix.setOrthoM(
                        -ratio, ratio,
                        -1F, 1F,
                        2F, 10F
                    )
                } else {
                    mProjectMatrix.setOrthoM(
                        -1F, 1F,
                        -ratio, ratio,
                        2F, 10F
                    )
                }
                mPreviewSize = previewSize
            }
        }
    }

    class StarProgram : GLProgram() {
        // 外圆半径
        private val mOuterRadius = 1F

        // 内圆半径
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

        // 【修改此处】因为 GLSL 中使用 layout(location=xx) 固定了值，需要和 GLSL 中保持一致
        private var mMVPMatrixHandle = 10
        private var mPositionHandle = 11
        private var mColorHandle = 12

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
            for (i in 1 until mVertexCount) {
                colors[i * 4 + 0] = cornerRed
                colors[i * 4 + 1] = cornerGreen
                colors[i * 4 + 2] = cornerBlue
                colors[i * 4 + 3] = cornerAlpha
            }
            mColorBuffer = allocateFloatBuffer(colors)
        }

        override fun onInit() {
            // 单独一致变量不能使用 layout(location=xx) ，所以还是继续获取
            mMVPMatrixHandle = getUniformLocation("uMVPMatrix")

            // 【修改此处】因为 GLSL 中使用 layout(location=xx) 固定了值，更改为 glBindAttribLocation 进行绑定索引数值
            // mPositionHandle = getAttribLocation("aPosition")
            // mColorHandle = getAttribLocation("aColor")
            GLES20.glBindAttribLocation(id, mPositionHandle, "aPosition")
            GLES20.glBindAttribLocation(id, mColorHandle, "aColor")
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
            mMVPMatrixHandle = 0
            mPositionHandle = 0
            mColorHandle = 0
        }

        override fun getVertexShaderSource(): String = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            layout(location=11) in vec3 aPosition;
            layout(location=12) in vec4 aColor;
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
            layout(location=0) out vec4 fragColor;
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
}