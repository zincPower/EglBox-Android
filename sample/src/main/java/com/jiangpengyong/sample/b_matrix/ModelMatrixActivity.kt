package com.jiangpengyong.sample.b_matrix

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Size
import android.util.SizeF
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
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import java.util.logging.Logger
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min

/**
 * @author jiang peng yong
 * @date 2024/6/16 15:03
 * @email 56002982@qq.com
 * @des 模型变化
 */
class ModelMatrixActivity : AppCompatActivity() {
    companion object {
        private const val MODE = "mode"
        private const val TRIGGER_RENDER = "trigger_render"
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
        setContentView(R.layout.activity_model)

        mRenderView = findViewById(R.id.surface_view)

        val model = findViewById<RadioGroup>(R.id.model)
        model.setOnCheckedChangeListener { group, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.translation -> {
                    bundle.putInt(MODE, ModelMode.Translation.value)
                }

                R.id.scale -> {
                    bundle.putInt(MODE, ModelMode.Scale.value)
                }

                R.id.rotation -> {
                    bundle.putInt(MODE, ModelMode.Rotation.value)
                }
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
            private var mBundle = Bundle()

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
//                val bundle = synchronized(mBundle) {
//                    Bundle(mBundle)
//                }
//                mFilter.updateData(bundle)
                mFilter.draw(mImage)
            }

            fun updateFilterData(bundle: Bundle) {
                synchronized(mBundle) {
                    mBundle.putAll(bundle)
                }
            }
        }
    }

    enum class ModelMode(val value: Int) { Translation(1), Scale(2), Rotation(3) }

    class CubeFilter : GLFilter() {
        enum class State {
            RightToLeft,
            LeftToRight,
        }

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

        private var mState = State.LeftToRight

        private var mMode = ModelMode.Translation
        private val mCubeProgram = PureColorCubeProgram()
        private val mProjectMatrix = ProjectMatrix()
        private val mViewMatrix = ViewMatrix()
        private var mModelMatrix = ModelMatrix()

        private var mTranslationOffset = 0F

        private var mDisplaySize = Size(0, 0)

        override fun onInit() {
            mCubeProgram.init()
            mProjectMatrix.setFrustumM(
                -1F, 1F,
                -1F, 1F,
                2F, 10F
            )
            mViewMatrix.setLookAtM(
                0F, 0F, 5F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            if (mDisplaySize.width != context.displaySize.width || mDisplaySize.height != context.displaySize.height) {
                val width = min(context.displaySize.width, context.displaySize.height)
                mModelMatrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
                mModelMatrix.translate(mSpace.leftToRightSize.start, mSpace.bottomToTopSize.start, mSpace.nearToFarSize.start)
                mDisplaySize = context.displaySize
            }
            if (!mDisplaySize.isValid()) return
            if (mMode == ModelMode.Translation) {
                val ratio = 1 / 100F
                val tranVer = mSpace.leftToRightSize.width * ratio
                val tranHor = mSpace.bottomToTopSize.width * ratio
                val tranDis = mSpace.nearToFarSize.width * ratio
                if (mState == State.LeftToRight) {
                    mTranslationOffset += ratio
                    mModelMatrix.translate(tranVer, tranHor, tranDis)
                    if (mTranslationOffset >= 1F) {
                        mState = State.RightToLeft
                    }
                } else if (mState == State.RightToLeft) {
                    mTranslationOffset -= ratio
                    mModelMatrix.translate(-tranVer, -tranHor, -tranDis)
                    if (mTranslationOffset <= 0F) {
                        mState = State.LeftToRight
                    }
                }
            }
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
            mCubeProgram.draw()
        }

        override fun onRelease() {
            mCubeProgram.release()
        }

        override fun onUpdateData(inputData: Bundle) {
            val mode = inputData.getInt(MODE, ModelMode.Translation.value)
            mMode = when (mode) {
                ModelMode.Translation.value -> ModelMode.Translation
                ModelMode.Scale.value -> ModelMode.Scale
                ModelMode.Rotation.value -> ModelMode.Rotation
                else -> ModelMode.Translation
            }
        }

        override fun onRestoreData(restoreData: Bundle) {}
        override fun onSaveData(saveData: Bundle) {}
    }
}