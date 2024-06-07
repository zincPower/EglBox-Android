package com.jiangpengyong.sample.page.triangle

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.sample.filter.TriangleFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/2/9 15:33
 * @email 56002982@qq.com
 * @des 简易三角形 demo
 */
class SimpleTriangleActivity : AppCompatActivity() {

    private lateinit var mSimpleTriangleView: SimpleTriangleView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSimpleTriangleView = SimpleTriangleView(this)
        setContentView(mSimpleTriangleView)
    }

    override fun onResume() {
        super.onResume()
        mSimpleTriangleView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mSimpleTriangleView.onPause()
    }
}

class SimpleTriangleView(context: Context?) : GLSurfaceView(context) {
    private val mRenderer = Renderer()

    init {
        setEGLContextClientVersion(3)
        setRenderer(mRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }


    private class Renderer : GLSurfaceView.Renderer {

        private val mTriangleFilter = TriangleFilter()
        private val mContext = FilterContext()

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            mTriangleFilter.init(mContext)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            mContext.width = width
            mContext.height = height
            mTriangleFilter.updateSize(mContext)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
            mTriangleFilter.render(mContext)
        }

    }
}