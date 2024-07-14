package com.jiangpengyong.sample.e_texture

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.gles.GLTexture
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.gles.Target
import com.jiangpengyong.eglbox.program.ScaleType
import com.jiangpengyong.eglbox.program.Texture2DProgram
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.sample.App
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Texture2DActivity : AppCompatActivity() {
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
        private val mFilter = Texture2DFilter()
        private val mContext = FilterContext()
        private val mTexture = GLTexture()
        private val mImage = ImageInOut()

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            mFilter.init(mContext)
            mTexture.init()
            mTexture.setData(
                BitmapFactory.decodeFile(
                    File(App.context.filesDir, "images/original_image_1.jpeg").absolutePath
//                    File(App.context.filesDir, "images/original_image_2.jpeg").absolutePath
                )
            )
            mImage.reset(mTexture)
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

class Texture2DFilter : GLFilter() {
    private val mTexture2DProgram = Texture2DProgram(Target.TEXTURE_2D)
    private val mMatrix = ModelMatrix()

    override fun onInit() {
        mTexture2DProgram.init()
        mMatrix.reset()
        mMatrix.scale(-1F, 1F, 1F)
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        imageInOut.texture?.let { mTexture2DProgram.setTexture(it) }
        mTexture2DProgram.isMirrorY(true)
//        mTexture2DProgram.setScaleType(ScaleType.CENTER_INSIDE)
//        mTexture2DProgram.setScaleType(ScaleType.FIT_XY)
//        mTexture2DProgram
//            .setVertexMatrix(mMatrix.matrix)
//            .setScaleType(ScaleType.MATRIX)
        mTexture2DProgram.setScaleType(ScaleType.CENTER_CROP)
        mContext?.let { mTexture2DProgram.setTargetSize(it.displaySize) }
        mTexture2DProgram.draw()
    }

    override fun onRelease() {
        mTexture2DProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}
    override fun onRestoreData(restoreData: Bundle) {}
    override fun onSaveData(saveData: Bundle) {}
}