package com.jiangpengyong.eglbox_core.gles

import java.nio.IntBuffer

data class ImageInfo(
    val width: Int,
    val height: Int,
    val pixels: IntBuffer
)