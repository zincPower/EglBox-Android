package com.jiangpengyong.eglbox_core.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.FrameLayout
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.processor.display.DisplayProcessor
import com.jiangpengyong.eglbox_core.processor.listener.SurfaceViewManager
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author: jiang peng yong
 * @date: 2024/8/15 12:55
 * @email: 56002982@qq.com
 * @desc: GL 显示控件
 */
class GLPreviewView : FrameLayout {
    private val mSurfaceId = "GLPreviewView-${System.currentTimeMillis()}"
    private val mDisplayProcessor = DisplayProcessor()
    private var mFilterIdCreator = AtomicInteger()

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        mDisplayProcessor.apply {
            launch()
            setPreviewSurfaceId(mSurfaceId)
        }
        val textureView = TextureView(context)
        textureView.surfaceTextureListener = SurfaceTextureListenerImpl(mSurfaceId)
        addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun release() {
        mDisplayProcessor.destroy()
    }

    fun requestRender() {
        mDisplayProcessor.requestRender()
    }

    fun setImage(bitmap: Bitmap, isAutoRelease: Boolean) {
        mDisplayProcessor.setImage(bitmap, isAutoRelease)
    }

    fun setBlank() {
        mDisplayProcessor.setBlank()
    }

    fun addFilter(filterType: DisplayProcessor.FilterType, name: String, order: Int): String? {
        val filter = FilterCenter.createFilter(name)
        if (filter == null) {
            Log.e(TAG, "Filter create failure.")
            return null
        }
        val filterId = mFilterIdCreator.incrementAndGet().toString()
        mDisplayProcessor.addFilter(filterType, filterId, name, order, filter)
        return filterId
    }

    private class SurfaceTextureListenerImpl(private val surfaceId: String) : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            SurfaceViewManager.surfaceCreated(surfaceId, surface, width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            SurfaceViewManager.surfaceSizeChanged(surfaceId, surface, width, height)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            SurfaceViewManager.surfaceDestroy(surfaceId, surface)
            // TODO 这里需要改下
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }

    }

    companion object {
        const val TAG = "GLPreviewView"
    }
}