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
    companion object {
        const val MESSAGE_TARGET = 100002
    }

    private lateinit var mGLPreviewView: GLPreviewView
    private var mFilterId: String? = null

    private var mEyeTarget = SolarSystemFilter.Target.SolarSystem
    private val mFloatAnimation = ValueAnimator.ofFloat(0F, 1F).apply {
        setDuration(2000)
        interpolator = DecelerateInterpolator()
        addUpdateListener { animator ->
            val filterId = mFilterId ?: return@addUpdateListener
            val value = animator.animatedValue as? Float ?: return@addUpdateListener
            mGLPreviewView.sendMessageToFilter(filterId, Message.obtain().apply {
                what = MESSAGE_TARGET
                arg1 = mEyeTarget.value
                obj = value
            })
        }
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = object : Runnable {
        override fun run() {
            mGLPreviewView.requestRender()
            mHandler.postDelayed(this, 10)
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
            mHandler.postDelayed(mRunnable, 10)
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
        if (mFloatAnimation.isRunning) mFloatAnimation.cancel()
        mEyeTarget = target
        mFloatAnimation.start()
    }
}