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
import com.jiangpengyong.eglbox_core.utils.allocateByteBuffer
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.utils.SizeUtils
import java.io.File
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * @author jiang peng yong
 * @date 2024/8/22 08:41
 * @email 56002982@qq.com
 * @des 点精灵——纹理数组
 */
class PointSpriteArrayActivity : AppCompatActivity() {

    private lateinit var mRenderView: RenderView
    private lateinit var mPointSizeView: SeekBar
    private val maxSize = SizeUtils.dp2px(35F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_point_sprite_array)

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

            private val mResourcePathList = arrayListOf(
                File(App.context.filesDir, "images/texture_image/snowflake.png").absolutePath,
                File(App.context.filesDir, "images/texture_image/snowflake1.png").absolutePath,
                File(App.context.filesDir, "images/texture_image/snowflake2.png").absolutePath,
                File(App.context.filesDir, "images/texture_image/snowflake3.png").absolutePath,
                File(App.context.filesDir, "images/texture_image/snowflake4.png").absolutePath,
                File(App.context.filesDir, "images/texture_image/snowflake5.png").absolutePath,
                File(App.context.filesDir, "images/texture_image/snowflake6.png").absolutePath,
            )
            private val mResourceSize = Size(200, 200)

            fun updateFilterData(bundle: Bundle) {
                mFilter.updateData(mFilterId, bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
                mTexture.init()
                // 设置纹理数组数据
                val perPicByteCount = mResourceSize.width * mResourceSize.height * 4
                val byteBuffer = allocateByteBuffer(perPicByteCount * mResourcePathList.size)
                mResourcePathList.forEachIndexed { index, path ->
                    BitmapFactory.decodeFile(path).apply {
                        byteBuffer.position(perPicByteCount * index)
                        copyPixelsToBuffer(byteBuffer)
                        recycle()
                    }
                }
                byteBuffer.position(0)
                EGLBox.checkError("jiang6")

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, mTexture.id)
                EGLBox.checkError("jiang5")
                GLES20.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
                GLES20.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
                GLES20.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
                GLES20.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_WRAP_R, GLES20.GL_CLAMP_TO_EDGE)

                EGLBox.checkError("jiang4")
                GLES30.glTexImage3D(
                    GLES30.GL_TEXTURE_2D_ARRAY,
                    0,
                    GLES30.GL_RGBA8,
                    mResourceSize.width,
                    mResourceSize.height,
                    mResourcePathList.size,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    byteBuffer
                )
                EGLBox.checkError("jiang2")
                GLES20.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, 0)
                EGLBox.checkError("jiang3")
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
        private val mProgram = PointSpritArrayProgram()

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

            // 开启纹理数组
            GLES20.glEnable(GLES30.GL_TEXTURE_2D_ARRAY)

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

    class PointSpritArrayProgram : GLProgram() {
        private val mVertexBuffer: FloatBuffer
        private val mVertexCount: Int

        private var mMVPMatrixHandle = 0
        private var mPositionHandle = 0
        private var mPointSizeHandle = 0

        private var mMatrix: GLMatrix = GLMatrix()
        private var mTexture: GLTexture = GLTexture()
        private var mPointSize: Float = 0F

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
        }

        override fun onDraw() {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, mTexture.id)
            EGLBox.checkError("jiang1")

            GLES20.glVertexAttrib1f(mPointSizeHandle, mPointSize)
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mVertexCount)
            GLES20.glDisableVertexAttribArray(mPositionHandle)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, 0)
        }

        override fun onRelease() {
            mMVPMatrixHandle = 0
            mPositionHandle = 0
            mPointSizeHandle = 0
        }

        override fun getVertexShaderSource(): String = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec3 aPosition;
            in float aPointSize;
            out float vid;
            void main() {
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
                gl_PointSize = aPointSize;
                vid = float(gl_VertexID);
            }
        """.trimIndent()

        override fun getFragmentShaderSource(): String = """
            #version 300 es
            precision mediump float;
            uniform mediump sampler2DArray sTexture;
            in float vid;
            out vec4 fragColor;
            void main() {
                // 为了兼容某些驱动，直接使用 gl_PointCoord 作为 texture 参数会有问题
                vec3 texCoor = vec3(gl_PointCoord.st, vid);
                fragColor = texture(sTexture, texCoor);
            }
        """.trimIndent()
    }
}