package com.jiangpengyong.sample.b_matrix

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
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
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min

/**
 * @author jiang peng yong
 * @date 2024/6/16 15:03
 * @email 56002982@qq.com
 * @des 投影使用
 * GLSL 中使用的两种投影方式：透视投影、正交投影
 * 透视投影：近大远小
 * 正价投影：直接投射至屏幕，无论远近都一样大小
 */
class ProjectionActivity : AppCompatActivity() {
    companion object {
        private const val MODE = "projectionMode"
    }

    private lateinit var mRenderView: RenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projection)

        mRenderView = findViewById(R.id.surface_view)

        val projection = findViewById<RadioGroup>(R.id.projection)
        projection.setOnCheckedChangeListener { group, checkedId ->
            val bundle = Bundle()
            if (checkedId == R.id.frustum) {
                bundle.putInt(MODE, ProjectionMode.Frustum.value)
            } else {
                bundle.putInt(MODE, ProjectionMode.Ortho.value)
            }
            mRenderView.updateFilterData(bundle)
        }
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
            requestRender()
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilter = CubeFilter()
            private val mContext = FilterContext()
            private val mImage = ImageInOut()
            private var mBundle: Bundle? = null

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
                // 开启深度测试
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                // 开启背面裁剪
                GLES20.glEnable(GLES20.GL_CULL_FACE)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                val bundle = synchronized(this) {
                    val temp = mBundle
                    mBundle = null
                    temp
                }
                bundle?.let { mFilter.updateData(it) }
                mFilter.draw(mImage)
            }

            fun updateFilterData(bundle: Bundle) {
                synchronized(this) { mBundle = bundle }
            }
        }
    }

    enum class ProjectionMode(val value: Int) { Frustum(1), Ortho(2) }

    class CubeFilter : GLFilter() {
        private var mProjectionMode = ProjectionMode.Frustum
        private val mCubeProgram = PureColorCubeProgram()
        private val mProjectMatrix = ProjectMatrix()
        private val mViewMatrix = ViewMatrix()

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
            val width = min(context.displaySize.width, context.displaySize.height)
            val leftMatrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
            leftMatrix.translate(-1.5F, -1F, 0F)
            leftMatrix.rotate(30F, 0F, 0F, 1F)
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * leftMatrix)
            mCubeProgram.draw()

            val rightMatrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
            rightMatrix.translate(1.5F, 1F, 0F)
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * rightMatrix)
            mCubeProgram.draw()

            val centerMatrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * centerMatrix)
            mCubeProgram.draw()
        }

        override fun onRelease() {
            mCubeProgram.release()
        }

        private fun updateProjection() {
            if (mProjectionMode == ProjectionMode.Frustum) {
                mProjectMatrix.setFrustumM(
                    -1F, 1F,
                    -1F, 1F,
                    2F, 10F
                )
            } else {
                mProjectMatrix.setOrthoM(
                    -1F, 1F,
                    -1F, 1F,
                    2F, 10F
                )
            }
        }

        override fun onUpdateData(inputData: Bundle) {
            mProjectionMode = if (inputData.getInt(MODE, ProjectionMode.Frustum.value) == ProjectionMode.Frustum.value) {
                ProjectionMode.Frustum
            } else {
                ProjectionMode.Ortho
            }
            updateProjection()
        }

        override fun onRestoreData(restoreData: Bundle) {}
        override fun onSaveData(saveData: Bundle) {}
    }
}
