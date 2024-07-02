package com.jiangpengyong.sample.c_drawing_mode

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.AttributeSet
import android.util.Size
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.gles.GLProgram
import com.jiangpengyong.eglbox.utils.GLMatrix
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.ProjectMatrix
import com.jiangpengyong.eglbox.utils.ViewMatrix
import com.jiangpengyong.eglbox.utils.allocateFloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/6/16 15:03
 * @email 56002982@qq.com
 * @des 绘制方式
 */
class DrawingModeActivity : AppCompatActivity() {
    companion object {
        private const val MODE = "mode"
    }

    private lateinit var mRenderView: RenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing_mode)

        mRenderView = findViewById(R.id.surface_view)

        val model = findViewById<RadioGroup>(R.id.drawing_mode)
        model.setOnCheckedChangeListener { _, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.gl_points -> bundle.putInt(MODE, DrawingMode.Points.value)
                R.id.gl_lines -> bundle.putInt(MODE, DrawingMode.Lines.value)
                R.id.gl_line_strip -> bundle.putInt(MODE, DrawingMode.LineStrip.value)
                R.id.gl_line_loop -> bundle.putInt(MODE, DrawingMode.LineLoop.value)
                R.id.gl_triangles -> bundle.putInt(MODE, DrawingMode.Triangles.value)
                R.id.gl_triangle_strip -> bundle.putInt(MODE, DrawingMode.TriangleStrip.value)
                R.id.gl_triangle_fan -> bundle.putInt(MODE, DrawingMode.TriangleFan.value)
            }
            mRenderView.updateFilterData(bundle)
        }
    }

    class RenderView : GLSurfaceView {
        private val mRenderer = Renderer()

        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        fun updateFilterData(bundle: Bundle) {
            mRenderer.updateFilterData(bundle)
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilter = StarFilter()
            private val mContext = FilterContext()
            private val mImage = ImageInOut()
            private var mBundle: Bundle? = null

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                mFilter.init(mContext)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                synchronized(this) {
                    val temp = mBundle
                    mBundle = null
                    temp
                }?.let { mFilter.updateData(it) }
                mFilter.draw(mImage)
            }

            fun updateFilterData(bundle: Bundle) {
                synchronized(this) { mBundle = bundle }
            }
        }
    }

    enum class DrawingMode(val value: Int) {
        Points(1), Lines(2), LineStrip(3), LineLoop(4), Triangles(5), TriangleStrip(6), TriangleFan(7),
    }
}

private class StarFilter : GLFilter() {
    private val mStarProgram = StarProgram()
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()
    private var mDisplaySize = Size(0, 0)

    override fun onInit() {
        mStarProgram.init()
        mViewMatrix.setLookAtM(
            0F, 0F, 5F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
        mModelMatrix.scale(0.8F, 0.8F, 1F)
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        updateProjectionMatrix(context)
        drawStar()
    }

    override fun onRelease() {
        mStarProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}
    override fun onRestoreData(restoreData: Bundle) {}
    override fun onSaveData(saveData: Bundle) {}

    private fun drawStar() {
        mStarProgram.setColor(0F / 255F, 50F / 255F, 133F / 255F, 0F)
        mStarProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
        mStarProgram.draw()
    }

    // 为了方便观察，使用正交投影
    private fun updateProjectionMatrix(context: FilterContext) {
        val displaySize = context.displaySize
        if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
            val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
            if (displaySize.width > displaySize.height) {
                mProjectMatrix.setOrthoM(
                    -ratio, ratio,
                    -1F, 1F,
                    2F, 20F
                )
            } else {
                mProjectMatrix.setOrthoM(
                    -1F, 1F,
                    -ratio, ratio,
                    2F, 20F
                )
            }
            mDisplaySize = displaySize
        }
    }
}

private class StarProgram : GLProgram() {
    private val radius = 1F
    private val ratio = radius * 0.382F
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            radius * sin(0.toRadians()).toFloat(), radius * cos(0.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(36.toRadians()).toFloat(), ratio * cos(36.toRadians()).toFloat(), 0F,

            ratio * sin(36.toRadians()).toFloat(), ratio * cos(36.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(72.toRadians()).toFloat(), radius * cos(72.toRadians()).toFloat(), 0F,

            radius * sin(72.toRadians()).toFloat(), radius * cos(72.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(108.toRadians()).toFloat(), ratio * cos(108.toRadians()).toFloat(), 0F,

            ratio * sin(108.toRadians()).toFloat(), ratio * cos(108.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(144.toRadians()).toFloat(), radius * cos(144.toRadians()).toFloat(), 0F,

            radius * sin(144.toRadians()).toFloat(), radius * cos(144.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(180.toRadians()).toFloat(), ratio * cos(180.toRadians()).toFloat(), 0F,

            ratio * sin(180.toRadians()).toFloat(), ratio * cos(180.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(216.toRadians()).toFloat(), radius * cos(216.toRadians()).toFloat(), 0F,

            radius * sin(216.toRadians()).toFloat(), radius * cos(216.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(252.toRadians()).toFloat(), ratio * cos(252.toRadians()).toFloat(), 0F,

            ratio * sin(252.toRadians()).toFloat(), ratio * cos(252.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(288.toRadians()).toFloat(), radius * cos(288.toRadians()).toFloat(), 0F,

            radius * sin(288.toRadians()).toFloat(), radius * cos(288.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            ratio * sin(324.toRadians()).toFloat(), ratio * cos(324.toRadians()).toFloat(), 0F,

            ratio * sin(324.toRadians()).toFloat(), ratio * cos(324.toRadians()).toFloat(), 0F,
            0F, 0F, 0F,
            radius * sin(0.toRadians()).toFloat(), radius * cos(0.toRadians()).toFloat(), 0F,
        )
    )
    private var mColorBuffer = allocateFloatBuffer(
        floatArrayOf(
            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,

            1F, 158F / 255F, 170F / 255F, 0F,
            1F, 208F / 255F, 208F / 255F, 0F,
            1F, 158F / 255F, 170F / 255F, 0F,
        )
    )

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private val mVertexCount = 30
    private var mMatrix: GLMatrix = GLMatrix()

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    fun setColor(red: Float, green: Float, blue: Float, alpha: Float) {
        val colors = FloatArray(mVertexCount * 4)
        for (i in 0 until (mVertexCount / 3)) {
            colors[i * 12 + 0] = red
            colors[i * 12 + 1] = green
            colors[i * 12 + 2] = blue
            colors[i * 12 + 3] = alpha

            colors[i * 12 + 4] = max(red + 0.2F, 0F)
            colors[i * 12 + 5] = max(green + 0.2F, 0F)
            colors[i * 12 + 6] = max(blue + 0.2F, 0F)
            colors[i * 12 + 7] = alpha

            colors[i * 12 + 8] = red
            colors[i * 12 + 9] = green
            colors[i * 12 + 10] = blue
            colors[i * 12 + 11] = alpha
        }
        mColorBuffer = allocateFloatBuffer(colors)
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
        mColorHandle = getAttribLocation("aColor")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
    }

    override fun onRelease() {
        mVertexBuffer.clear()
        mColorBuffer.clear()
        mMVPMatrixHandle = 0
        mPositionHandle = 0
        mColorHandle = 0
    }

    override fun getVertexShaderSource(): String = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        in vec3 aPosition;
        in vec4 aColor;
        out vec4 vColor;
        void main() {
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            vColor = aColor;
        }
    """.trimIndent()

    override fun getFragmentShaderSource(): String = """
        #version 300 es
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        void main() {
            fragColor = vColor;
        }
    """.trimIndent()
}

private fun Int.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}
