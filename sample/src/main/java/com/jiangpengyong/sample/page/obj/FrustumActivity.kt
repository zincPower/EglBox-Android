package com.jiangpengyong.sample.page.obj

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.sample.filter.TriangleFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FrustumActivity : AppCompatActivity() {

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

private class RenderView(context: Context?) : GLSurfaceView(context) {
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
//            mContext.width = width
//            mContext.height = height
//            mTriangleFilter.updateData(mContext)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
            mTriangleFilter.draw(mImage)
        }

    }

}