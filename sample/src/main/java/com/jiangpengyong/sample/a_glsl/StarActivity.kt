package com.jiangpengyong.sample.a_glsl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.program.StarProgram
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/2/9 15:33
 * @email 56002982@qq.com
 * @des 三角形
 */
class StarActivity : AppCompatActivity() {
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

    class RenderView(context: Context?) : GLSurfaceView(context) {
        private val mRenderer = Renderer()

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_CONTINUOUSLY
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilter = StarFilter()
            private val mContext = FilterContext()
            private val mImage = ImageInOut()

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                GLES20.glFrontFace(GLES20.GL_CW)
                mFilter.init(mContext)
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
}

class StarFilter : GLFilter() {
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private val mStarProgram = StarProgram()
    private var mDisplaySize = Size(0, 0)

    override fun onInit() {
        mStarProgram.init()
        mViewMatrix.setLookAtM(
            0F, 0F, 5F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        updateProjectionMatrix(context)
        drawStar()
    }

    override fun onRelease() {
        mStarProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}
    override fun onRestoreData(restoreData: Bundle) {}
    override fun onSaveData(saveData: Bundle) {}

    private fun drawStar() {
        mStarProgram.setColor("#FF0000", "#FFFFFF")
        mStarProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
        mStarProgram.draw()
    }

    private fun updateProjectionMatrix(context: FilterContext) {
        val displaySize = context.displaySize
        if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
            val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
            if (displaySize.width > displaySize.height) {
                mProjectMatrix.setOrthoM(
                    -ratio, ratio,
                    -1F, 1F,
                    2F, 10F
                )
            } else {
                mProjectMatrix.setOrthoM(
                    -1F, 1F,
                    -ratio, ratio,
                    2F, 10F
                )
            }
            mDisplaySize = displaySize
        }
    }
}