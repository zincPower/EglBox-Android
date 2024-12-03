package com.jiangpengyong.eglbox_core.gles

import android.opengl.GLES20

fun blend(
    srcFactor: Int,
    dstFactor: Int,
    block: () -> Unit,
) {
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFunc(srcFactor, dstFactor)
    block()
    GLES20.glDisable(GLES20.GL_BLEND)
}

fun blend(
    srcRGB: Int,
    dstRGB: Int,
    srcAlpha: Int,
    dstAlpha: Int,
    block: () -> Unit,
) {
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)
    block()
    GLES20.glDisable(GLES20.GL_BLEND)
}