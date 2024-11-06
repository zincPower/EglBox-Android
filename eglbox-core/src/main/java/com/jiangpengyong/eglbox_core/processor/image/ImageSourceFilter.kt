package com.jiangpengyong.eglbox_core.processor.image

import android.os.Bundle
import android.os.Message
import android.util.Log
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.filter.Orientation
import com.jiangpengyong.eglbox_core.filter.SourceFilter
import com.jiangpengyong.eglbox_core.processor.MessageType
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import roundToEven

class ImageSourceFilter : SourceFilter() {
    private val mMatrix = ModelMatrix()
    private var mImageParams: ImageParams? = null

    override fun onInit() {}

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val imageParams = mImageParams ?: return
        val targetSize = imageParams.targetSize
        val normalOrientation = imageParams.normalOrientation

        val adjustSize = when (normalOrientation) {
            Orientation.Orientation_0, Orientation.Orientation_180 -> {
                Size(imageParams.bitmap.width, imageParams.bitmap.height)
            }

            Orientation.Orientation_90, Orientation.Orientation_270 -> {
                Size(imageParams.bitmap.height, imageParams.bitmap.width)
            }
        }

        mMatrix.reset()
        when (imageParams.deviceOrientation) {
            Orientation.Orientation_0, Orientation.Orientation_180 -> {
                mMatrix.scale(if (imageParams.focusMirror) -1F else 1F, 1F, 1F)
            }

            Orientation.Orientation_90, Orientation.Orientation_270 -> {
                mMatrix.scale(1F, if (imageParams.focusMirror) -1F else 1F, 1F)
            }
        }

        val (scaleX, scaleY) = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, adjustSize, targetSize)
        mMatrix.scale(scaleX, scaleY, 1F)
        mMatrix.rotate(normalOrientation.value.toFloat(), 0F, 0F, 1F)
        mMatrix.scale(if (imageParams.normalMirror) -1F else 1F, 1F, 1F)
        mMatrix.scale(1F, -1F, 1F)

        val texture2DProgram = context.texture2DProgram
        texture2DProgram.reset()
        texture2DProgram.setVertexMatrix(mMatrix.matrix)
        imageInOut.texture?.let { texture2DProgram.setTexture(it) }

        mImageParams?.bitmap?.recycle()
        mImageParams = null

        context.getTexFBO(
            roundToEven(targetSize.width.toFloat()),
            roundToEven(targetSize.height.toFloat()),
        ).let { fbo ->
            fbo.use { texture2DProgram.draw() }
            imageInOut.out(fbo)
        }
    }

    override fun onRelease() {}
    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}

    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            MessageType.INPUT_PARAMS -> {
                handleImageParams(message.obj as? ImageParams)
            }
        }
    }

    private fun handleImageParams(imageParams: ImageParams?) {
        if (imageParams == null) {
            Log.e(TAG, "handleImageParams imageParams is null.")
            return
        }
        mImageParams = imageParams
        Log.i(TAG, "handleImageParam filterId=${id} imageParams=${imageParams}")
        mContext?.getColorTexture(imageParams.bitmap.width, imageParams.bitmap.height)
            ?.let { texture ->
                // TODO 可以使用 update
                texture.setData(imageParams.bitmap)
                updateTexture(texture)
            }
    }

    companion object {
        private const val TAG = "ImageSourceFilter"
    }
}