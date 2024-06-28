package com.jiangpengyong.eglbox.egl


/**
 * @author: jiang peng yong
 * @date: 2023/9/25 23:04
 * @email: 56002982@qq.com
 * @desc:
 */
class GLEngine {

}

data class Parameters(
    val surfaceType: SurfaceType,
    val width: Int = -1,
    val height: Int = -1,
)

//enum class SurfaceType { PBuffer, Window }