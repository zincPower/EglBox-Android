package com.jiangpengyong.eglbox_filter.model

object ModelCreator {
    fun createBall(
        angleSpan: Int = 10,
        radius: Float = 1F,
        sideRenderingType: SideRenderingType = SideRenderingType.Single,
    ): ModelData {
        return Ball(angleSpan, radius, sideRenderingType).create()
    }
}