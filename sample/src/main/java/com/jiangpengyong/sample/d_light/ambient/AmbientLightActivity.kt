package com.jiangpengyong.sample.d_light.ambient

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectionMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_sample.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/7/24 22:18
 * @email 56002982@qq.com
 * @des 环境光
 */
class AmbientLightActivity : AppCompatActivity() {
    companion object {
        private const val TOUCH_SCALE_FACTOR = 1 / 4F
        private const val RESET = 10000
    }

    private lateinit var mRenderView: RenderView
    private lateinit var mSpanAngleTitle: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_light_ambient)
        mRenderView = findViewById(R.id.surface_view)
        mSpanAngleTitle = findViewById(R.id.span_angle_title)
        findViewById<View>(R.id.reset).setOnClickListener {
            mRenderView.sendMessageToFilter(Message.obtain().apply { what = RESET })
            mRenderView.requestRender()
        }
        mSpanAngleTitle.text = "圆切割度数（10度）"
        findViewById<SeekBar>(R.id.span_angle).apply {
            setProgress(2)
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val spanAngle = (progress + 1) * 5
                    mSpanAngleTitle.text = "圆切割度数（${spanAngle}度）"
                    mRenderView.updateFilterData(Bundle().apply {
                        putInt("spanAngle", spanAngle)
                    })
                    mRenderView.requestRender()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        findViewById<RadioGroup>(R.id.drawing_mode).setOnCheckedChangeListener { group, checkedId ->
            mRenderView.updateFilterData(Bundle().apply {
                when (checkedId) {
                    R.id.gl_points -> putInt("drawingMode", AmbientLightBallProgram.DrawMode.Point.value)
                    R.id.gl_lines -> putInt("drawingMode", AmbientLightBallProgram.DrawMode.Line.value)
                    R.id.gl_triangles -> putInt("drawingMode", AmbientLightBallProgram.DrawMode.Face.value)
                }
            })
            mRenderView.requestRender()
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

    class RenderView : GLSurfaceView {
        private val mRenderer = Renderer()

        // 上次触控位置坐标
        private var mBeforeY = 0f
        private var mBeforeX = 0f

        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        fun updateFilterData(bundle: Bundle) {
            mRenderer.updateFilterData(bundle)
        }

        fun sendMessageToFilter(message: Message) {
            mRenderer.sendMessageToFilter(message)
        }

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent?): Boolean {
            event ?: return false
            val y = event.y
            val x = event.x
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dy: Float = y - mBeforeY
                    val yAngle = dy * TOUCH_SCALE_FACTOR

                    val dx: Float = x - mBeforeX
                    val xAngle = dx * TOUCH_SCALE_FACTOR

                    mRenderer.updateFilterData(
                        Bundle().apply {
                            putFloat("xAngle", xAngle)
                            putFloat("yAngle", yAngle)
                        }
                    )
                    requestRender()
                }
            }
            mBeforeY = y
            mBeforeX = x
            return true
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilterId = "BallFilter"
            private val mBallFilter = BallFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()

            fun updateFilterData(bundle: Bundle) {
                mBallFilter.updateData(mFilterId, bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mBallFilter.init(mContext)
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.previewSize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mBallFilter.draw(mImage)
            }

            fun sendMessageToFilter(message: Message) {
                mBallFilter.receiveMessage(mFilterId, message)
            }
        }
    }

    class BallFilter : GLFilter() {
        private val mProgram = AmbientLightBallProgram()

        private val mProjectMatrix = ProjectionMatrix()
        private val mViewMatrix = ViewMatrix()
        private val mModelMatrix = ModelMatrix()

        private var xAngle = 0F
        private var yAngle = 0F

        private var mPreviewSize = Size(0, 0)

        override fun onInit(context: FilterContext) {
            mProgram.init()
            mViewMatrix.setLookAtM(
                0F, 0F, 10F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            GLES20.glClearColor(0F, 0F, 0F, 1F)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            updateProjectionMatrix(context)
            synchronized(this) {
                mProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
                mProgram.draw()
            }
        }

        override fun onRelease(context: FilterContext) {
            mProgram.release()
        }

        private fun updateProjectionMatrix(context: FilterContext) {
            val previewSize = context.previewSize
            if (mPreviewSize.width != previewSize.width || mPreviewSize.height != previewSize.height) {
                val ratio = previewSize.width.toFloat() / previewSize.height.toFloat()
                if (previewSize.width > previewSize.height) {
                    mProjectMatrix.setFrustumM(
                        -ratio, ratio,
                        -1F, 1F,
                        5F, 20F
                    )
                } else {
                    mProjectMatrix.setFrustumM(
                        -1F, 1F,
                        -ratio, ratio,
                        5F, 20F
                    )
                }
                mPreviewSize = previewSize
            }
        }

        override fun onUpdateData(updateData: Bundle) {
            synchronized(this) {
                xAngle += updateData.getFloat("xAngle", 0F)
                yAngle += updateData.getFloat("yAngle", 0F)
                mModelMatrix.reset()
                mModelMatrix.rotate(xAngle, 0F, 1F, 0F)
                mModelMatrix.rotate(yAngle, 1F, 0F, 0F)

                val mode = updateData.getInt("drawingMode", 0)
                if (mode != 0) {
                    when (mode) {
                        AmbientLightBallProgram.DrawMode.Point.value -> mProgram.setDrawMode(AmbientLightBallProgram.DrawMode.Point)
                        AmbientLightBallProgram.DrawMode.Line.value -> mProgram.setDrawMode(AmbientLightBallProgram.DrawMode.Line)
                        AmbientLightBallProgram.DrawMode.Face.value -> mProgram.setDrawMode(AmbientLightBallProgram.DrawMode.Face)
                    }
                }

                val spanAngle = updateData.getInt("spanAngle", 0)
                if (spanAngle != 0) {
                    mProgram.setAngleSpan(spanAngle)
                }
            }
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {
            synchronized(this) {
                if (message.what == RESET) {
                    xAngle = 0F
                    yAngle = 0F
                    mModelMatrix.reset()
                }
            }
        }
    }
}