package com.jiangpengyong.eglbox_filter.model.model

import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.model.Model
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.model.NormalVectorType
import com.jiangpengyong.eglbox_filter.utils.toRadians
import kotlin.math.cos
import kotlin.math.sin

class Ball(
    private val angleSpan: Int = 10,
    private val radius: Float = 1F,
) : Model() {
    private val vertexList = ArrayList<Float>()
    private var vertexCount = 0

    override fun onCreate(): ModelData {
        var verticalAngle = -90.0

        // 计算中间每一层的点
        while (verticalAngle < 90F) {   // 垂直角度从 -90 到 90
            // 这一层的半径
            val curLayerAngle = verticalAngle.toRadians()
            val layerRadius = radius * cos(curLayerAngle)

            // 下一层的半径
            val nextLayerAngle = (verticalAngle + angleSpan).toRadians()
            val nextLayerRadius = radius * cos(nextLayerAngle)

            val curLayerY = radius * sin(curLayerAngle)
            val nextLayerY = radius * sin(nextLayerAngle)

            var horizontalAngle = 0.0
            while (horizontalAngle < 360) {     // 水平角度从 0 到 360
                val curHorAngle = horizontalAngle.toRadians()
                val nextHorAngle = (horizontalAngle + angleSpan).toRadians()

                /**
                 *     P2(x2, y2, z2)   P3(x3, y3, z3)
                 *      ------------------
                 *      ｜              ╱｜
                 *      ｜            ╱  ｜
                 *      ｜          ╱    ｜
                 *      ｜        ╱      ｜
                 *      ｜      ╱        ｜
                 *      ｜    ╱          ｜
                 *      ｜  ╱            ｜
                 *      ｜╱              ｜
                 *      ------------------
                 *     P1(x1, y1, z1)   P0(x0, y0, z0)
                 */
                val x0 = layerRadius * cos(curHorAngle)
                val y0 = curLayerY
                val z0 = layerRadius * sin(curHorAngle)

                val x1 = layerRadius * cos(nextHorAngle)
                val y1 = curLayerY
                val z1 = layerRadius * sin(nextHorAngle)

                val x2 = nextLayerRadius * cos(nextHorAngle)
                val y2 = nextLayerY
                val z2 = nextLayerRadius * sin(nextHorAngle)

                val x3 = nextLayerRadius * cos(curHorAngle)
                val y3 = nextLayerY
                val z3 = nextLayerRadius * sin(curHorAngle)

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                vertexList.add(x2.toFloat())
                vertexList.add(y2.toFloat())
                vertexList.add(z2.toFloat())

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x0.toFloat())
                vertexList.add(y0.toFloat())
                vertexList.add(z0.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                horizontalAngle += angleSpan
            }
            verticalAngle += angleSpan
        }
        vertexCount = vertexList.size / 3

        val textureList = ArrayList<Float>()
        val vCounts = 180 / angleSpan
        val vSpan = 1 / vCounts.toFloat()
        val hCounts = 360 / angleSpan
        val hSpan = 1 / hCounts.toFloat()
        for (v in 0 until vCounts) {
            for (h in 0 until hCounts) {
                /**
                 *     P2(x2, y2)   P3(x3, y3)
                 *      ------------------
                 *      ｜              ╱｜
                 *      ｜            ╱  ｜
                 *      ｜          ╱    ｜
                 *      ｜        ╱      ｜
                 *      ｜      ╱        ｜
                 *      ｜    ╱          ｜
                 *      ｜  ╱            ｜
                 *      ｜╱              ｜
                 *      ------------------
                 *     P1(x1, y1)   P0(x0, y0)
                 */
                val x0 = 1 - h * hSpan
                val y0 = 1 - v * vSpan

                val x1 = 1 - (h + 1) * hSpan
                val y1 = 1 - v * vSpan

                val x2 = 1 - (h + 1) * hSpan
                val y2 = 1 - (v + 1) * vSpan

                val x3 = 1 - h * hSpan
                val y3 = 1 - (v + 1) * vSpan

                textureList.add(x1)
                textureList.add(y1)

                textureList.add(x3)
                textureList.add(y3)

                textureList.add(x2)
                textureList.add(y2)

                textureList.add(x1)
                textureList.add(y1)

                textureList.add(x0)
                textureList.add(y0)

                textureList.add(x3)
                textureList.add(y3)
            }
        }

        return ModelData(
            vertexData = vertexList.toFloatArray(),
            textureData = textureList.toFloatArray(),
            textureStep = 2,
            // 因为球体的几何体征，球心在原点，所以各个点法向量和顶点位置刚好一致，不用再次计算
            normalData = vertexList.toFloatArray(),
            frontFace = FrontFace.CCW,
            normalVectorType = NormalVectorType.Vertex,
        )
    }
}