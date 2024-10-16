package com.jiangpengyong.eglbox_core.gles

import java.nio.ByteBuffer

data class ImageInfo(
    val width: Int,
    val height: Int,
    val pixels: ByteBuffer
)