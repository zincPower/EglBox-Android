package com.jiangpengyong.sample.d_light

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_sample.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/7/20 14:47
 * @email 56002982@qq.com
 * @des 球体
 */
class BallActivity : AppCompatActivity() {
    companion object {
        private const val TOUCH_SCALE_FACTOR = 1 / 4F
    }

    private lateinit var mRenderView: RenderView

    // 上次触控位置坐标
    private var mBeforeY = 0f
    private var mBeforeX = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRenderView = RenderView(this)
        setContentView(mRenderView)
        mRenderView.setOnTouchListener { v, event ->
            val y = event.y
            val x = event.x
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dy: Float = y - mBeforeY
                    val yAngle = dy * TOUCH_SCALE_FACTOR

                    val dx: Float = x - mBeforeX
                    val xAngle = dx * TOUCH_SCALE_FACTOR

                    mRenderView.updateFilterData(
                        Bundle().apply {
                            putFloat("xAngle", xAngle)
                            putFloat("yAngle", yAngle)
                        }
                    )
                    mRenderView.requestRender()
                }
            }
            mBeforeY = y
            mBeforeX = x
            true
        }
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

        fun updateFilterData(bundle: Bundle) {
            mRenderer.updateFilterData(bundle)
        }

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mBallFilter = BallFilter().apply { id = "BallFilter" }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()

            fun updateFilterData(bundle: Bundle) {
                mBallFilter.updateData("BallFilter", bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mBallFilter.init(mContext)
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                GLES20.glFrontFace(GLES20.GL_CW)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mBallFilter.draw(mImage)
            }
        }
    }

    class BallFilter : GLFilter() {
        private val mProgram = TrigonometricBallProgram()

        private val mProjectMatrix = ProjectMatrix()
        private val mViewMatrix = ViewMatrix()
        private val mModelMatrix = ModelMatrix()

        private var xAngle = 0F
        private var yAngle = 0F

        private var mDisplaySize = Size(0, 0)

        override fun onInit() {
            mProgram.init()
            mViewMatrix.setLookAtM(
                0F, 0F, 3F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            updateProjectionMatrix(context)
            mProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
            mProgram.draw()
        }

        override fun onRelease() {
            mProgram.release()
        }

        private fun updateProjectionMatrix(context: FilterContext) {
            val displaySize = context.displaySize
            if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
                val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
                if (displaySize.width > displaySize.height) {
                    mProjectMatrix.setFrustumM(
                        -ratio, ratio,
                        -1F, 1F,
                        2F, 10F
                    )
                } else {
                    mProjectMatrix.setFrustumM(
                        -1F, 1F,
                        -ratio, ratio,
                        2F, 10F
                    )
                }
                mDisplaySize = displaySize
            }
        }

        override fun onUpdateData(updateData: Bundle) {
            xAngle += updateData.getFloat("xAngle", 0F)
            yAngle += updateData.getFloat("yAngle", 0F)
            mModelMatrix.reset()
            mModelMatrix.rotate(xAngle, 0F, 1F, 0F)
            mModelMatrix.rotate(yAngle, 1F, 0F, 0F)
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}
    }
}