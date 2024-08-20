package com.jiangpengyong.sample.e_texture

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.sample.App
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/8/10 15:46
 * @email 56002982@qq.com
 * @des 三角形纹理
 */
class TriangleTextureActivity : AppCompatActivity() {
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
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mTriangleFilter = TriangleFilter()
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()
            private val mTexture = GLTexture()

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mTriangleFilter.init(mContext)
                mTexture.init()
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_horizontal.png").absolutePath).let { bitmap ->
                    mTexture.setData(bitmap)
                    bitmap.recycle()
                }
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mImage.reset(mTexture)
                mTriangleFilter.draw(mImage)
                mImage.clear()
            }
        }
    }

    class TriangleFilter : GLFilter() {
        private val mProgram = TriangleTextureProgram()

        override fun onInit() {
            mProgram.init()
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            imageInOut.texture?.let { mProgram.setTexture(it) }
            mProgram.draw()
        }

        override fun onRelease() {
            mProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {}
        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}
    }
}