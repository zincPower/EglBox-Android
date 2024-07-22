package com.jiangpengyong.sample.b_matrix

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R
import com.jiangpengyong.eglbox.box.RenderType
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.program.CubeProgram
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import com.jiangpengyong.sample.utils.toRadians
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/6/16 15:03
 * @email 56002982@qq.com
 * @des 视图变化
 */
class ViewMatrixActivity : AppCompatActivity() {
    companion object {
        private const val MODE = "mode"
    }

    private lateinit var mRenderView: RenderView
    private val mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = object : Runnable {
        override fun run() {
            mRenderView.requestRender()
            mHandler.postDelayed(this, 10)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_matrix)

        mRenderView = findViewById(R.id.surface_view)

        val model = findViewById<RadioGroup>(R.id.model)
        model.setOnCheckedChangeListener { group, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.position -> bundle.putInt(MODE, ViewMode.Position.value)
                R.id.viewpoint -> bundle.putInt(MODE, ViewMode.Viewpoint.value)
                R.id.orientation -> bundle.putInt(MODE, ViewMode.Orientation.value)
            }
            mRenderView.updateFilterData(bundle)
        }

        mHandler.postDelayed(mRunnable, 10)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mRunnable)
    }

    class RenderView : GLSurfaceView {
        private val mRenderer = Renderer()

        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        fun updateFilterData(bundle: Bundle) {
            mRenderer.updateFilterData(bundle)
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilter = CubeFilter().apply {
                id = "CubeFilter"
                name = "CubeFilter"
            }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()
            private var mBundle: Bundle? = null

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                // 开启深度测试
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                // 开启背面裁剪
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                mFilter.init(mContext)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                synchronized(this) {
                    val temp = mBundle
                    mBundle = null
                    temp
                }?.let { mFilter.updateData("CubeFilter", it) }
                mFilter.draw(mImage)
            }

            fun updateFilterData(bundle: Bundle) {
                synchronized(this) { mBundle = bundle }
            }
        }
    }

    enum class ViewMode(val value: Int) { Position(1), Viewpoint(2), Orientation(3) }

    class CubeFilter : GLFilter() {
        enum class State { Out, In }

        // 立方体
        private val mCubeProgram = CubeProgram()

        // 投影矩阵
        private val mProjectMatrix = ProjectMatrix()

        // 视图矩阵
        private val mViewMatrix = ViewMatrix()

        // 模型矩阵
        // 左矩阵
        private val leftMatrix = ModelMatrix()
            .apply {
                translate(-1.5F, 0F, 0F)
                rotate(15F, 1F, 0F, 0F)
            }

        // 右矩阵
        private val rightMatrix = ModelMatrix()
            .apply {
                translate(1.5F, 0F, 0F)
                rotate(15F, 1F, 0F, 0F)
            }

        // 视图模式
        private var mMode = ViewMode.Position

        // 步长
        private val mStepSize = 1 / 100F

        // 当前步长累积
        private var mCurrentOffset = 0F
        private var mState = State.Out

        // 预览尺寸
        private var mDisplaySize = Size(0, 0)

        override fun onInit() {
            mCubeProgram.init()
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            val displaySize = mContext?.displaySize ?: return

            // 计算更新投影矩阵
            if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
                val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
                // 设置投影矩阵
                if (displaySize.width > displaySize.height) {
                    mProjectMatrix.setFrustumM(
                        -ratio, ratio,
                        -1F, 1F,
                        2F, 20F
                    )
                } else {
                    mProjectMatrix.setFrustumM(
                        -1F, 1F,
                        -ratio, ratio,
                        2F, 20F
                    )
                }
                mDisplaySize = displaySize
            }

            // 根据模式更新视图矩阵
            when (mMode) {
                ViewMode.Position -> handlePosition()
                ViewMode.Viewpoint -> handleViewpoint()
                ViewMode.Orientation -> handleOrientation()
            }

            // 设置矩阵并绘制
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * leftMatrix)
            mCubeProgram.draw()

            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * rightMatrix)
            mCubeProgram.draw()
        }

        private fun handlePosition() {
            val offset = 5 * mCurrentOffset
            when (mState) {
                State.Out -> {
                    mCurrentOffset += mStepSize / 2
                    if (mCurrentOffset >= 1F) mState = State.In
                }

                State.In -> {
                    mCurrentOffset -= mStepSize / 2
                    if (mCurrentOffset <= 0F) mState = State.Out
                }
            }
            // 摄像机的位置在 (0, 0, 3) 到 (0, 0, 8) 间来回移动
            // 观察点为 (0, 0, 0)
            // 观察方向为 (0, 1, 0)
            mViewMatrix.setLookAtM(
                0F, 0F, 3F + offset,
                0F, 0F, 0F,
                0F, 1F, 0F,
            )
        }

        private fun handleViewpoint() {
            val offset = 4 * mCurrentOffset
            when (mState) {
                State.Out -> {
                    mCurrentOffset += mStepSize / 2
                    if (mCurrentOffset >= 1F) mState = State.In
                }

                State.In -> {
                    mCurrentOffset -= mStepSize / 2
                    if (mCurrentOffset <= 0F) mState = State.Out
                }
            }

            // 摄像机的位置在 (0, 0, 5)
            // 观察点在 (-2，0，0) 到 (2, 0, 0) 间来回移动
            // 观察方向为 (0, 1, 0)
            mViewMatrix.setLookAtM(
                0F, 0F, 5F,
                -2F + offset, 0F, 0F,
                0F, 1F, 0F
            )
        }

        private fun handleOrientation() {
            val offset = mCurrentOffset * 360
            mCurrentOffset += mStepSize / 5
            // 摄像机的位置在 (0, 0, 5)
            // 观察点在 (0，0，0)
            // 观察方向为与 xy 平面平行的平面中 360 度旋转
            mViewMatrix.setLookAtM(
                0F, 0F, 5F,
                0F, 0F, 0F,
                cos(offset.toRadians()).toFloat(), sin(offset.toRadians()).toFloat(), 0F
            )
        }

        override fun onRelease() {
            mCubeProgram.release()
        }

        // 更新视图变化模式
        override fun onUpdateData(updateData: Bundle) {
            val mode = updateData.getInt(MODE, ViewMode.Position.value)
            mMode = when (mode) {
                ViewMode.Position.value -> ViewMode.Position
                ViewMode.Viewpoint.value -> ViewMode.Viewpoint
                ViewMode.Orientation.value -> ViewMode.Orientation
                else -> ViewMode.Position
            }
            mCurrentOffset = 0F
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}
    }
}