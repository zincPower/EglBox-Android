package com.jiangpengyong.sample.a_glsl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.program.TriangleProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/2/9 15:33
 * @email 56002982@qq.com
 * @des 三角形
 */
class TriangleActivity : AppCompatActivity() {
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
            private val mTriangleFilter = TriangleFilter()
            private val mContext = FilterContext()
            private val mImage = ImageInOut()

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mTriangleFilter.init(mContext)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mTriangleFilter.draw(mImage)
            }
        }
    }
}

class TriangleFilter : GLFilter() {

    private val mTriangleProgram = TriangleProgram()

    override fun onInit() {
        mTriangleProgram.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        mTriangleProgram.draw()
    }

    override fun onRelease() {
        mTriangleProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}
    override fun onRestoreData(restoreData: Bundle) {}
    override fun onSaveData(saveData: Bundle) {}
}