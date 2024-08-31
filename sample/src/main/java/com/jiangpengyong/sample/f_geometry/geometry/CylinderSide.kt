package com.jiangpengyong.sample.f_geometry.geometry

import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.utils.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/8/31 11:10
 * @email 56002982@qq.com
 * @des 圆柱体侧面
 */
class CylinderSide(
    val radius: Float,      // 半径
    val height: Float,      // 高度
    val segment: Int,       // 裁剪份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
) {
    private var mGeometryInfo: GeometryInfo? = null

    fun create(): GeometryInfo {
        val geometryInfo = mGeometryInfo
        if (geometryInfo != null) {
            return geometryInfo
        }

        val spanAngle = 360F / segment

        val vertex = calculateVertex(spanAngle)
        val count = vertex.size / 3
        val texture = calculateTexture(spanAngle)
        val normal = calculateNormal(spanAngle)

        return GeometryInfo(
            vertexBuffer = allocateFloatBuffer(vertex),
            textureBuffer = allocateFloatBuffer(texture),
            normalBuffer = allocateFloatBuffer(normal),
            vertexCount = count,
            drawMode = DrawMode.Triangles,
            frontFace = FrontFace.CCW,
        ).apply { mGeometryInfo = this }
    }

    private fun calculateVertex(spanAngle: Float): FloatArray {
        val vertexList = ArrayList<Float>()
        var currentAngle = 0F

        // 水平角度从 0 到 360，逆时针进行
        while (currentAngle < 360F) {
            val curRadian = currentAngle.toRadians()
            val curX = (radius * cos(curRadian)).toFloat()
            val curY = (radius * sin(curRadian)).toFloat()
            currentAngle += spanAngle
            val nextRadian = (currentAngle).toRadians()
            val nextX = (radius * cos(nextRadian)).toFloat()
            val nextY = (radius * sin(nextRadian)).toFloat()

            val nearZ = height / 2
            val farZ = -height / 2

            // 第一个三角形
            vertexList.add(curX)
            vertexList.add(curY)
            vertexList.add(nearZ)

            vertexList.add(curX)
            vertexList.add(curY)
            vertexList.add(farZ)

            vertexList.add(nextX)
            vertexList.add(nextY)
            vertexList.add(farZ)

            // 第二个三角形
            vertexList.add(nextX)
            vertexList.add(nextY)
            vertexList.add(farZ)

            vertexList.add(nextX)
            vertexList.add(nextY)
            vertexList.add(nearZ)

            vertexList.add(curX)
            vertexList.add(curY)
            vertexList.add(nearZ)
        }

        return vertexList.toFloatArray()
    }

    private fun calculateTexture(spanAngle: Float): FloatArray {
        val textureList = ArrayList<Float>()
        var currentAngle = 0F

        // 水平角度从 0 到 360，逆时针进行
        while (currentAngle < 360F) {
            val curPercent = currentAngle / 360F
            currentAngle += spanAngle
            val nextPercent = currentAngle / 360F

            // 第一个三角形
            textureList.add(curPercent)
            textureList.add(0F)

            textureList.add(curPercent)
            textureList.add(1F)

            textureList.add(nextPercent)
            textureList.add(1F)

            // 第二个三角形
            textureList.add(nextPercent)
            textureList.add(1F)

            textureList.add(nextPercent)
            textureList.add(0F)

            textureList.add(curPercent)
            textureList.add(0F)
        }

        return textureList.toFloatArray()
    }

    private fun calculateNormal(spanAngle: Float): FloatArray {
        val normalList = ArrayList<Float>()
        var currentAngle = 0F

        // 水平角度从 0 到 360，逆时针进行
        while (currentAngle < 360F) {
            val curRadian = currentAngle.toRadians()
            val curX = (radius * cos(curRadian)).toFloat()
            val curY = (radius * sin(curRadian)).toFloat()
            currentAngle += spanAngle
            val nextRadian = (currentAngle).toRadians()
            val nextX = (radius * cos(nextRadian)).toFloat()
            val nextY = (radius * sin(nextRadian)).toFloat()

            // 第一个三角形
            normalList.add(curX)
            normalList.add(curY)
            normalList.add(0F)

            normalList.add(curX)
            normalList.add(curY)
            normalList.add(0F)

            normalList.add(nextX)
            normalList.add(nextY)
            normalList.add(0F)

            // 第二个三角形
            normalList.add(nextX)
            normalList.add(nextY)
            normalList.add(0F)

            normalList.add(nextX)
            normalList.add(nextY)
            normalList.add(0F)

            normalList.add(curX)
            normalList.add(curY)
            normalList.add(0F)
        }

        return normalList.toFloatArray()
    }
}