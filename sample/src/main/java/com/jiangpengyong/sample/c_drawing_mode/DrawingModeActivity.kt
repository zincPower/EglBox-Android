package com.jiangpengyong.sample.c_drawing_mode

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
import com.jiangpengyong.eglbox.program.toRadians
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/6/16 15:03
 * @email 56002982@qq.com
 * @des 绘制方式
 */
class DrawingModeActivity : AppCompatActivity() {
    companion object {
        private const val MODE = "mode"
    }

    private lateinit var mRenderView: RenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing_mode)

        mRenderView = findViewById(R.id.surface_view)

        val model = findViewById<RadioGroup>(R.id.drawing_mode)
        model.setOnCheckedChangeListener { _, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.point -> bundle.putInt(MODE, DrawingMode.Point.value)
                R.id.line -> bundle.putInt(MODE, DrawingMode.Line.value)
                R.id.triangle -> bundle.putInt(MODE, DrawingMode.Triangle.value)
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

    enum class DrawingMode(val value: Int) { Point(1), Line(2), Triangle(3) }

    class CubeFilter : GLFilter() {
        // 立方体
        private val mCubeProgram = PureColorCubeProgram()

        // 投影矩阵
        private val mProjectMatrix = ProjectMatrix()

        // 视图矩阵
        private val mViewMatrix = ViewMatrix().apply {
            this.setLookAtM(
                0F, 0F, 5F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

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

        // 绘制方式
        private var mDrawingMode = DrawingMode.Point

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

            // 设置矩阵并绘制
            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * leftMatrix)
            mCubeProgram.draw()

            mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * rightMatrix)
            mCubeProgram.draw()
        }


        override fun onRelease() {
            mCubeProgram.release()
        }

        // 更新视图变化模式
        override fun onUpdateData(inputData: Bundle) {
            val mode = inputData.getInt(MODE, DrawingMode.Point.value)
            mDrawingMode = when (mode) {
                DrawingMode.Point.value -> DrawingMode.Point
                DrawingMode.Line.value -> DrawingMode.Line
                DrawingMode.Triangle.value -> DrawingMode.Triangle
                else -> DrawingMode.Point
            }
        }

        override fun onRestoreData(restoreData: Bundle) {}
        override fun onSaveData(saveData: Bundle) {}
    }
}