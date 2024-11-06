package com.jiangpengyong.eglbox_core.processor

object MessageType{
    const val SET_IMAGE = -10001    // 设置图片
    const val SET_BLANK = -10002    // 设置空白

    const val INPUT_PARAMS = -11001     // 后置处理参数
    const val INPUT_CALLBACK = -11002   // 后置处理回调
    const val RESULT_OUTPUT = -11003    // 后置处理出图

    const val REQUEST_RENDER = -12001   // 驱动渲染
    const val SURFACE_CREATED = -12005  // native window 生命周期，创建
    const val SURFACE_CHANGED = -12006  // native window 生命周期，变化
    const val SURFACE_DESTROY = -12007  // native window 生命周期，销毁

    const val SNAPSHOT_REQUEST = -13000 // 快照
}