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
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.EGLBox
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.utils.SizeUtils
import java.io.File
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10


/**
 * @author jiang peng yong
 * @date 2024/8/22 08:41
 * @email 56002982@qq.com
 * @des 采样器配置对象
 */
class SamplerActivity : AppCompatActivity() {

    private lateinit var mRenderView: RenderView
    private lateinit var mPointSizeView: SeekBar
    private val maxSize = SizeUtils.dp2px(35F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_sampler)

        mRenderView = findViewById(R.id.surface_view)
        mPointSizeView = findViewById<SeekBar>(R.id.point_size).apply {
            max = 80
            progress = 50
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updatePointSize(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        mRenderView.post {
            updatePointSize(mPointSizeView.progress)
        }
    }

    private fun updatePointSize(progress: Int) {
        val realProgression = Math.min(progress + 20, 100)
        val size = realProgression / 100F
        mRenderView.updateFilterData(Bundle().apply {
            putFloat("pointSize", size * maxSize)
        })
        mRenderView.requestRender()
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
            private val mFilterId = "PointSpriteProgram"
            private val mFilter = PointSpriteFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mTexture = GLTexture()
            private val mImage = ImageInOut()

            fun updateFilterData(bundle: Bundle) {
                mFilter.updateData(mFilterId, bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
                mTexture.init()

                BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/snowflake.png").absolutePath).let { bitmap ->
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
                mFilter.draw(mImage)
                mImage.clear()
            }
        }
    }

    class PointSpriteFilter : GLFilter() {
        private val mProgram = PointSpriteProgram()

        private val mProjectMatrix = ProjectMatrix()
        private val mViewMatrix = ViewMatrix()
        private val mModelMatrix = ModelMatrix()

        private var mDisplaySize = Size(0, 0)

        private var mPointSize = 0F

        override fun onInit() {
            mProgram.init()
            mViewMatrix.setLookAtM(
                0F, 0F, 10F,
                0F, 0F, 0F,
                0F, 1F, 0F
            )
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)

            // 开启混合，否则透明通道会遮挡后面的纹理
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            updateProjectionMatrix(context)
            imageInOut.texture?.let { mProgram.setTexture(it) }
            mProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
            synchronized(this) {
                mProgram.setPointSize(mPointSize)
            }
            mProgram.draw()
        }

        override fun onRelease() {
            mProgram.release()
        }

        override fun onUpdateData(updateData: Bundle) {
            synchronized(this) {
                updateData.getFloat("pointSize", 0F)
                    .takeIf { it != 0F }
                    ?.let { mPointSize = it }
            }
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {}

        private fun updateProjectionMatrix(context: FilterContext) {
            val displaySize = context.displaySize
            if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
                if (displaySize.width > displaySize.height) {
                    val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
                    mProjectMatrix.setFrustumM(
                        -ratio, ratio,
                        -1F, 1F,
                        5F, 30F
                    )
                } else {
                    val ratio = displaySize.height.toFloat() / displaySize.width.toFloat()
                    mProjectMatrix.setFrustumM(
                        -1F, 1F,
                        -ratio, ratio,
                        5F, 30F
                    )
                }
                mDisplaySize = displaySize
            }
        }
    }

    class PointSpriteProgram : GLProgram() {
        private val mVertexBuffer: FloatBuffer
        private val mVertexCount: Int

        private var mMVPMatrixHandle = 0
        private var mPositionHandle = 0
        private var mPointSizeHandle = 0

        private var mMatrix: GLMatrix = GLMatrix()
        private var mTexture: GLTexture = GLTexture()
        private var mPointSize: Float = 0F

        private var mSamplerId = 0

        init {
            val vertexList = floatArrayOf(
                0F, 0F, 0F,
                0F, 0.5F, -5F,
                0.5F, 0F, -3F,
                -0.35F, -0.3F, 2F,
                -1F, 0.3F, 1F,
                0.45F, 0.54F, 1.5F,
                -1F, -1F, -1.5F,
            )
            mVertexBuffer = allocateFloatBuffer(vertexList)
            mVertexCount = vertexList.size / 3
        }

        fun setMatrix(matrix: GLMatrix) {
            mMatrix = matrix
        }

        fun setTexture(texture: GLTexture) {
            mTexture = texture
        }

        fun setPointSize(value: Float) {
            mPointSize = value
        }

        override fun onInit() {
            mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
            mPositionHandle = getAttribLocation("aPosition")
            mPointSizeHandle = getAttribLocation("aPointSize")

            val samplerIds = IntArray(1)
            GLES30.glGenSamplers(1, samplerIds, 0)
            mSamplerId = samplerIds[0]
            GLES30.glSamplerParameteri(mSamplerId, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES30.glSamplerParameteri(mSamplerId, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES30.glSamplerParameteri(mSamplerId, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES30.glSamplerParameteri(mSamplerId, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        }

        override fun onDraw() {
            mTexture.bind()
            // 需要在这里进行使用，而且不是使用纹理 id
            GLES30.glBindSampler(0, mSamplerId)
            GLES20.glVertexAttrib1f(mPointSizeHandle, mPointSize)
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mVertexCount)
            GLES20.glDisableVertexAttribArray(mPositionHandle)
            mTexture.unbind()
        }

        override fun onRelease() {
            mMVPMatrixHandle = 0
            mPositionHandle = 0
            mPointSizeHandle = 0
            GLES30.glDeleteSamplers(1, intArrayOf(mSamplerId), 0)
        }

        override fun getVertexShaderSource(): String = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec3 aPosition;
            in float aPointSize;
            void main() {
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
                gl_PointSize = aPointSize;
            }
        """.trimIndent()

        override fun getFragmentShaderSource(): String = """
            #version 300 es
            precision mediump float;
            uniform sampler2D sTexture;
            out vec4 fragColor;
            void main() {
                // 为了兼容某些驱动，直接使用 gl_PointCoord 作为 texture 参数会有问题
                vec2 texCoor = gl_PointCoord;
                fragColor = texture(sTexture, texCoor);
            }
        """.trimIndent()
    }
}