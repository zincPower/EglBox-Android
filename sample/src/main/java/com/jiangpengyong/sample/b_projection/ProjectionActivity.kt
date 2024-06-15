package com.jiangpengyong.sample.b_projection

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.program.CubeProgram
import com.jiangpengyong.eglbox.program.ScaleType
import com.jiangpengyong.eglbox.program.StarProgram
import com.jiangpengyong.eglbox.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min

class ProjectionActivity : AppCompatActivity() {
    private lateinit var mRenderView: RenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRenderView = RenderView(this)
        setContentView(mRenderView)
    }

    override fun onResume() {
        super.onResume()
        mRenderView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mRenderView.onPause()
    }
}

class RenderView(context: Context?) : GLSurfaceView(context) {
    private val mRenderer = Renderer()

    init {
        setEGLContextClientVersion(3)
        setRenderer(mRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    private class Renderer : GLSurfaceView.Renderer {
        private val mFilter = CubeFilter()
        private val mContext = FilterContext()
        private val mImage = ImageInOut()

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            mFilter.init(mContext)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            mContext.displaySize = Size(width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
            mFilter.draw(mImage)
        }
    }
}

class StarFilter : GLFilter() {
    private val mColors = arrayOf(
        floatArrayOf(0F / 255F, 50F / 255F, 133F / 255F),
        floatArrayOf(42F / 255F, 98F / 255F, 154F / 255F),
        floatArrayOf(255F / 255F, 127F / 255F, 62F / 255F),
        floatArrayOf(255F / 255F, 218F / 255F, 120F / 255F),
    )
    private val mStarProgram = StarProgram()
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()

    override fun onInit() {
        mStarProgram.init()
        mProjectMatrix.setFrustumM(
//        mProjectMatrix.setOrthoM(
            -1F, 1F,
            -1F, 1F,
            5F, 20F
        )
        mViewMatrix.setLookAtM(
            1F, 1F, 10F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        for (index in mColors.size - 1 downTo 0) {
            drawStar(context, index, mColors[index])
        }
    }

    override fun onRelease() {
        mStarProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}
    override fun onRestoreData(restoreData: Bundle) {}
    override fun onSaveData(saveData: Bundle) {}

    private fun drawStar(context: FilterContext, index: Int, colors: FloatArray) {
        val width = min(context.displaySize.width, context.displaySize.height)
        val matrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
        matrix.scale(0.8F, 0.8F, 1F)
        matrix.translate(index * 0.5F, index * 1F, index * -2F)
        mStarProgram.setColor(colors[0], colors[1], colors[2], 0F)
        mStarProgram.setMatrix(mProjectMatrix * mViewMatrix * matrix)
        mStarProgram.draw()
    }
}

class CubeFilter : GLFilter() {
    private val mCubeProgram = CubeProgram()
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()

    override fun onInit() {
        mCubeProgram.init()
        mProjectMatrix.setFrustumM(
//        mProjectMatrix.setOrthoM(
            -1F, 1F,
            -1F, 1F,
            2F, 20F
        )
        mViewMatrix.setLookAtM(
            0F, 0F, 5F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val width = min(context.displaySize.width, context.displaySize.height)
        val leftMatrix= VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
        leftMatrix.scale(0.8F, 0.8F, 1F)
        leftMatrix.translate(-2F, -2F, 0F)
        mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * leftMatrix)
        mCubeProgram.draw()

        val rightMatrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
        rightMatrix.scale(0.8F, 0.8F, 1F)
        rightMatrix.translate(2F, 2F, 0F)
        mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * rightMatrix)
        mCubeProgram.draw()

        val centerMatrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, context.displaySize, Size(width, width))
        centerMatrix.scale(0.8F, 0.8F, 1F)
        mCubeProgram.setMatrix(mProjectMatrix * mViewMatrix * centerMatrix)
        mCubeProgram.draw()
    }

    override fun onRelease() {
        mCubeProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}
    override fun onRestoreData(restoreData: Bundle) {}
    override fun onSaveData(saveData: Bundle) {}

}