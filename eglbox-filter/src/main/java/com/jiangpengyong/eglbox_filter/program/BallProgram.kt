package com.jiangpengyong.eglbox_filter.program

import com.jiangpengyong.eglbox_filter.model.ModelCreator

/**
 * @author jiang peng yong
 * @date 2024/6/19 10:08
 * @email 56002982@qq.com
 * @des 球体程序
 */
class BallProgram(
    angleSpan: Int = 10,
    radius: Float = 1F,
    lightCalculateType: LightCalculateType = LightCalculateType.Fragment
) : LightProgram(
    modelData = ModelCreator.createBall(angleSpan, radius),
    lightCalculateType = lightCalculateType,
) {
    private var mAngleSpan = angleSpan
    private var mRadius = radius

    fun setAngleSpan(value: Int) {
        mAngleSpan = value
        modelData = ModelCreator.createBall(mAngleSpan, mRadius)
    }
}