package com.jiangpengyong.sample.e_texture.planet

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2024/8/12 21:57
 * @email 56002982@qq.com
 * @des 太阳系
 */
class SolarSystemActivity : AppCompatActivity() {
    private lateinit var mGLPreviewView: GLPreviewView
    private var mFilterId: String? = null

    private var mEyeTarget = SolarSystemFilter.Target.SolarSystem
    private val mTranAnim = ValueAnimator.ofFloat(0F, 1F).apply {
        setDuration(2_000)
        interpolator = DecelerateInterpolator()
        addUpdateListener { animator ->
            val filterId = mFilterId ?: return@addUpdateListener
            val value = animator.animatedValue as? Float ?: return@addUpdateListener
            mGLPreviewView.sendMessageToFilter(filterId, Message.obtain().apply {
                what = SolarSystemMessageType.CHANGE_TARGET
                arg1 = mEyeTarget.value
                obj = value
            })
        }
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private val mRenderTrigger = object : Runnable {
        override fun run() {
            val filterId = mFilterId ?: return
            mGLPreviewView.sendMessageToFilter(filterId, Message.obtain().apply {
                what = SolarSystemMessageType.UPDATE_ORBIT_AND_ROTATION
            })
            mGLPreviewView.requestRender()
            mHandler.postDelayed(this, RENDER_INTERVAL)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solar_system)

        FilterCenter.registerFilter(SolarSystemFilter.TAG, SolarSystemFilter::class.java)

        mGLPreviewView = findViewById(R.id.gl_preview_view)
        mGLPreviewView.post {
            mGLPreviewView.setBlank()
            mFilterId = mGLPreviewView.addFilter(SolarSystemFilter.TAG)
            mGLPreviewView.setRotation(60F, 45F, 0F)
            mGLPreviewView.setProjection(ProjectionType.Perspective, 10F, 1000F, 1F)
            mHandler.postDelayed(mRenderTrigger, RENDER_INTERVAL)
        }

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
        if (mTranAnim.isRunning) mTranAnim.cancel()
        mEyeTarget = target
        mTranAnim.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val RENDER_INTERVAL = 10L
    }
}