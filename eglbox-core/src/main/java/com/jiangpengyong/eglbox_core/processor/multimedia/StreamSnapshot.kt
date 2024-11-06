package com.jiangpengyong.eglbox_core.processor.multimedia

import android.os.Message
import android.util.Log
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.Orientation
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.processor.MessageType
import com.jiangpengyong.eglbox_core.processor.image.ImageError
import com.jiangpengyong.eglbox_core.processor.image.ProcessFinishCallback
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

data class SnapshotParams(
    val size: Size,
    val mirror: Boolean,
    val callback: ProcessFinishCallback,
)

class StreamSnapshot {
    private var mIsNeedSnapshot: Boolean = false
    private var mSnapshotParams: SnapshotParams? = null
    private val mMatrix: ModelMatrix = ModelMatrix()
    private var mFilterId: String? = null

    fun setFilterId(filterId: String) {
        mFilterId = filterId
    }

    fun maybeSnapshot(context: FilterContext, texture: GLTexture) {
        if (!mIsNeedSnapshot) return

        val filterId = mFilterId
        if (filterId == null) {
            Log.e(TAG, "Filter id is null.")
            return
        }

        val snapshotParams = mSnapshotParams
        if (snapshotParams == null) {
            Log.e(TAG, "Snapshot params is null.")
            return
        }

        mIsNeedSnapshot = false
        val orgWidth: Int
        val orgHeight: Int
        val paramsWidth: Int
        val paramsHeight: Int
        when (context.deviceOrientation) {
            Orientation.Orientation_0, Orientation.Orientation_180 -> {
                orgWidth = texture.width
                orgHeight = texture.height
                paramsWidth = snapshotParams.size.width
                paramsHeight = snapshotParams.size.height
            }

            Orientation.Orientation_90, Orientation.Orientation_270 -> {
                orgWidth = texture.height
                orgHeight = texture.width
                paramsWidth = snapshotParams.size.height
                paramsHeight = snapshotParams.size.width
            }
        }
        val (scaleX, scaleY) = VertexAlgorithmFactory.calculate(
            ScaleType.CENTER_CROP,
            Size(orgWidth, orgHeight),
            Size(paramsWidth, paramsHeight)
        )

        mMatrix.reset()
        mMatrix.scale(if (snapshotParams.mirror) 1F else -1F, -1F, 1F)
        mMatrix.scale(scaleX, scaleY, 1F)
        mMatrix.rotate(context.deviceOrientation.value.toFloat(), 0F, 0F, 1F)

        val texture2DProgram = context.texture2DProgram
        texture2DProgram.reset()
        texture2DProgram.setTexture(texture)
        texture2DProgram.setVertexMatrix(mMatrix.matrix)

        val fbo = context.getTexFBO(paramsWidth, paramsHeight)
        fbo.use { texture2DProgram.draw() }
        val imageInfo = fbo.readPixels()
        context.recycle(fbo)

        if (imageInfo == null) {
            snapshotParams.callback.onFailure(ImageError.SnapshotFailure)
        } else {
            snapshotParams.callback.onSuccess(
                pixelBuffer = imageInfo.pixels,
                size = Size(imageInfo.width, imageInfo.height),
                data = HashMap(context.renderData)
            )
        }
    }

    fun handleMessage(message: Message) {
        if (message.what == MessageType.SNAPSHOT_REQUEST) {
            mIsNeedSnapshot = true
            mSnapshotParams = message.obj as? SnapshotParams
        }
    }

    companion object {
        private const val TAG = "StreamSnapshot"
    }
}