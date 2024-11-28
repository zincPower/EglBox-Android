package com.jiangpengyong.eglbox_filter.model

import android.opengl.GLES20

/**
 * @author jiang peng yong
 * @date 2024/11/27 08:15
 * @email 56002982@qq.com
 * @des 模型数据构造
 */
object ModelCreator {
    /**
     * 创建球体
     * @param angleSpan 角度跨度，单位：度
     * @param radius 半径
     */
    fun createBall(
        angleSpan: Int = 10,
        radius: Float = 1F
    ): ModelData {
        return Ball(angleSpan, radius).create()
    }

    /**
     * 创建环
     * @param majorRadius 主半径，圆环中心到圆环切面中心距离
     * @param minorRadius 子半径，圆环切面半径
     * @param majorSegment 裁剪主圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
     * @param minorSegment 裁剪子圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
     */
    fun createRing(
        majorRadius: Float,
        minorRadius: Float,
        majorSegment: Int,
        minorSegment: Int,
    ): ModelData {
        return Ring(majorRadius, minorRadius, majorSegment, minorSegment).create()
    }

    /**
     * 创建圆圈（只有线，空心），需要通过 [GLES20.glLineWidth] 设置线宽度，单位：像素
     * @param angleSpan 角度跨度，单位：度
     * @param radius 半径
     */
    fun createCircle(
        angleSpan: Float = 1F,
        radius: Float = 1F,
    ): ModelData {
        return Circle(angleSpan, radius).create()
    }
}