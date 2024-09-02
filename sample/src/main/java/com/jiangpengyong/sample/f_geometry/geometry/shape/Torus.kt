package com.jiangpengyong.sample.f_geometry.geometry.shape

import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.f_geometry.geometry.DrawMode
import com.jiangpengyong.sample.f_geometry.geometry.FrontFace
import com.jiangpengyong.sample.f_geometry.geometry.GeometryInfo
import com.jiangpengyong.sample.f_geometry.utils.VectorUtil
import com.jiangpengyong.sample.utils.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/8/31 17:44
 * @email 56002982@qq.com
 * @des 圆环
 */
class Torus(
    val majorRadius: Float,     // 主半径，圆环中心到圆环切面中心距离
    val minorRadius: Float,     // 子半径，圆环切面半径
    val majorSegment: Int,      // 裁剪主圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
    val minorSegment: Int,      // 裁剪子圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
) {
    private var mGeometryInfo: GeometryInfo? = null
    private val mMajorSpanAngle = 360F / majorSegment
    private val mMinorSpanAngle = 360F / minorSegment

    fun create(): GeometryInfo {
        val geometryInfo = mGeometryInfo
        if (geometryInfo != null) {
            return geometryInfo
        }

        // 计算顶点坐标
        val vertexOrgList = ArrayList<Float>()
        var curMajorAngle = 0F
        while (curMajorAngle < 360F) {
            val curMajorRadian = curMajorAngle.toRadians().toFloat()
            calculateVertex(vertexOrgList, curMajorRadian)
            curMajorAngle += mMajorSpanAngle
        }
        calculateVertex(vertexOrgList, 0F.toRadians().toFloat())

        // 计算纹理坐标
        val textureOrgList = ArrayList<Float>()
        curMajorAngle = 0F
        while (curMajorAngle < 360F) {
            calculateTexture(textureOrgList, curMajorAngle)
            curMajorAngle += mMajorSpanAngle
        }
        calculateTexture(textureOrgList, 0F)

        val vertexList = ArrayList<Float>()
        val textureList = ArrayList<Float>()
        val normalList = ArrayList<Float>()
        for (majorIndex in 0 until majorSegment) {
            // 组装顶点坐标
            val curVertexIndex = majorIndex * minorSegment * 3
            val nextVertexIndex = majorIndex * minorSegment * 3
            for (minorIndex in 0 until minorSegment) {
                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 0])
                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 1])
                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 2])

                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 0])
                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 1])
                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 2])

                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 3])
                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 4])
                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 5])

                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 3])
                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 4])
                vertexList.add(vertexOrgList[nextVertexIndex + minorIndex * 3 + 5])

                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 3])
                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 4])
                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 5])

                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 0])
                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 1])
                vertexList.add(vertexOrgList[curVertexIndex + minorIndex * 3 + 2])

                // 组装法向量
                val normalVector1 = VectorUtil.cross(
                    floatArrayOf(
                        vertexOrgList[nextVertexIndex + 0] - vertexOrgList[curVertexIndex + 0],
                        vertexOrgList[nextVertexIndex + 1] - vertexOrgList[curVertexIndex + 1],
                        vertexOrgList[nextVertexIndex + 2] - vertexOrgList[curVertexIndex + 2]
                    ),
                    floatArrayOf(
                        vertexOrgList[nextVertexIndex + 3] - vertexOrgList[nextVertexIndex + 0],
                        vertexOrgList[nextVertexIndex + 4] - vertexOrgList[nextVertexIndex + 1],
                        vertexOrgList[nextVertexIndex + 5] - vertexOrgList[nextVertexIndex + 2]
                    )
                )
                val normalVector2 = VectorUtil.cross(
                    floatArrayOf(
                        vertexOrgList[nextVertexIndex + 3] - vertexOrgList[curVertexIndex + 3],
                        vertexOrgList[nextVertexIndex + 4] - vertexOrgList[curVertexIndex + 4],
                        vertexOrgList[nextVertexIndex + 5] - vertexOrgList[curVertexIndex + 5]
                    ),
                    floatArrayOf(
                        vertexOrgList[curVertexIndex + 3] - vertexOrgList[curVertexIndex + 0],
                        vertexOrgList[curVertexIndex + 4] - vertexOrgList[curVertexIndex + 1],
                        vertexOrgList[curVertexIndex + 5] - vertexOrgList[curVertexIndex + 2]
                    )
                )
                normalList.add(normalVector1[0])
                normalList.add(normalVector1[1])
                normalList.add(normalVector1[2])

                normalList.add(normalVector1[0])
                normalList.add(normalVector1[1])
                normalList.add(normalVector1[2])

                normalList.add(normalVector1[0])
                normalList.add(normalVector1[1])
                normalList.add(normalVector1[2])

                normalList.add(normalVector2[0])
                normalList.add(normalVector2[1])
                normalList.add(normalVector2[2])

                normalList.add(normalVector2[0])
                normalList.add(normalVector2[1])
                normalList.add(normalVector2[2])

                normalList.add(normalVector2[0])
                normalList.add(normalVector2[1])
                normalList.add(normalVector2[2])

                // 组装纹理顶点
                val curTextureIndex = majorIndex * minorSegment * 2
                val nextTextureIndex = (majorIndex + 1) * minorSegment * 2

                textureList.add(textureOrgList[curTextureIndex + 0])
                textureList.add(textureOrgList[curTextureIndex + 1])

                textureList.add(textureOrgList[nextTextureIndex + 0])
                textureList.add(textureOrgList[nextTextureIndex + 1])

                textureList.add(textureOrgList[nextTextureIndex + 2])
                textureList.add(textureOrgList[nextTextureIndex + 3])

                textureList.add(textureOrgList[nextTextureIndex + 2])
                textureList.add(textureOrgList[nextTextureIndex + 3])

                textureList.add(textureOrgList[curTextureIndex + 2])
                textureList.add(textureOrgList[curTextureIndex + 3])

                textureList.add(textureOrgList[curTextureIndex + 0])
                textureList.add(textureOrgList[curTextureIndex + 1])
            }
        }

        return GeometryInfo(
            vertexBuffer = allocateFloatBuffer(vertexList.toFloatArray()),
            textureBuffer = allocateFloatBuffer(textureList.toFloatArray()),
            normalBuffer = allocateFloatBuffer(normalList.toFloatArray()),
            vertexCount = vertexList.size / 3,
            drawMode = DrawMode.Triangles,
            frontFace = FrontFace.CCW,
        ).apply { mGeometryInfo = this }
    }

    private fun calculateVertex(vertexOrgList: ArrayList<Float>, curMajorRadian: Float) {
        var curMinorAngle = 0F
        while (curMinorAngle < 360F) {
            val curMinorRadian = curMinorAngle.toRadians()

            val temp = majorRadius + minorRadius * cos(curMinorRadian)

            val x = temp * cos(curMajorRadian)
            val y = minorRadius * sin(curMinorRadian)
            val z = temp * sin(curMajorRadian)

            vertexOrgList.add(x.toFloat())
            vertexOrgList.add(y.toFloat())
            vertexOrgList.add(z.toFloat())

            curMinorAngle += mMinorSpanAngle
        }
    }

    private fun calculateTexture(textureOrgList: ArrayList<Float>, curMajorAngle: Float) {
        var curMinorAngle = 0F
        val t = curMajorAngle / 360F
        while (curMinorAngle < 360F) {
            val s = curMinorAngle / 360F
            textureOrgList.add(s)
            textureOrgList.add(t)
            curMinorAngle += mMinorSpanAngle
        }
    }
}