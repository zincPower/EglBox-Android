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
 * @date 2024/2/9 15:33
 * @email 56002982@qq.com
 * @des 顶点常量
 */
class VertexAttribActivity : AppCompatActivity() {
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
            // 选择 EGL 为 3.0 版本
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            // 按需驱动渲染
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

    private class StarFilter : GLFilter() {
        private val mProjectMatrix = ProjectionMatrix()
        private val mViewMatrix = ViewMatrix()
        private val mModelMatrix = ModelMatrix()

        private val mStarProgram = StarProgram()
        private var mPreviewSize = Size(0, 0)

        override fun onInit() {
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

        override fun onRelease() {
            mStarProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {}
        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}

        private fun drawStar() {
            // 设置颜色
            mStarProgram.setColor("#FFFF00")
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

        // 【更改此处】颜色改为一个数组
        private var mColors = floatArrayOf(1F, 1F, 0F, 1F)

        private var mMVPMatrixHandle = 0
        private var mPositionHandle = 0
        private var mColorHandle = 0

        private val mVertexCount = 12
        private var mMatrix: GLMatrix = GLMatrix()

        fun setMatrix(matrix: GLMatrix) {
            mMatrix = matrix
        }

        fun setColor(color: String) {
            val realColor = try {
                Color.parseColor(color)
            } catch (e: Exception) {
                Logger.e(TAG, "SetColor failure. Corner color isn't a valid color.")
                return
            }
            mColors[0] = Color.red(realColor) / 255F
            mColors[1] = Color.green(realColor) / 255F
            mColors[2] = Color.blue(realColor) / 255F
            mColors[3] = Color.alpha(realColor) / 255F
        }

        override fun onInit() {
            mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
            mPositionHandle = getAttribLocation("aPosition")
            mColorHandle = getAttribLocation("aColor")
        }

        override fun onDraw() {
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
            GLES20.glEnableVertexAttribArray(mPositionHandle)

            // 【更改此处】因为是顶点常量，传递三种方式：
            // 第一种：glVertexAttrib4f
            GLES20.glVertexAttrib4f(mColorHandle, mColors[0], mColors[1], mColors[2], mColors[3])
            // 第二种：glVertexAttrib4fv
//            GLES20.glVertexAttrib4fv(mColorHandle, allocateFloatBuffer(mColors))
            // 第三种：glVertexAttrib4fv
//            GLES20.glVertexAttrib4fv(mColorHandle, mColors, 0)
//            GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
//            GLES20.glEnableVertexAttribArray(mColorHandle)

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

}