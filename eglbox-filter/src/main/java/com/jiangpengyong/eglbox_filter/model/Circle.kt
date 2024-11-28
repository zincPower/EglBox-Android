package com.jiangpengyong.eglbox_filter.model

import com.jiangpengyong.eglbox_filter.utils.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/11/27 08:39
 * @email 56002982@qq.com
 * @des 圆圈，空心
 */
class Circle(
    val angleSpan: Float = 1F,
    val radius: Float = 1F
) : Model() {
    override fun onCreate(): ModelData {
        val vertexList = ArrayList<Float>()
        var angle = 0F
        while (angle < 360F) {     // 水平角度从 0 到 360
            val curAngle = angle.toRadians()

            val x = radius * cos(curAngle)
            val y = radius * sin(curAngle)
            val z = 0F

            vertexList.add(x.toFloat())
            vertexList.add(y.toFloat())
            vertexList.add(z)

            angle += angleSpan
        }
        return ModelData(
            vertexData = vertexList.toFloatArray(),
            textureData = null,
            textureStep = 0,
            normalData = null,
            frontFace = FrontFace.CCW,
            normalVectorType = NormalVectorType.Vertex,
        )
    }
}