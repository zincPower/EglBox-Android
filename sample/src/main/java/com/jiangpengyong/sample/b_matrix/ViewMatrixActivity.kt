package com.jiangpengyong.sample.b_matrix

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Size
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.program.PureColorCubeProgram
import com.jiangpengyong.eglbox.program.ScaleType
import com.jiangpengyong.eglbox.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox.program.isValid
import com.jiangpengyong.eglbox.program.toRadians
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.min
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
            private val mFilter = CubeFilter()
            private val mContext = FilterContext()
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
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                synchronized(this) {
                    val temp = mBundle
                    mBundle = null
                    temp
                }?.let { mFilter.updateData(it) }
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

        data class Area(val start: Float, val end: Float) {
            val width = end - start
        }

        data class Space(
            val leftToRightSize: Area,
            val bottomToTopSize: Area,
            val nearToFarSize: Area,
        )

        private var mSpace = Space(
            leftToRightSize = Area(start = -2.5F, end = 2.5F),
            bottomToTopSize = Area(start = -1F, end = 1F),
            nearToFarSize = Area(start = -1F, end = 1F)
        )

        private var mMode = ViewMode.Position

        private val mCubeProgram = PureColorCubeProgram()
        private val mProjectMatrix = ProjectMatrix()
        private val mViewMatrix = ViewMatrix()

        private val mRatio = 1 / 100F
        private var mCurrentOffset = 0F
        private var mState = State.Out

        override fun onInit() {
            mCubeProgram.init()
            mViewMatrix.setLookAtM(
                0F, 0F, 5F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            val displaySize = mContext?.displaySize ?: return
            val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
            mProjectMatrix.setFrustumM(
                -ratio, ratio,
                -1F, 1F,
                2F, 20F
            )

            when (mMode) {
                ViewMode.Position -> handlePosition()
                ViewMode.Viewpoint -> handleViewpoint()
                ViewMode.Orientation -> handleOrientation()
            }

            val width = min(context.displaySize.width, context.displaySize.height)
            val leftMatrix = ModelMatrix()//VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
            leftMatrix.translate(-1.5F, 0F, 0F)
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * leftMatrix)
            mCubeProgram.draw()

            val rightMatrix = ModelMatrix()//VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
            rightMatrix.translate(1.5F, 0F, 0F)
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * rightMatrix)
            mCubeProgram.draw()
        }

        private fun handlePosition() {
            val offset = 5 * mCurrentOffset
            when (mState) {
                State.Out -> {
                    mCurrentOffset += mRatio / 2
                    if (mCurrentOffset >= 1F) mState = State.In
                }

                State.In -> {
                    mCurrentOffset -= mRatio / 2
                    if (mCurrentOffset <= 0F) mState = State.Out
                }
            }

            mViewMatrix.setLookAtM(
                0F, 0F, 3F + offset,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        private fun handleViewpoint() {
            val offset = 4 * mCurrentOffset
            when (mState) {
                State.Out -> {
                    mCurrentOffset += mRatio / 2
                    if (mCurrentOffset >= 1F) mState = State.In
                }

                State.In -> {
                    mCurrentOffset -= mRatio / 2
                    if (mCurrentOffset <= 0F) mState = State.Out
                }
            }
            mViewMatrix.setLookAtM(
                0F, 0F, 3F,
                -2F + offset, 0F, 0F,
                0F, 1F, 0F
            )
        }

        private fun handleOrientation() {
            val offset = mCurrentOffset * 360
            mCurrentOffset += mRatio / 5

            mViewMatrix.setLookAtM(
                0F, 0F, 5F,
                0F, 0F, 0F,
                cos(offset.toRadians()).toFloat(), sin(offset.toRadians()).toFloat(), 0F
            )
        }

        override fun onRelease() {
            mCubeProgram.release()
        }

        override fun onUpdateData(inputData: Bundle) {
            val mode = inputData.getInt(MODE, ViewMode.Position.value)
            mMode = when (mode) {
                ViewMode.Position.value -> ViewMode.Position
                ViewMode.Viewpoint.value -> ViewMode.Viewpoint
                ViewMode.Orientation.value -> ViewMode.Orientation
                else -> ViewMode.Position
            }
            mCurrentOffset = 0F
        }

        override fun onRestoreData(restoreData: Bundle) {}
        override fun onSaveData(saveData: Bundle) {}
    }
}