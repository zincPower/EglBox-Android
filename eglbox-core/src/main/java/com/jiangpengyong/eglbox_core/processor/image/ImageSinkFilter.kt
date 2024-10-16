package com.jiangpengyong.eglbox_core.processor.image

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.filter.Orientation
import com.jiangpengyong.eglbox_core.filter.SinkFilter
import com.jiangpengyong.eglbox_core.gles.EGLBox
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.GLProcessor
import com.jiangpengyong.eglbox_core.processor.MessageType
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

class ImageSinkFilter : SinkFilter() {
    private val mMatrix: ModelMatrix = ModelMatrix()
    private var mProcessId: Int = 0
    private var mProcessFinishCallback: ProcessFinishCallback? = null

    override fun onInit() {}

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return

        val width: Int
        val height: Int
        when (context.deviceOrientation) {
            Orientation.Orientation_0, Orientation.Orientation_180 -> {
                width = texture.width
                height = texture.height
            }

            Orientation.Orientation_90, Orientation.Orientation_270 -> {
                width = texture.height
                height = texture.width
            }
        }

        mMatrix.reset()
        mMatrix.rotate(-context.deviceOrientation.value.toFloat(), 0F, 0F, 1F)
        mMatrix.scale(1F, -1F, 1F)

        val texture2DProgram = context.texture2DProgram
        texture2DProgram.reset()
        texture2DProgram.setVertexMatrix(mMatrix.matrix)
        texture2DProgram.setTexture(texture)

        val resultFBO = context.getTexFBO(width, height)
        resultFBO.use { texture2DProgram.draw() }
        texture2DProgram.reset()
        val imageInfo = resultFBO.readPixels()
        imageInOut.out(resultFBO)

        val imageResult = if (imageInfo == null) {
            ImageResult(
                isSuccess = false,
                processId = mProcessId,
                pixelBuffer = null,
                imageSize = null,
                data = null,
                callback = mProcessFinishCallback,
                error = ImageError.ReadPixelsFailure,
            )
        } else {
            ImageResult(
                isSuccess = true,
                processId = mProcessId,
                pixelBuffer = imageInfo.pixels,
                imageSize = Size(width, height),
                data = HashMap(context.renderData),
                callback = mProcessFinishCallback,
                error = null,
            )
        }
        val message = Message.obtain().apply {
            what = MessageType.PROCESS_RESULT_OUTPUT
            obj = imageResult
        }
        context.sendMessage(GLProcessor.SINK_FILTER_ID, message)
    }

    override fun onRelease() {}
    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}

    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            MessageType.PROCESS_INPUT_CALLBACK -> {
                handleImageParams(message.arg1, message.obj as? ProcessFinishCallback)
            }
        }
    }

    private fun handleImageParams(processId: Int, processFinishCallback: ProcessFinishCallback?) {
        if (processFinishCallback == null) {
            Logger.e(TAG, "handleImageParams processFinishCallback is null")
            return
        }

        mProcessFinishCallback = processFinishCallback
        mProcessId = processId
    }

    companion object {
        private const val TAG = "ImageSinkFilter"
    }
}