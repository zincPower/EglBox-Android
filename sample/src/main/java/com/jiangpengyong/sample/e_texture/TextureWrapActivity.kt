package com.jiangpengyong.sample.e_texture

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.gles.WrapMode
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
 * @date 2024/8/11 16:21
 * @email 56002982@qq.com
 * @des 纹理拉伸方式
 */
class TextureWrapActivity : AppCompatActivity() {
    private lateinit var mRenderView: RenderView
    private lateinit var mTexturePositionTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_wrap)
        mRenderView = findViewById(R.id.surface_view)

        findViewById<RadioGroup>(R.id.wrap_mode).setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.repeat_mode -> mRenderView.updateFilterData(Bundle().apply { putInt("wrapMode", 1) })
                R.id.mirror_mode -> mRenderView.updateFilterData(Bundle().apply { putInt("wrapMode", 2) })
                R.id.edge_mode -> mRenderView.updateFilterData(Bundle().apply { putInt("wrapMode", 3) })
            }
            mRenderView.requestRender()
        }

        mTexturePositionTitle = findViewById(R.id.texture_position_title)
        findViewById<SeekBar>(R.id.texture_position).apply {
            min = 10
            max = 50
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val texturePositionMax = progress / 10F
                    mTexturePositionTitle.text = "纹理顶点坐标（${texturePositionMax}）"
                    mRenderView.updateFilterData(Bundle().apply { putFloat("texturePositionMax", texturePositionMax) })
                    mRenderView.requestRender()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
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
            private val mFilterId = "Texture2DFilter"
            private val mFilter = Texture2DFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)

            private val mImage = ImageInOut()

            private val mRepeatTexture = GLTexture(wrapS = WrapMode.REPEAT, wrapT = WrapMode.REPEAT)
            private val mMirrorTexture = GLTexture(wrapS = WrapMode.MIRROR, wrapT = WrapMode.MIRROR)
            private val mEdgeTexture = GLTexture(wrapS = WrapMode.EDGE, wrapT = WrapMode.EDGE)
            private var mWrapMode = 1

            fun updateFilterData(bundle: Bundle) {
                mFilter.updateData(mFilterId, bundle)
                bundle.getInt("wrapMode", 0)
                    .takeIf { it != 0 }
                    ?.let { mWrapMode = it }
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
                mRepeatTexture.init()
                mMirrorTexture.init()
                mEdgeTexture.init()
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_square.jpg").absolutePath).let { bitmap ->
                    mRepeatTexture.setData(bitmap)
                    mMirrorTexture.setData(bitmap)
                    mEdgeTexture.setData(bitmap)
                    bitmap.recycle()
                }
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                when (mWrapMode) {
                    1 -> mImage.reset(mRepeatTexture)
                    2 -> mImage.reset(mMirrorTexture)
                    3 -> mImage.reset(mEdgeTexture)
                }
                mFilter.draw(mImage)
                mImage.clear()
            }
        }
    }

    class Texture2DFilter : GLFilter() {
        private val mTexture2DProgram = Texture2DProgram(Target.TEXTURE_2D)

        private var mTextureCoordinates: FloatArray? = null

        override fun onInit() {
            mTexture2DProgram.init()
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            imageInOut.texture?.let { mTexture2DProgram.setTexture(it) }
            mTexture2DProgram.isMirrorY(true)
            mTexture2DProgram.setScaleType(ScaleType.FIT_XY)
            mContext?.let { mTexture2DProgram.setTargetSize(it.displaySize) }
            mTextureCoordinates?.let { mTexture2DProgram.setTextureCoordinates(it) }
            mTexture2DProgram.draw()
        }

        override fun onRelease() {
            mTexture2DProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {
            updateData.getFloat("texturePositionMax", 0F)
                .takeIf { it != 0F }
                ?.let {
                    mTextureCoordinates = floatArrayOf(
                        0.0f, 0.0f,     // 左下
                        0.0f, it,     // 左上
                        it, 0.0f,     // 右下
                        it, it      // 右上
                    )
                }
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}
    }
}