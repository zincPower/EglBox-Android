package com.jiangpengyong.sample.f_geometry.geometry.shape

import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.program.DrawMode
import com.jiangpengyong.eglbox_filter.utils.VectorUtil
import com.jiangpengyong.sample.f_geometry.geometry.GeometryInfo
import com.jiangpengyong.sample.utils.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/8/31 14:31
 * @email 56002982@qq.com
 * @des 圆锥体侧面
 */
class ConeSide(
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
            drawMode = DrawMode.TriangleFan,
            frontFace = FrontFace.CW,
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
            val nextRadian = currentAngle.toRadians()
            val nextX = (radius * cos(nextRadian)).toFloat()
            val nextY = (radius * sin(nextRadian)).toFloat()

            vertexList.add(0F)
            vertexList.add(0F)
            vertexList.add(-height / 2F)

            vertexList.add(curX)
            vertexList.add(curY)
            vertexList.add(height / 2F)

            vertexList.add(nextX)
            vertexList.add(nextY)
            vertexList.add(height / 2F)
        }

        vertexList.add(radius * cos(0F))
        vertexList.add(radius * sin(0F))
        vertexList.add(height / 2F)

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

            textureList.add(0.5F)
            textureList.add(1F)

            textureList.add(curPercent)
            textureList.add(0F)

            textureList.add(nextPercent)
            textureList.add(0F)
        }

        textureList.add(0F)
        textureList.add(0F)

        return textureList.toFloatArray()
    }

    private fun calculateNormal(spanAngle: Float): FloatArray {
        val normalList = ArrayList<Float>()
        var currentAngle = 0F

        normalList.add(0F)
        normalList.add(0F)
        normalList.add(1F)

        // 水平角度从 0 到 360，逆时针进行
        while (currentAngle < 360F) {
            val curRadian = currentAngle.toRadians()
            val currentX = (radius * cos(curRadian)).toFloat()
            val currentY = (radius * sin(curRadian)).toFloat()
            val currentZ = height / 2
            currentAngle += spanAngle
            val nextRadian = currentAngle.toRadians()
            val nextX = (radius * cos(nextRadian)).toFloat()
            val nextY = (radius * sin(nextRadian)).toFloat()
            val nextZ = height / 2
            val normalVector = VectorUtil.cross(
                floatArrayOf(nextX, nextY, nextZ),
                floatArrayOf(currentX, currentY, currentZ)
            )
            normalList.add(normalVector[0])
            normalList.add(normalVector[1])
            normalList.add(normalVector[2])

            normalList.add(normalVector[0])
            normalList.add(normalVector[1])
            normalList.add(normalVector[2])

            normalList.add(normalVector[0])
            normalList.add(normalVector[1])
            normalList.add(normalVector[2])
        }

        return normalList.toFloatArray()
    }
}