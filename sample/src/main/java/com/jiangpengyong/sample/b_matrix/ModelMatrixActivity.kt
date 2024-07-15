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
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.program.CubeProgram
import com.jiangpengyong.eglbox.program.isValid
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/6/16 15:03
 * @email 56002982@qq.com
 * @des 模型矩阵变化
 */
class ModelMatrixActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_model_matrix)

        mRenderView = findViewById(R.id.surface_view)

        val model = findViewById<RadioGroup>(R.id.model)
        model.setOnCheckedChangeListener { group, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.translation -> bundle.putInt(MODE, ModelMode.Translation.value)
                R.id.scale -> bundle.putInt(MODE, ModelMode.Scale.value)
                R.id.rotation -> bundle.putInt(MODE, ModelMode.Rotation.value)
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
                }?.let { mFilter.updateData("CubeFilter", it) }
                mFilter.draw(mImage)
            }

            fun updateFilterData(bundle: Bundle) {
                synchronized(this) { mBundle = bundle }
            }
        }
    }

    enum class ModelMode(val value: Int) { Translation(1), Scale(2), Rotation(3) }

    class CubeFilter : GLFilter() {
        enum class State { Out, In }

        data class Area(val start: Float, val end: Float) {
            val width = end - start
        }

        data class Space(
            val leftToRightSize: Area,
            val bottomToTopSize: Area,
            val farToNearSize: Area,
        )

        // 移动范围
        private var mSpace = Space(
            leftToRightSize = Area(start = -5F, end = 2.5F),
            bottomToTopSize = Area(start = -5F, end = 1F),
            farToNearSize = Area(start = -10F, end = 1F)
        )

        private var mState = State.Out
        private var mMode = ModelMode.Translation
        private val mRatio = 1 / 100F
        private var mCurrentOffset = 0F
        private var mDisplaySize = Size(0, 0)

        private val mCubeProgram = CubeProgram()
        private val mProjectMatrix = ProjectMatrix()
        private val mViewMatrix = ViewMatrix()
        private var mModelMatrix = ModelMatrix()

        override fun onInit() {
            mCubeProgram.init()
            mViewMatrix.setLookAtM(
                0F, 0F, 5F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            if (mDisplaySize.width != context.displaySize.width || mDisplaySize.height != context.displaySize.height) {
                updateModelMatrix(context)
                updateProjectionMatrix(context)
                mDisplaySize = context.displaySize
            }
            if (!mDisplaySize.isValid()) return
            when (mMode) {
                ModelMode.Translation -> handleTranslation()
                ModelMode.Scale -> handleScale()
                ModelMode.Rotation -> handleRotation()
            }

            // 旋转模式的绘制两个立方体
            if (mMode == ModelMode.Rotation) {
                val leftMatrix = ModelMatrix().apply { translate(-1F, 0F, 0F) }
                mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * leftMatrix * mModelMatrix)
                mCubeProgram.draw()

                val rightMatrix = ModelMatrix().apply { translate(1F, 0F, 0F) }
                mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * rightMatrix * mModelMatrix)
                mCubeProgram.draw()
            } else {
                mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
                mCubeProgram.draw()
            }
        }

        private fun updateProjectionMatrix(context: FilterContext) {
            val displaySize = context.displaySize
            if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
                val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
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
        }

        override fun onRelease() {
            mCubeProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {
            val mode = updateData.getInt(MODE, ModelMode.Translation.value)
            mMode = when (mode) {
                ModelMode.Translation.value -> ModelMode.Translation
                ModelMode.Scale.value -> ModelMode.Scale
                ModelMode.Rotation.value -> ModelMode.Rotation
                else -> ModelMode.Translation
            }
            mContext?.let { updateModelMatrix(it) }
        }

        private fun updateModelMatrix(context: FilterContext) {
            mModelMatrix.reset()
            when (mMode) {
                ModelMode.Translation -> {
                    mModelMatrix.translate(mSpace.leftToRightSize.start, mSpace.bottomToTopSize.start, mSpace.farToNearSize.start)
                }

                ModelMode.Scale -> {
                    mModelMatrix.rotate(30F, 1F, 0F, 0F)
                    mModelMatrix.rotate(-30F, 0F, 1F, 0F)
                }

                ModelMode.Rotation -> {
                    mModelMatrix.rotate(45F, 1F, 0F, 0F)
                    mModelMatrix.rotate(45F, 0F, 1F, 0F)
                }
            }

            // 重置
            mCurrentOffset = 0F
            mState = State.Out
        }

        // 处理移动
        private fun handleTranslation() {
            val ratio = mRatio / 2
            mCurrentOffset += ratio

            val tranVer = mSpace.leftToRightSize.width * ratio
            val tranHor = mSpace.bottomToTopSize.width * ratio
            val tranDis = mSpace.farToNearSize.width * ratio
            if (mState == State.Out) {  // 从左向右移动
                mModelMatrix.translate(tranVer, tranHor, tranDis)
            } else if (mState == State.In) {   // 从右向左移动
                mModelMatrix.translate(-tranVer, -tranHor, -tranDis)
            }

            if (mCurrentOffset >= 1F) {
                mState = if (mState == State.Out) State.In else State.Out
                mCurrentOffset = 0F
            }
        }

        // 处理缩放
        private fun handleScale() {
            val beforeScale = mCurrentOffset
            if (mState == State.Out) {          // 变长
                mCurrentOffset += mRatio * 5
                if (mCurrentOffset >= 3F) mState = State.In
            } else if (mState == State.In) {   // 变短
                mCurrentOffset -= mRatio * 5
                if (mCurrentOffset <= 0F) mState = State.Out
            }
            mModelMatrix.scale((1F + mCurrentOffset) / (1F + beforeScale), 1F, 1F)
        }

        // 处理旋转
        private fun handleRotation() {
            mCurrentOffset += mRatio
            // 以 (1, 1, 1) 向量进行逆时针旋转
            mModelMatrix.rotate(mRatio / 5 * 360, 1F, 1F, 1F)
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}
    }
}