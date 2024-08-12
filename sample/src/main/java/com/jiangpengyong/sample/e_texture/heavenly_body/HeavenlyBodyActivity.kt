package com.jiangpengyong.sample.e_texture.heavenly_body

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/8/12 21:57
 * @email 56002982@qq.com
 * @des 天体
 */
class HeavenlyBodyActivity : AppCompatActivity() {
    companion object {
        private const val TOUCH_SCALE_FACTOR = 1 / 4F
        private const val RESET = 10000
    }

    private lateinit var mRenderView: RenderView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_heavenly_body)
        mRenderView = findViewById(R.id.surface_view)

//        findViewById<View>(R.id.reset).setOnClickListener {
//            mRenderView.sendMessageToFilter(Message.obtain().apply { what = RESET })
//            mRenderView.requestRender()
//        }
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

        // 上次触控位置坐标
        private var mBeforeY = 0f
        private var mBeforeX = 0f

        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        fun updateFilterData(bundle: Bundle) {
            mRenderer.updateFilterData(bundle)
        }

        fun sendMessageToFilter(message: Message) {
            mRenderer.sendMessageToFilter(message)
        }

        init {
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent?): Boolean {
            event ?: return false
            val y = event.y
            val x = event.x
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dy: Float = y - mBeforeY
                    val yAngle = dy * TOUCH_SCALE_FACTOR

                    val dx: Float = x - mBeforeX
                    val xAngle = dx * TOUCH_SCALE_FACTOR

                    mRenderer.updateFilterData(Bundle().apply {
                        putFloat("xAngle", xAngle)
                        putFloat("yAngle", yAngle)
                    })
                    requestRender()
                }
            }
            mBeforeY = y
            mBeforeX = x
            return true
        }

        private class Renderer : GLSurfaceView.Renderer {
            private val mFilterId = "BallFilter"
            private val mBallFilter = HeavenlyBodyFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()

            fun updateFilterData(bundle: Bundle) {
                mBallFilter.updateData(mFilterId, bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mBallFilter.init(mContext)
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                GLES20.glFrontFace(GLES20.GL_CW)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.displaySize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mBallFilter.draw(mImage)
            }

            fun sendMessageToFilter(message: Message) {
                mBallFilter.receiveMessage(mFilterId, message)
            }
        }
    }

    class HeavenlyBodyFilter : GLFilter() {
        private val mProgram = HeavenlyBodyProgram()

        private val mProjectMatrix = ProjectMatrix()
        private val mViewMatrix = ViewMatrix()
        private val mModelMatrix = ModelMatrix()

        private var mXAngle = 0F
        private var mYAngle = 0F

        private var mDisplaySize = Size(0, 0)

        private var mLightPosition = floatArrayOf(0F, 0F, 5F)
        private var mCameraPosition = floatArrayOf(0F, 0F, 10F)

        private var mTexture = GLTexture()

        override fun onInit() {
            mProgram.init()
            mViewMatrix.setLookAtM(
                mCameraPosition[0], mCameraPosition[1], mCameraPosition[2],
                0F, 0F, 0F,
                0F, 1F, 0F
            )
            mTexture.init()
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_earth_daymap.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_earth_nightmap.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_earth_clouds.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_moon.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_jupiter.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_mars.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_mercury.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_neptune.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_saturn.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_sun.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_uranus.jpg").absolutePath).let { bitmap ->
//            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_venus_atmosphere.jpg").absolutePath).let { bitmap ->
            BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_venus_surface.jpg").absolutePath).let { bitmap ->
                mTexture.setData(bitmap)
                bitmap.recycle()
            }
        }

        override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
            GLES20.glClearColor(0F, 0F, 0F, 1F)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            updateProjectionMatrix(context)
            synchronized(this) {
                mProgram.setTexture(mTexture)
                mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
                mProgram.setMMatrix(mModelMatrix)
                mProgram.setLightPosition(mLightPosition)
                mProgram.setCameraPosition(mCameraPosition)
                mProgram.draw()
            }
        }

        override fun onRelease() {
            mProgram.release()
        }

        private fun updateProjectionMatrix(context: FilterContext) {
            val displaySize = context.displaySize
            if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
                val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
                if (displaySize.width > displaySize.height) {
                    mProjectMatrix.setFrustumM(
                        -ratio, ratio,
                        -1F, 1F,
                        5F, 20F
                    )
                } else {
                    mProjectMatrix.setFrustumM(
                        -1F, 1F,
                        -ratio, ratio,
                        5F, 20F
                    )
                }
                mDisplaySize = displaySize
            }
        }

        override fun onUpdateData(updateData: Bundle) {
            synchronized(this) {
                mXAngle += updateData.getFloat("xAngle", 0F)
                mYAngle += updateData.getFloat("yAngle", 0F)
                mModelMatrix.reset()
                mModelMatrix.rotate(mXAngle, 0F, 1F, 0F)
                mModelMatrix.rotate(mYAngle, 1F, 0F, 0F)
            }
        }

        override fun onRestoreData(inputData: Bundle) {}
        override fun onStoreData(outputData: Bundle) {}
        override fun onReceiveMessage(message: Message) {
            synchronized(this) {
                if (message.what == RESET) {
                    mXAngle = 0F
                    mYAngle = 0F
                    mModelMatrix.reset()
                }
            }
        }
    }
}