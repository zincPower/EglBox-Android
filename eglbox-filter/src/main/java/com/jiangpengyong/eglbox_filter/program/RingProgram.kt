package com.jiangpengyong.eglbox_filter.program

import com.jiangpengyong.eglbox_filter.model.ModelCreator

/**
 * @author jiang peng yong
 * @date 2024/8/12 22:04
 * @email 56002982@qq.com
 * @des 环形
 */
class RingProgram(
    majorRadius: Float = 1.5F,     // 主半径，圆环中心到圆环切面中心距离
    minorRadius: Float = 0.5F,     // 子半径，圆环切面半径
    majorSegment: Int = 36,        // 裁剪主圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
    minorSegment: Int = 36,
    lightCalculateType: LightCalculateType = LightCalculateType.Vertex,
) : LightProgram(
    modelData = ModelCreator.createRing(majorRadius, minorRadius, majorSegment, minorSegment),
    lightCalculateType = lightCalculateType,
)
