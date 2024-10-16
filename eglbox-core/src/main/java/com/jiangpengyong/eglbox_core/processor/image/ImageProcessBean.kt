package com.jiangpengyong.eglbox_core.processor.image

import android.graphics.Bitmap
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterData
import com.jiangpengyong.eglbox_core.filter.Orientation
import java.nio.ByteBuffer

data class ImageProcessTask(
    val id: Int,
    val params: ImageParams,
    val callback: ProcessFinishCallback,
)

interface ProcessFinishCallback {
    fun onSuccess(pixelBuffer: ByteBuffer, size: Size, data: Map<String, Any>)
    fun onFailure(error: ImageError)
}

data class ImageParams(
    val bitmap: Bitmap,
    val targetSize: Size,
    val normalOrientation: Orientation,
    val deviceOrientation: Orientation,
    val normalMirror: Boolean,
    val focusMirror: Boolean,
    val data: HashMap<String, Any>,
    val filterData: FilterData,
)

data class ImageResult(
    val isSuccess: Boolean,
    val processId: Int,
    val pixelBuffer: ByteBuffer? = null,
    val imageSize: Size? = null,
    val data: HashMap<String, Any>? = HashMap(),
    val callback: ProcessFinishCallback?,
    val error: ImageError? = null
)