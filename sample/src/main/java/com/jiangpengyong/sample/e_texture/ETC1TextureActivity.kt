package com.jiangpengyong.sample.e_texture

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.ETC1Util
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.Texture2DProgram
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/8/19 22:13
 * @email 56002982@qq.com
 * @des 压缩纹理 ETC
 */
class ETC1TextureActivity : AppCompatActivity() {
    private lateinit var mRenderView: RenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_ect1)
        mRenderView = findViewById(R.id.surface_view)

        findViewById<RadioGroup>(R.id.texture_type).setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.horizontal_image -> mRenderView.updateFilterData(Bundle().apply { putInt("textureType", 1) })
                R.id.vertical_image -> mRenderView.updateFilterData(Bundle().apply { putInt("textureType", 2) })
            }
            mRenderView.requestRender()
        }

        findViewById<RadioGroup>(R.id.scale_mode).setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.center_inside -> mRenderView.updateFilterData(Bundle().apply { putInt("scaleMode", 1) })
                R.id.center_crop -> mRenderView.updateFilterData(Bundle().apply { putInt("scaleMode", 2) })
                R.id.fix_xy -> mRenderView.updateFilterData(Bundle().apply { putInt("scaleMode", 3) })
                R.id.matrix -> mRenderView.updateFilterData(Bundle().apply { putInt("scaleMode", 4) })
            }
            mRenderView.requestRender()
        }
    }

    override fun onResume() {
        super.onResume()
        mRenderView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mRenderView.onPause()
    }


    class RenderView : GLSurfaceView {
        private val mRenderer = Renderer()

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        fun updateFilterData(bundle: Bundle) {
            mRenderer.updateFilterData(bundle)
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilterId = "ECT1TextureFilter"
            private val mFilter = Texture2DFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mTexture1 = GLTexture()
            private val mTexture2 = GLTexture()
            private val mImage = ImageInOut()

            private var mTextureType = 1

            fun updateFilterData(bundle: Bundle) {
                bundle.getInt("textureType", 0)
                    .takeIf { it == 1 || it == 2 }
                    ?.let { mTextureType = it }
                mFilter.updateData(mFilterId, bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
                mTexture1.init()
                // 使用 ETC1 工具加载
                // 图片使用 etc1tool '图片路径' --encode 进行编译
                // 透明通道会丢失
                File(App.context.filesDir, "images/test_image/test_image_horizontal.pkm").inputStream().use { inputStream ->
                    val etc1Texture = ETC1Util.createTexture(inputStream)
                    mTexture1.setData(etc1Texture)
                }
                mTexture2.init()
                // 使用 ETC1 工具加载
                File(App.context.filesDir, "images/test_image/test_image_vertical.pkm").inputStream().use { inputStream ->
                    val etc1Texture = ETC1Util.createTexture(inputStream)
                    mTexture2.setData(etc1Texture)
                }
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

                if (mTextureType == 1) {
                    mImage.reset(mTexture1)
                } else {
                    mImage.reset(mTexture2)
                }

                mFilter.draw(mImage)

                mImage.clear()
            }
        }
    }

    class Texture2DFilter : GLFilter() {
        private val mTexture2DProgram = Texture2DProgram(Target.TEXTURE_2D)
        private val mMatrix = ModelMatrix()
        private var mScaleType = ScaleType.CENTER_INSIDE

        override fun onInit() {
            mTexture2DProgram.init()
            mMatrix.reset()
            mMatrix.scale(-1F, -1F, 1F)
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            imageInOut.texture?.let { mTexture2DProgram.setTexture(it) }
            mTexture2DProgram.isMirrorY(true)
            if (mScaleType == ScaleType.MATRIX) {
                mTexture2DProgram.setVertexMatrix(mMatrix.matrix)
                    .setScaleType(ScaleType.MATRIX)
            } else {
                mTexture2DProgram.setScaleType(mScaleType)
            }
            mContext?.let { mTexture2DProgram.setTargetSize(it.displaySize) }
            mTexture2DProgram.draw()
        }

        override fun onRelease() {
            mTexture2DProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {
            updateData.getInt("scaleMode", 0)
                .takeIf { it in 1 until 5 }
                ?.let {
                    mScaleType = when (it) {
                        1 -> ScaleType.CENTER_INSIDE
                        2 -> ScaleType.CENTER_CROP
                        3 -> ScaleType.FIT_XY
                        4 -> ScaleType.MATRIX
                        else -> ScaleType.CENTER_CROP
                    }
                }
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}
    }
}