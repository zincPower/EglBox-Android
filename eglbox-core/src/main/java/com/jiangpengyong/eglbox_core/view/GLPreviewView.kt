package com.jiangpengyong.eglbox_core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.FrameLayout
import com.jiangpengyong.eglbox_core.filter.Orientation
import com.jiangpengyong.eglbox_core.processor.GLProcessor.Companion.SOURCE_FILTER_ID
import com.jiangpengyong.eglbox_core.processor.image.ImageError
import com.jiangpengyong.eglbox_core.processor.image.ImageParams
import com.jiangpengyong.eglbox_core.processor.image.ImageProcessor
import com.jiangpengyong.eglbox_core.processor.image.ProcessFinishCallback
import com.jiangpengyong.eglbox_core.processor.listener.SurfaceViewManager
import com.jiangpengyong.eglbox_core.processor.preview.PreviewProcessor
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.space3d.Space3DMessageType
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author: jiang peng yong
 * @date: 2024/8/15 12:55
 * @email: 56002982@qq.com
 * @desc: GL 预览控件
 */
class GLPreviewView : FrameLayout {
    private val mSurfaceId = "GLPreviewView-${System.currentTimeMillis()}"
    private val mFilterIdCreator = AtomicInteger()
    private val mPreviewProcessor = PreviewProcessor()
    private val mImageProcessor = ImageProcessor()

    private var mAngleX = 0F
    private var mAngleY = 0F
    private var mBeforeY = 0F
    private var mBeforeX = 0F

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    private fun init(context: Context) {
        mPreviewProcessor.apply {
            launch()
            setPreviewSurfaceId(mSurfaceId)
        }
        mImageProcessor.launch()
        val textureView = TextureView(context)
        textureView.surfaceTextureListener = SurfaceTextureListenerImpl(mSurfaceId)
        addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun release() {
        mPreviewProcessor.destroy()
        mImageProcessor.destroy()
    }

    fun requestRender() {
        mPreviewProcessor.requestRender()
    }

    fun setImage(bitmap: Bitmap, isAutoRelease: Boolean) {
        mPreviewProcessor.setImage(bitmap, isAutoRelease)
        requestRender()
    }

    fun setBlank() {
        mPreviewProcessor.setBlank()
        requestRender()
    }

    fun addFilter(filterType: PreviewProcessor.FilterType, name: String, order: Int): String? {
        val filter = FilterCenter.createFilter(name)
        if (filter == null) {
            Log.e(TAG, "Filter create failure.")
            return null
        }
        val filterId = mFilterIdCreator.incrementAndGet().toString()
        mPreviewProcessor.addFilter(filterType, filterId, name, order, filter)
        requestRender()
        return filterId
    }

    fun removeFilter(filterId: String) {
        mPreviewProcessor.removeFilter(filterId)
        requestRender()
    }

    fun sendMessageToFilter(filterId: String, message: Message) {
        mPreviewProcessor.sendMessageToFilter(filterId, message)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val y = event.y
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - mBeforeX
                mAngleX += dx * TOUCH_SCALE_FACTOR
                val dy = y - mBeforeY
                mAngleY += dy * TOUCH_SCALE_FACTOR
                Space3DMessageType.obtainUpdateRotationMessage(
                    mAngleX, mAngleY, 0F
                ).apply {
                    sendMessageToFilter(SOURCE_FILTER_ID, this)
                }
                requestRender()
            }
        }
        mBeforeX = x
        mBeforeY = y
        return true
    }

    fun resetRotation() {
        mBeforeX = 0F
        mBeforeY = 0F
        mAngleX = 0F
        mAngleY = 0F
        Space3DMessageType.obtainResetRotationMessage().apply {
            sendMessageToFilter(SOURCE_FILTER_ID, this)
        }
        requestRender()
    }

    fun setViewpoint(x: Float, y: Float, z: Float) {
        Space3DMessageType.obtainUpdateViewpointMessage(x, y, z).apply {
            sendMessageToFilter(SOURCE_FILTER_ID, this)
        }
        requestRender()
    }

    fun setCenterPoint(x: Float, y: Float, z: Float) {
        Space3DMessageType.obtainUpdateCenterPointMessage(x, y, z).apply {
            sendMessageToFilter(SOURCE_FILTER_ID, this)
        }
        requestRender()
    }

    fun setUpVector(x: Float, y: Float, z: Float) {
        Space3DMessageType.obtainUpdateUpVectorMessage(x, y, z).apply {
            sendMessageToFilter(SOURCE_FILTER_ID, this)
        }
        requestRender()
    }

    fun setProjection(type: ProjectionType, near: Float, far: Float, ratio: Float) {
        Space3DMessageType.obtainUpdateProjectionMessage(type, near, far, ratio).apply {
            sendMessageToFilter(SOURCE_FILTER_ID, this)
        }
    }

    fun setLightPoint(x: Float, y: Float, z: Float) {
        Space3DMessageType.obtainUpdateLightPointMessage(x, y, z).apply {
            sendMessageToFilter(SOURCE_FILTER_ID, this)
        }
        requestRender()
    }

    fun exportImage(bitmap: Bitmap, data: HashMap<String, Any>, callback: (result: Bitmap?) -> Unit) {
        mPreviewProcessor.getFilterData(PreviewProcessor.FilterType.Process) { filterData ->
            if (filterData == null) {
                callback(null)
                return@getFilterData
            }
            Log.i(TAG, "ExportImage filter data=${filterData}")
            mImageProcessor.process(
                ImageParams(
                    bitmap = bitmap,
                    targetSize = Size(bitmap.width, bitmap.height),
                    normalOrientation = Orientation.Orientation_0,
                    deviceOrientation = Orientation.Orientation_0,
                    normalMirror = false,
                    focusMirror = false,
                    data = data,
                    filterData = filterData
                ), object : ProcessFinishCallback {
                    override fun onSuccess(pixelBuffer: ByteBuffer, size: Size, data: Map<String, Any>) {
                        Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
                            .apply {
                                copyPixelsFromBuffer(pixelBuffer)
                                callback(this)
                            }
                    }

                    override fun onFailure(error: ImageError) {
                        Log.e(TAG, "Process image failure. error=${error}")
                        callback(null)
                    }
                })
        }
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
        private const val TOUCH_SCALE_FACTOR = 1 / 4F
    }
}