package com.jiangpengyong.sample.c_drawing_mode

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.widget.RadioButton
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
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/6/16 15:03
 * @email 56002982@qq.com
 * @des 绘制方式
 */
class DrawingModeActivity : AppCompatActivity() {
    companion object {
        const val DRAWING_MODE = "drawing_mode"
        const val CULL_FACE_STATE_MODE = "cull_face_state_mode"
        const val CULL_FACE_ORIENTATION = "cull_face_orientation"
    }

    private lateinit var mRenderView: RenderView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing_mode)

        mRenderView = findViewById(R.id.surface_view)

        val drawingModel = findViewById<RadioGroup>(R.id.drawing_mode)
        drawingModel.setOnCheckedChangeListener { _, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.gl_points -> bundle.putInt(DRAWING_MODE, DrawingMode.Points.value)
                R.id.gl_lines -> bundle.putInt(DRAWING_MODE, DrawingMode.Lines.value)
                R.id.gl_line_strip -> bundle.putInt(DRAWING_MODE, DrawingMode.LineStrip.value)
                R.id.gl_line_loop -> bundle.putInt(DRAWING_MODE, DrawingMode.LineLoop.value)
                R.id.gl_triangles -> bundle.putInt(DRAWING_MODE, DrawingMode.Triangles.value)
                R.id.gl_triangle_strip -> bundle.putInt(DRAWING_MODE, DrawingMode.TriangleStrip.value)
                R.id.gl_triangle_fan -> bundle.putInt(DRAWING_MODE, DrawingMode.TriangleFan.value)
            }
            mRenderView.updateFilterData(bundle)
            mRenderView.requestRender()
        }

        val cullFaceOrientation = findViewById<RadioGroup>(R.id.cull_face_direction)
        cullFaceOrientation.setOnCheckedChangeListener { _, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.cw -> bundle.putInt(CULL_FACE_ORIENTATION, CullFaceOrientation.CW.value)
                R.id.ccw -> bundle.putInt(CULL_FACE_ORIENTATION, CullFaceOrientation.CCW.value)
            }
            mRenderView.updateFilterData(bundle)
            mRenderView.requestRender()
        }

        val cullFaceStateModel = findViewById<RadioGroup>(R.id.cull_face_state_mode)
        cullFaceStateModel.setOnCheckedChangeListener { _, checkedId ->
            val bundle = Bundle()
            when (checkedId) {
                R.id.enable_cull_face -> {
                    findViewById<RadioButton>(R.id.cw).isEnabled = true
                    findViewById<RadioButton>(R.id.ccw).isEnabled = true
                    bundle.putInt(CULL_FACE_STATE_MODE, CullFaceState.Enable.value)
                }

                R.id.disable_cull_face -> {
                    findViewById<RadioButton>(R.id.cw).isEnabled = false
                    findViewById<RadioButton>(R.id.ccw).isEnabled = false
                    bundle.putInt(CULL_FACE_STATE_MODE, CullFaceState.Disable.value)
                }
            }
            mRenderView.updateFilterData(bundle)
            mRenderView.requestRender()
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
            private val mFilter = StarFilter().apply {
                id = "StarFilter"
                name = "StarFilter"
            }
            private val mContext = FilterContext()
            private val mImage = ImageInOut()
            private var mBundle: Bundle? = null

            private var mCullFaceState = CullFaceState.Disable
            private var mCullFaceOrientation = CullFaceOrientation.CCW

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                if (mCullFaceState == CullFaceState.Enable) {
                    GLES20.glEnable(GLES20.GL_CULL_FACE)
                    if (mCullFaceOrientation == CullFaceOrientation.CW) {
                        GLES20.glFrontFace(GLES20.GL_CW)
                    } else {
                        GLES20.glFrontFace(GLES20.GL_CCW)
                    }
                } else {
                    GLES20.glDisable(GLES20.GL_CULL_FACE)
                }

                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                synchronized(this) {
                    val temp = mBundle
                    mBundle = null
                    temp
                }?.let { mFilter.updateData("StarFilter", it) }
                mFilter.draw(mImage)
            }

            fun updateFilterData(bundle: Bundle) {
                synchronized(this) {
                    mBundle = bundle
                    bundle.getInt(CULL_FACE_STATE_MODE, -1).takeIf { it != -1 }?.let {
                        mCullFaceState = if (it == CullFaceState.Enable.value) {
                            CullFaceState.Enable
                        } else {
                            CullFaceState.Disable
                        }
                    }
                    bundle.getInt(CULL_FACE_ORIENTATION, -1).takeIf { it != -1 }?.let {
                        mCullFaceOrientation = if (it == CullFaceOrientation.CW.value) {
                            CullFaceOrientation.CW
                        } else {
                            CullFaceOrientation.CCW
                        }
                    }
                }
            }
        }
    }
}

private enum class DrawingMode(val value: Int) {
    Points(GLES20.GL_POINTS),
    Lines(GLES20.GL_LINES),
    LineStrip(GLES20.GL_LINE_STRIP),
    LineLoop(GLES20.GL_LINE_LOOP),
    Triangles(GLES20.GL_TRIANGLES),
    TriangleStrip(GLES20.GL_TRIANGLE_STRIP),
    TriangleFan(GLES20.GL_TRIANGLE_FAN),
}

private enum class CullFaceState(val value: Int) {
    Enable(1),
    Disable(2),
}

private enum class CullFaceOrientation(val value: Int) {
    CW(GLES20.GL_CW),
    CCW(GLES20.GL_CCW),
}

private class StarFilter : GLFilter() {
    private val mStarProgram = StarProgram()
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()
    private var mDisplaySize = Size(0, 0)
    private var mDrawingMode = DrawingMode.Points.value

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

    override fun onUpdateData(updateData: Bundle) {
        mDrawingMode = updateData.getInt(DrawingModeActivity.DRAWING_MODE, -1).takeIf { it != -1 } ?: return
    }

    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    private fun drawStar() {
        mStarProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
        mStarProgram.setMode(mDrawingMode)
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
    // 外圆半径
    private val outerRadius = 1F

    // 内圆半径
    private val innerRadius = outerRadius * 0.382F
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            // 第一个点
            outerRadius * sin(0.toRadians()).toFloat(), outerRadius * cos(0.toRadians()).toFloat(), 0F,
            // 第二个点
            innerRadius * sin(36.toRadians()).toFloat(), innerRadius * cos(36.toRadians()).toFloat(), 0F,
            // 第三个点
            outerRadius * sin(72.toRadians()).toFloat(), outerRadius * cos(72.toRadians()).toFloat(), 0F,
            // 第四个点
            innerRadius * sin(108.toRadians()).toFloat(), innerRadius * cos(108.toRadians()).toFloat(), 0F,
            // 第五个点
            outerRadius * sin(144.toRadians()).toFloat(), outerRadius * cos(144.toRadians()).toFloat(), 0F,
            // 第六个点
            innerRadius * sin(180.toRadians()).toFloat(), innerRadius * cos(180.toRadians()).toFloat(), 0F,
            // 第七个点
            outerRadius * sin(216.toRadians()).toFloat(), outerRadius * cos(216.toRadians()).toFloat(), 0F,
            // 第八个点
            innerRadius * sin(252.toRadians()).toFloat(), innerRadius * cos(252.toRadians()).toFloat(), 0F,
            // 第九个点
            outerRadius * sin(288.toRadians()).toFloat(), outerRadius * cos(288.toRadians()).toFloat(), 0F,
            // 第十个点
            innerRadius * sin(324.toRadians()).toFloat(), innerRadius * cos(324.toRadians()).toFloat(), 0F,
        )
    )
    private var mColorBuffer = allocateFloatBuffer(
        floatArrayOf(
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
            1F, 0F, 0F, 0F,
        )
    )

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private val mVertexCount = 10
    private var mMatrix: GLMatrix = GLMatrix()

    private var mDrawingMode: Int = DrawingMode.Points.value

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    fun setMode(mode: Int) {
        mDrawingMode = mode
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
        mColorHandle = getAttribLocation("aColor")

        GLES20.glLineWidth(10.0F)
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glDrawArrays(mDrawingMode, 0, mVertexCount)
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
            gl_PointSize = 10.0;
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
