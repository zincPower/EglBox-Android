package com.jiangpengyong.sample.f_geometry

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.d_light.LightPointProgram
import com.jiangpengyong.sample.d_light.NormalTypeCubeProgram
import com.jiangpengyong.sample.f_geometry.geometry.CylinderFilter
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/8/26 08:29
 * @email 56002982@qq.com
 * @des 圆柱体
 */
class CylinderActivity : AppCompatActivity() {
    companion object {
        private const val TOUCH_SCALE_FACTOR = 1 / 4F
        private const val RESET = 10000
    }

    private lateinit var mRenderView: RenderView
    private lateinit var mLightPositionTip: TextView
    private lateinit var mShininessTitle: TextView

    private val mLightPosition = floatArrayOf(0F, 0F, 5F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geometry_cylinder)

        mRenderView = findViewById(R.id.surface_view)

        findViewById<View>(R.id.reset).setOnClickListener {
            mRenderView.sendMessageToFilter(Message.obtain().apply { what = RESET })
            mRenderView.requestRender()
        }

        findViewById<CheckBox>(R.id.ambient_light).setOnCheckedChangeListener { _, isChecked ->
            mRenderView.updateFilterData(Bundle().apply {
                putInt("ambientLight", if (isChecked) 1 else 0)
            })
            mRenderView.requestRender()
        }
        findViewById<CheckBox>(R.id.scattered_light).setOnCheckedChangeListener { _, isChecked ->
            mRenderView.updateFilterData(Bundle().apply {
                putInt("scatteredLight", if (isChecked) 1 else 0)
            })
            mRenderView.requestRender()
        }
        findViewById<CheckBox>(R.id.specular_light).setOnCheckedChangeListener { _, isChecked ->
            mRenderView.updateFilterData(Bundle().apply {
                putInt("specularLight", if (isChecked) 1 else 0)
            })
            mRenderView.requestRender()
        }

        mShininessTitle = findViewById(R.id.shininess_title)
        findViewById<SeekBar>(R.id.shininess).apply {
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = progress + 1
                    mRenderView.updateFilterData(Bundle().apply {
                        putFloat("shininess", value.toFloat())
                    })
                    mShininessTitle.text = "光滑度（${value}）"
                    mRenderView.requestRender()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        findViewById<RadioGroup>(R.id.normal_type).setOnCheckedChangeListener { _, checkedId ->
            mRenderView.updateFilterData(Bundle().apply {
                putInt("normalType", if (checkedId == R.id.vertex_normal) NormalTypeCubeProgram.NormalType.VERTEX_NORMAL.value else NormalTypeCubeProgram.NormalType.FACE_NORMAL.value)
            })
            mRenderView.requestRender()
        }

        mLightPositionTip = findViewById(R.id.light_position_tip)
        updateLightPositionTip()
        findViewById<SeekBar>(R.id.light_x_position).apply {
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // x 在 [-10, 10] 区间游走
                    val x = progress / max.toFloat() * 20 - 10
                    mLightPosition[0] = x
                    mRenderView.updateFilterData(Bundle().apply {
                        putFloat("lightXPosition", x)
                    })
                    updateLightPositionTip()
                    mRenderView.requestRender()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        findViewById<SeekBar>(R.id.light_y_position).apply {
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // y 在 [-10, 10] 区间游走
                    val y = progress / max.toFloat() * 20 - 10
                    mLightPosition[1] = y
                    mRenderView.updateFilterData(Bundle().apply {
                        putFloat("lightYPosition", y)
                    })
                    updateLightPositionTip()
                    mRenderView.requestRender()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        findViewById<SeekBar>(R.id.light_z_position).apply {
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // z 在 [-5, 5] 区间游走
                    val z = progress / max.toFloat() * 10 - 5
                    mLightPosition[2] = z
                    mRenderView.updateFilterData(Bundle().apply {
                        putFloat("lightZPosition", z)
                    })
                    updateLightPositionTip()
                    mRenderView.requestRender()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun updateLightPositionTip() {
        mLightPositionTip.text = "(${String.format("%.2f", mLightPosition[0])}, ${String.format("%.2f", mLightPosition[1])}, ${String.format("%.2f", mLightPosition[2])})"
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

                    mRenderer.updateFilterData(Bundle().apply {
                        putFloat("xAngle", xAngle)
                        putFloat("yAngle", yAngle)
                    })
                    requestRender()
                }
            }
            mBeforeY = y
            mBeforeX = x
            return true
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilterId = "CylinderFilter"
            private val mFilter = CylinderFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()

            private val mTexture = GLTexture()

            fun updateFilterData(bundle: Bundle) {
                mFilter.updateData(mFilterId, bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
//                GLES20.glFrontFace(GLES20.GL_CCW)
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_square.png").absolutePath).let { bitmap ->
                    mTexture.init()
                    mTexture.setData(bitmap)
                    bitmap.recycle()
                }
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mFilter.setTexture(mTexture, mTexture, mTexture)
                mFilter.draw(mImage)
            }

            fun sendMessageToFilter(message: Message) {
                mFilter.receiveMessage(mFilterId, message)
            }
        }
    }
//
//    class BallFilter : GLFilter() {
//        private val mProgram = NormalTypeCubeProgram()
//        private val mLightPointProgram = LightPointProgram()
//
//        private val mProjectMatrix = ProjectMatrix()
//        private val mViewMatrix = ViewMatrix()
//        private val mRotateMatrix = ModelMatrix()
//        private val mLeftModelMatrix = ModelMatrix()
//            .apply {
//                translate(-2F, 0F, 0F)
//            }
//        private val mRightModelMatrix = ModelMatrix()
//            .apply {
//                translate(2F, 0F, 0F)
//            }
//
//        private var mXAngle = 0F
//        private var mYAngle = 0F
//
//        private var mDisplaySize = Size(0, 0)
//
//        private var mLightPosition = floatArrayOf(0F, 0F, 5F)
//        private var mCameraPosition = floatArrayOf(0F, 0F, 10F)
//
//        override fun onInit() {
//            mProgram.init()
//            mLightPointProgram.init()
//            mViewMatrix.setLookAtM(
//                mCameraPosition[0], mCameraPosition[1], mCameraPosition[2],
//                0F, 0F, 0F,
//                0F, 1F, 0F
//            )
//        }
//
//        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
//            GLES20.glClearColor(0F, 0F, 0F, 1F)
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//            updateProjectionMatrix(context)
//            synchronized(this) {
//                mProgram.setLightPosition(mLightPosition)
//                mProgram.setCameraPosition(mCameraPosition)
//
//                mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mRotateMatrix * mLeftModelMatrix)
//                mProgram.setMMatrix(mRotateMatrix * mLeftModelMatrix)
//                mProgram.draw()
//
//                mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mRotateMatrix * mRightModelMatrix)
//                mProgram.setMMatrix(mRotateMatrix * mRightModelMatrix)
//                mProgram.draw()
//
//                mLightPointProgram.setMatrix(mProjectMatrix * mViewMatrix)
//                mLightPointProgram.setLightPosition(mLightPosition)
//                mLightPointProgram.draw()
//            }
//        }
//
//        override fun onRelease() {
//            mProgram.release()
//            mLightPointProgram.release()
//        }
//
//        private fun updateProjectionMatrix(context: FilterContext) {
//            val displaySize = context.displaySize
//            if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
//                val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
//                if (displaySize.width > displaySize.height) {
//                    mProjectMatrix.setFrustumM(
//                        -ratio, ratio,
//                        -1F, 1F,
//                        5F, 20F
//                    )
//                } else {
//                    mProjectMatrix.setFrustumM(
//                        -1F, 1F,
//                        -ratio, ratio,
//                        5F, 20F
//                    )
//                }
//                mDisplaySize = displaySize
//            }
//        }
//
//        override fun onUpdateData(updateData: Bundle) {
//            synchronized(this) {
//                mXAngle += updateData.getFloat("xAngle", 0F)
//                mYAngle += updateData.getFloat("yAngle", 0F)
//                mRotateMatrix.reset()
//                mRotateMatrix.rotate(mXAngle, 0F, 1F, 0F)
//                mRotateMatrix.rotate(mYAngle, 1F, 0F, 0F)
//
//                updateData.getFloat("lightXPosition", -10000F)
//                    .takeIf { it != -10000F }
//                    ?.let {
//                        mLightPosition[0] = it
//                        mProgram.setLightPosition(mLightPosition)
//                    }
//                updateData.getFloat("lightYPosition", -10000F)
//                    .takeIf { it != -10000F }
//                    ?.let {
//                        mLightPosition[1] = it
//                        mProgram.setLightPosition(mLightPosition)
//                    }
//                updateData.getFloat("lightZPosition", -10000F)
//                    .takeIf { it != -10000F }
//                    ?.let {
//                        mLightPosition[2] = it
//                        mProgram.setLightPosition(mLightPosition)
//                    }
//
//                updateData.getFloat("shininess", -10000F)
//                    .takeIf { it != -10000F }
//                    ?.let {
//                        mProgram.setShininess(it)
//                    }
//
//                updateData.getInt("normalType", -10000)
//                    .takeIf { it != -10000 }
//                    ?.let {
//                        when (it) {
//                            NormalTypeCubeProgram.NormalType.VERTEX_NORMAL.value -> mProgram.setNormalType(NormalTypeCubeProgram.NormalType.VERTEX_NORMAL)
//                            NormalTypeCubeProgram.NormalType.FACE_NORMAL.value -> mProgram.setNormalType(NormalTypeCubeProgram.NormalType.FACE_NORMAL)
//                            else -> {}
//                        }
//
//                    }
//
//                updateData.getInt("ambientLight", -10000)
//                    .takeIf { it != -10000 }
//                    ?.let {
//                        mProgram.isAddAmbientLight(it == 1)
//                    }
//                updateData.getInt("scatteredLight", -10000)
//                    .takeIf { it != -10000 }
//                    ?.let {
//                        mProgram.isAddScatteredLight(it == 1)
//                    }
//                updateData.getInt("specularLight", -10000)
//                    .takeIf { it != -10000 }
//                    ?.let {
//                        mProgram.isAddSpecularLight(it == 1)
//                    }
//            }
//        }
//
//        override fun onRestoreData(inputData: Bundle) {}
//        override fun onStoreData(outputData: Bundle) {}
//        override fun onReceiveMessage(message: Message) {
//            synchronized(this) {
//                if (message.what == RESET) {
//                    mXAngle = 0F
//                    mYAngle = 0F
//                    mRotateMatrix.reset()
//                }
//            }
//        }
//    }
}