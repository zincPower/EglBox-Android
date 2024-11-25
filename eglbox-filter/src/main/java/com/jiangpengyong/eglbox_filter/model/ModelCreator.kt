package com.jiangpengyong.eglbox_filter.model

object ModelCreator {
    fun createBall(
        angleSpan: Int = 10,
        radius: Float = 1F
    ): ModelData {
        return Ball(angleSpan, radius).create()
    }

    fun createRing(
        majorRadius: Float,     // 主半径，圆环中心到圆环切面中心距离
        minorRadius: Float,     // 子半径，圆环切面半径
        majorSegment: Int,      // 裁剪主圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
        minorSegment: Int,      // 裁剪子圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
    ): ModelData {
        return Ring(majorRadius, minorRadius,majorSegment, minorSegment).create()
    }
}