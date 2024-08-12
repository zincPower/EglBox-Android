package com.jiangpengyong.sample.e_texture

import android.content.Context
import android.graphics.BitmapFactory
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
import com.jiangpengyong.eglbox_core.gles.MagFilter
import com.jiangpengyong.eglbox_core.gles.MinFilter
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.Texture2DProgram
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * @author jiang peng yong
 * @date 2024/8/11 16:46
 * @email 56002982@qq.com
 * @des 纹理采样模式
 */
class TextureSampleActivity : AppCompatActivity() {
    companion object {
        const val MIN_NEAREST = 0b0001
        const val MIN_LINEAR = 0b0010
        const val MAG_NEAREST = 0b0100
        const val MAG_LINEAR = 0b1000
    }

    private lateinit var mRenderView: RenderView

    private var filterMode = MIN_NEAREST or MAG_NEAREST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_sample)
        mRenderView = findViewById(R.id.surface_view)

        findViewById<RadioGroup>(R.id.min_filter).setOnCheckedChangeListener { _, checkedId ->
            filterMode = filterMode and MIN_NEAREST.inv()
            filterMode = filterMode and MIN_LINEAR.inv()
            if (checkedId == R.id.min_nearest) {
                filterMode = filterMode or MIN_NEAREST
            } else if (checkedId == R.id.min_linear) {
                filterMode = filterMode or MIN_LINEAR
            }
            mRenderView.updateFilterData(Bundle().apply { putInt("filterMode", filterMode) })
            mRenderView.requestRender()
        }

        findViewById<RadioGroup>(R.id.mag_filter).setOnCheckedChangeListener { _, checkedId ->
            filterMode = filterMode and MAG_NEAREST.inv()
            filterMode = filterMode and MAG_LINEAR.inv()
            if (checkedId == R.id.mag_nearest) {
                filterMode = filterMode or MAG_NEAREST
            } else if (checkedId == R.id.mag_linear) {
                filterMode = filterMode or MAG_LINEAR
            }
            mRenderView.updateFilterData(Bundle().apply { putInt("filterMode", filterMode) })
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
            private val mFilterId = "Texture2DFilter"
            private val mFilter = Texture2DFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)

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
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mFilter.draw(mImage)
                mImage.clear()
            }
        }
    }

    class Texture2DFilter : GLFilter() {
        private val mTexture2DProgram = Texture2DProgram(Target.TEXTURE_2D)
        private var mFilterMode = MIN_NEAREST or MAG_NEAREST

        private val mMinNearestTexture = GLTexture(
            minFilter = MinFilter.NEAREST,
            magFilter = MagFilter.NEAREST,
        )
        private val mMinLinearTexture = GLTexture(
            minFilter = MinFilter.LINEAR,
            magFilter = MagFilter.LINEAR,
        )
        private val mMagNearestTexture = GLTexture(
            minFilter = MinFilter.NEAREST,
            magFilter = MagFilter.NEAREST,
        )
        private val mMagLinearTexture = GLTexture(
            minFilter = MinFilter.LINEAR,
            magFilter = MagFilter.LINEAR,
        )

        override fun onInit() {
            mTexture2DProgram.init()

            mMinNearestTexture.init()
            mMinLinearTexture.init()
            mMagNearestTexture.init()
            mMagLinearTexture.init()

            BitmapFactory.decodeFile(File(App.context.filesDir, "images/fire_big.jpg").absolutePath).let { bitmap ->
                mMinNearestTexture.setData(bitmap)
                mMinLinearTexture.setData(bitmap)
                bitmap.recycle()
            }
            BitmapFactory.decodeFile(File(App.context.filesDir, "images/fire_small.jpg").absolutePath).let { bitmap ->
                mMagNearestTexture.setData(bitmap)
                mMagLinearTexture.setData(bitmap)
                bitmap.recycle()
            }
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            mTexture2DProgram.reset()
            mTexture2DProgram.setScaleType(ScaleType.MATRIX)
            mTexture2DProgram.setTargetSize(context.displaySize)

            val smallTexture = if (mFilterMode and MIN_NEAREST == MIN_NEAREST) {
                mMinNearestTexture
            } else if (mFilterMode and MIN_LINEAR == MIN_LINEAR) {
                mMinLinearTexture
            } else {
                null
            }
            smallTexture?.let {
                mTexture2DProgram.setTexture(it)
                val matrix = VertexAlgorithmFactory.calculate(
                    ScaleType.CENTER_INSIDE,
                    context.displaySize,
                    Size(it.width, it.height),
                )
                matrix.scale(0.8F, -0.8F, 1F)
                matrix.translate(-1.3F, 0F, 0F)
                mTexture2DProgram.setVertexMatrix(matrix.matrix)
                mTexture2DProgram.draw()
            }

            val bigTexture = if (mFilterMode and MAG_NEAREST == MAG_NEAREST) {
                mMagNearestTexture
            } else if (mFilterMode and MAG_LINEAR == MAG_LINEAR) {
                mMagLinearTexture
            } else {
                null
            }

            bigTexture?.let {
                mTexture2DProgram.setTexture(it)
                val matrix = VertexAlgorithmFactory.calculate(
                    ScaleType.CENTER_INSIDE,
                    context.displaySize,
                    Size(it.width, it.height),
                )
                matrix.scale(0.8F, -0.8F, 1F)
                matrix.translate(1.3F, 0F, 0F)
                mTexture2DProgram.setVertexMatrix(matrix.matrix)
                mTexture2DProgram.draw()
            }
        }

        override fun onRelease() {
            mTexture2DProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {
            updateData.getInt("filterMode", 0)
                .takeIf { it != 0 }
                ?.let { mFilterMode = it }
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}
    }
}