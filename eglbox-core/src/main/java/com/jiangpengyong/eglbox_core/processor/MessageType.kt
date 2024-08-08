package com.jiangpengyong.eglbox_core.processor

object MessageType {
    // 驱动渲染
    const val REQUEST_RENDER = -10000

    // 上屏处理，设置图片
    const val DISPLAY_SET_IMAGE = -20001

    // 上屏处理，设置空白
    const val DISPLAY_SET_BLANK = -20002

    const val SURFACE_CREATED = -20003
    const val SURFACE_CHANGED = -20004
    const val SURFACE_DESTROY = -20005
}