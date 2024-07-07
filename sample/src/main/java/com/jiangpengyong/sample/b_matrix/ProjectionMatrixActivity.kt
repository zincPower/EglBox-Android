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
import com.jiangpengyong.eglbox.program.CubeProgram
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

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
        private const val VIEWPORT_MODE = "viewportMode"
    }

    private lateinit var mRenderView: RenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projection_matrix)

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

        val viewport = findViewById<RadioGroup>(R.id.viewport)
        viewport.setOnCheckedChangeListener { group, checkedId ->
            val bundle = Bundle()
            if (checkedId == R.id.full) {
                bundle.putInt(VIEWPORT_MODE, ViewportMode.Full.value)
            } else {
                bundle.putInt(VIEWPORT_MODE, ViewportMode.Half.value)
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
    enum class ViewportMode(val value: Int) { Full(1), Half(2) }

    class CubeFilter : GLFilter() {
        private var mProjectionMode = ProjectionMode.Frustum
        private var mViewportMode = ViewportMode.Full
        private val mCubeProgram = CubeProgram()
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
            // 这里可以不用每次都计算，为了方便阅读
            updateProjection()

            val leftMatrix = ModelMatrix()
            leftMatrix.translate(-1F, 0F, -2F)
            leftMatrix.rotate(30F, 1F, 0F, 1F)
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * leftMatrix)
            mCubeProgram.draw()

            val rightMatrix = ModelMatrix()
            rightMatrix.translate(1F, 0F, 0F)
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * rightMatrix)
            mCubeProgram.draw()
        }

        override fun onRelease() {
            mCubeProgram.release()
        }

        private fun updateProjection() {
            val displaySize = mContext?.displaySize ?: return
            val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
            val left: Float
            val right: Float
            val bottom: Float
            val top: Float
            if (displaySize.width > displaySize.height) {   // 纵向屏
                left = -ratio
                right = ratio
                bottom = -1F
                top = 1F
            } else {                                         // 横向屏
                left = -1F
                right = 1F
                bottom = -ratio
                top = ratio
            }
            if (mProjectionMode == ProjectionMode.Frustum) {
                // 设置投影矩阵
                mProjectMatrix.setFrustumM(
                    left, right,
                    bottom, top,
                    2F, 10F
                )
            } else {
                // 设置正交投影
                mProjectMatrix.setOrthoM(
                    left, right,
                    bottom, top,
                    2F, 10F
                )
            }
        }

        private fun updateViewport() {
            val size = mContext?.displaySize ?: return
            if (mViewportMode == ViewportMode.Full) {
                GLES20.glViewport(0, 0, size.width, size.height)
            } else {
                GLES20.glViewport(0, 0, size.width / 2, size.height / 2)
            }
        }

        override fun onUpdateData(inputData: Bundle) {
            mProjectionMode = if (inputData.getInt(MODE, mProjectionMode.value) == ProjectionMode.Frustum.value) {
                ProjectionMode.Frustum
            } else {
                ProjectionMode.Ortho
            }
            updateProjection()

            mViewportMode = if (inputData.getInt(VIEWPORT_MODE, mViewportMode.value) == ViewportMode.Full.value) {
                ViewportMode.Full
            } else {
                ViewportMode.Half
            }
            updateViewport()
        }

        override fun onRestoreData(restoreData: Bundle) {}
        override fun onSaveData(saveData: Bundle) {}
    }
}
