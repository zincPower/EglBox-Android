package com.jiangpengyong.sample.e_texture.planet

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_sample.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author jiang peng yong
 * @date 2024/8/12 21:57
 * @email 56002982@qq.com
 * @des 太阳系
 */
class SolarSystemActivity : AppCompatActivity() {
    companion object {
        private const val TOUCH_SCALE_FACTOR = 1 / 4F
        const val MESSAGE_TARGET = 100002
    }

    private lateinit var mRenderView: RenderView

    private var mEyeTarget = SolarSystemFilter.Target.SolarSystem
    private val mFloatAnimation = ValueAnimator.ofFloat(0F, 1F).apply {
        setDuration(2000)
        interpolator = DecelerateInterpolator()
        addUpdateListener { animator ->
            val value = animator.animatedValue as? Float ?: return@addUpdateListener
            mRenderView.sendMessageToFilter(Message.obtain().apply {
                what = MESSAGE_TARGET
                arg1 = mEyeTarget.value
                obj = value
            })
        }
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = object : Runnable {
        override fun run() {
            mRenderView.requestRender()
            mHandler.postDelayed(this, 10)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solar_system)
        mRenderView = findViewById(R.id.surface_view)

        mHandler.postDelayed(mRunnable, 10)

        findViewById<View>(R.id.solar_system).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.SolarSystem)
        }
        findViewById<View>(R.id.mercury).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Mercury)
        }
        findViewById<View>(R.id.venus).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Venus)
        }
        findViewById<View>(R.id.earth).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Earth)
        }
        findViewById<View>(R.id.mars).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Mars)
        }
        findViewById<View>(R.id.jupiter).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Jupiter)
        }
        findViewById<View>(R.id.saturn).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Saturn)
        }
        findViewById<View>(R.id.uranus).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Uranus)
        }
        findViewById<View>(R.id.neptune).setOnClickListener {
            updateTarget(SolarSystemFilter.Target.Neptune)
        }
    }

    private fun updateTarget(target: SolarSystemFilter.Target) {
        if (mEyeTarget == target) return
        if (mFloatAnimation.isRunning) mFloatAnimation.cancel()
        mEyeTarget = target
        mFloatAnimation.start()
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
            private val mFilterId = "SolarSystemFilter"
            private val mFilter = SolarSystemFilter().apply { id = mFilterId }
            private val mContext = FilterContext(RenderType.OnScreen)
            private val mImage = ImageInOut()

            fun updateFilterData(bundle: Bundle) {
                mFilter.updateData(mFilterId, bundle)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                mFilter.init(mContext)
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                GLES20.glFrontFace(GLES20.GL_CW)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                mContext.previewSize = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                mFilter.draw(mImage)
            }

            fun sendMessageToFilter(message: Message) {
                mFilter.receiveMessage(mFilterId, message)
            }
        }
    }
}