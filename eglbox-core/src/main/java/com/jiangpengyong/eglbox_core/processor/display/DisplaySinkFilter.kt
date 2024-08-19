package com.jiangpengyong.eglbox_core.processor.display

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.Surface
import com.jiangpengyong.eglbox_core.egl.WindowSurface
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.filter.SinkFilter
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.MessageType
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_core.program.isValid
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

/**
 * @author: jiang peng yong
 * @date: 2024/8/6 13:01
 * @email: 56002982@qq.com
 * @desc: 上屏上屏节点
 */
class DisplaySinkFilter : SinkFilter() {
    private var mMatrix = ModelMatrix()
    private var mPreviewSize = Size(0, 0)
    private var mBeforeImageSize = Size(0, 0)
    private var mIsNeedCalculate = true
    private var mSurface: Any? = null
    private var mWindowSurface: WindowSurface? = null

    override fun onInit() {}

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val surface = mWindowSurface
        if (surface == null) {
            Logger.e(TAG, "Window surface is null.")
            return
        }
        if (!mPreviewSize.isValid()) {
            Logger.e(TAG, "Preview size is invalid. Preview size=${mPreviewSize}")
            return
        }
        if (!imageInOut.isValid()) {
            Log.e(TAG, "Image is invalid.")
            return
        }
        context.egl?.makeCurrent(surface)
        GLES20.glViewport(0, 0, mPreviewSize.width, mPreviewSize.height)
        GLES20.glClearColor(0F, 0F, 0F, 1F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val texture = imageInOut.texture!!
        val width = texture.width
        val height = texture.height
        if (mBeforeImageSize.width != width || mBeforeImageSize.height != height) {
            mBeforeImageSize = Size(width, height)
            mIsNeedCalculate = true
        }

        if (mIsNeedCalculate) {
            mMatrix = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, mPreviewSize, mBeforeImageSize)
            mIsNeedCalculate = false
        }

        context.texture2DProgram.apply {
            setTexture(texture)
            setScaleType(ScaleType.MATRIX)
            setVertexMatrix(mMatrix.matrix)
            draw()
        }

        surface.swapBuffer()
        context.surface?.let { context.egl?.makeCurrent(it) }
    }

    override fun onRelease() {
        mWindowSurface?.release()
        mWindowSurface = null
        mPreviewSize = Size(0, 0)
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            MessageType.SURFACE_CREATED -> {
                val width = message.arg1
                val height = message.arg2
                Logger.i(TAG, "Create preview window. size=${width}x${height}")
                createPreviewWindow(message.obj, width, height)
            }

            MessageType.SURFACE_CHANGED -> {
                val width = message.arg1
                val height = message.arg2
                Logger.i(TAG, "Update preview window. size=${width}x${height}")
                updatePreviewWindow(message.obj, width, height)
            }

            MessageType.SURFACE_DESTROY -> {
                val curSurface = message.obj
                val isEqual = if (mSurface == curSurface) "true" else "false"
                Logger.i(TAG, "Destroy preview window. mWindow==window is $isEqual")
                destroyPreviewWindow(curSurface)
            }
        }
    }

    private fun createPreviewWindow(surface: Any?, width: Int, height: Int) {
        if (surface == null) {
            Logger.e(TAG, "Surface is null.")
            return
        }
        val context = mContext
        if (context == null) {
            Logger.e(TAG, "Context is nullptr.[createPreviewWindow]")
            return
        }
        mIsNeedCalculate = true
        // 如果已经持有 WindowSurface ，并且之前 window 和当前传入的 window 相同，则说明只需要进行更改高度
        if (mSurface != null && mWindowSurface != null && mSurface == surface) {
            mPreviewSize = Size(width, height)
            return
        }
        mWindowSurface?.release()
        mWindowSurface = null
        mSurface = null
        mPreviewSize = Size(0, 0)

        mWindowSurface = context.egl?.createWindow(surface, width, height)
        mSurface = surface
        mPreviewSize = Size(width, height)
    }

    private fun updatePreviewWindow(surface: Any, width: Int, height: Int) {
        createPreviewWindow(surface, width, height)
    }

    private fun destroyPreviewWindow(surface: Any) {
        if (surface != mSurface) return
        mWindowSurface?.release()
        mWindowSurface = null
        mSurface = null
        mPreviewSize = Size(0, 0)
    }

    companion object {
        private const val TAG = "DisplaySinkFilter"
    }
}