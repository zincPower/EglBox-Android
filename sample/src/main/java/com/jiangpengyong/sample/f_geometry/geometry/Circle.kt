package com.jiangpengyong.sample.f_geometry.geometry

import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.utils.toRadians
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class Circle(
    val radius: Float,      // 半径
    val segment: Int,       // 裁剪份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
) {
    private var mGeometryInfo: GeometryInfo? = null

    fun create(): GeometryInfo {
        val geometryInfo = mGeometryInfo
        if (geometryInfo != null) {
            return geometryInfo
        }

        val count = segment + 2
        val spanAngle = 360F / segment

        return GeometryInfo(
            vertexBuffer = calculateVertex(spanAngle, count),
            textureBuffer = calculateTexture(spanAngle, count),
            normalBuffer = calculateNormal(count),
            vertexCount = count,
            drawMode = DrawMode.TriangleFan,
            frontFace = FrontFace.CCW,
        ).apply { mGeometryInfo = this }
    }

    private fun calculateVertex(spanAngle: Float, count: Int): FloatBuffer {
        val vertexList = ArrayList<Float>(count * 3)
        var currentAngle = 0F

        // 存放原点
        vertexList.add(0F)
        vertexList.add(0F)
        vertexList.add(0F)

        // 水平角度从 0 到 360，逆时针进行
        while (currentAngle < 360F) {
            val curAngle = currentAngle.toRadians()

            val x = radius * cos(curAngle)
            val y = radius * sin(curAngle)
            val z = 0F

            vertexList.add(x.toFloat())
            vertexList.add(y.toFloat())
            vertexList.add(z)

            currentAngle += spanAngle
        }

        // 增加最后一个点，即圆边第一个点，才能让圆闭合
        vertexList.add(radius * cos(0F.toRadians()).toFloat())
        vertexList.add(radius * sin(0F.toRadians()).toFloat())
        vertexList.add(0F)

        return allocateFloatBuffer(vertexList.toFloatArray())
    }

    private fun calculateTexture(spanAngle: Float, count: Int): FloatBuffer {
        val textureList = ArrayList<Float>(count * 3)
        val textureRadius = 0.5F
        var currentAngle = 0F

        // 存放原点
        textureList.add(0.5F)
        textureList.add(0.5F)
        textureList.add(0F)

        // 水平角度从 0 到 360，逆时针进行
        while (currentAngle < 360F) {
            val curAngle = currentAngle.toRadians()

            // 纹理的中心需要和顶点的对其，所以需要偏移到 (0.5, 0.5, 0)
            val x = 0.5F - textureRadius * cos(curAngle)
            val y = 0.5F - textureRadius * sin(curAngle)
            val z = 0F

            textureList.add(x.toFloat())
            textureList.add(y.toFloat())
            textureList.add(z)

            currentAngle += spanAngle
        }

        // 增加最后一个点，即圆边第一个点，才能让圆闭合
        textureList.add(0.5F - textureRadius * cos(0F.toRadians()).toFloat())
        textureList.add(0.5F - textureRadius * sin(0F.toRadians()).toFloat())
        textureList.add(0F)

        return allocateFloatBuffer(textureList.toFloatArray())
    }

    private fun calculateNormal(count: Int): FloatBuffer {
        val normalList = ArrayList<Float>(count)
        // 因为构建的圆是在 xy 平面，卷绕方向为逆时针，所以延 z 轴向外
        for (i in 0 until count) {
            normalList.add(0F)
            normalList.add(0F)
            normalList.add(1F)
        }
        return allocateFloatBuffer(normalList.toFloatArray())
    }
}