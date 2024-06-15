package com.jiangpengyong.eglbox.gles

import java.nio.IntBuffer

data class ImageInfo(
    val width: Int,
    val height: Int,
    val pixels: IntBuffer
)