package com.jiangpengyong.eglbox_core.processor

object MessageType {
    // 驱动渲染
    const val REQUEST_RENDER = -10000

    // 上屏处理，设置图片
    const val PREVIEW_SET_IMAGE = -10001

    // 上屏处理，设置空白
    const val PREVIEW_SET_BLANK = -10002

    // native window 生命周期
    const val SURFACE_CREATED = -10003
    const val SURFACE_CHANGED = -10004
    const val SURFACE_DESTROY = -10005

    // 后置处理参数
    const val PROCESS_INPUT_PARAMS = -10006

    // 后置处理回调
    const val PROCESS_INPUT_CALLBACK = -10007

    // 后置处理出图
    const val PROCESS_RESULT_OUTPUT = -10007

    // 触碰事件
    const val TOUCH_EVENT = -18000
}
