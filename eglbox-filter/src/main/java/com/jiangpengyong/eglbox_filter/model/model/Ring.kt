package com.jiangpengyong.eglbox_filter.model.model

import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.model.NormalVectorType
import com.jiangpengyong.eglbox_filter.utils.VectorUtil
import com.jiangpengyong.eglbox_filter.utils.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/8/31 17:44
 * @email 56002982@qq.com
 * @des 圆环
 */
class Ring(
    val majorRadius: Float,     // 主半径，圆环中心到圆环切面中心距离
    val minorRadius: Float,     // 子半径，圆环切面半径
    val majorSegment: Int,      // 裁剪主圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
    val minorSegment: Int,      // 裁剪子圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
) {
    private val mMajorSpanAngle = 360F / majorSegment
    private val mMinorSpanAngle = 360F / minorSegment
    private val mVertexOrgList = ArrayList<Float>()
    private val mTextureOrgList = ArrayList<Float>()

    private var mModelData: ModelData? = null

    fun create(): ModelData {
        val modelData = mModelData
        if (modelData != null) {
            return modelData
        }

        initData()

        val vertexList = ArrayList<Float>()
        val textureList = ArrayList<Float>()
        val normalList = ArrayList<Float>()
        for (majorIndex in 0 until majorSegment) {
            // 组装顶点坐标
            // 当前点，minorSegment + 1 因为每一个圈都多一个计算
            val curVertexIndex = majorIndex * (minorSegment + 1) * 3
            // 下一圈点，majorIndex + 1 因为推进下一个圈
            val nextVertexIndex = (majorIndex + 1) * (minorSegment + 1) * 3
            for (minorIndex in 0 until minorSegment) {
                assembleVertex(vertexList, curVertexIndex, nextVertexIndex, minorIndex)
                assembleNormal(normalList, curVertexIndex, nextVertexIndex, minorIndex)
                assembleTexture(textureList, majorIndex, minorIndex)
            }
        }

        return ModelData(
            vertexData = vertexList.toFloatArray(),
            textureData = textureList.toFloatArray(),
            textureStep = 2,
            normalData = normalList.toFloatArray(),
            frontFace = FrontFace.CCW,
            normalVectorType = NormalVectorType.Vertex,
        ).apply { mModelData = this }
    }

    private fun initData() {
        var curMajorAngle = 0F
        while (curMajorAngle < 360F) {
            val curMajorRadian = curMajorAngle.toRadians().toFloat()
            calculateVertex(mVertexOrgList, curMajorRadian)
            calculateTexture(mTextureOrgList, curMajorAngle)
            curMajorAngle += mMajorSpanAngle
        }
        calculateVertex(mVertexOrgList, 0F.toRadians().toFloat())
        calculateTexture(mTextureOrgList, 360F)
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

        val curMinorRadian = 360F.toRadians()
        val temp = majorRadius + minorRadius * cos(curMinorRadian)
        val x = temp * cos(curMajorRadian)
        val y = minorRadius * sin(curMinorRadian)
        val z = temp * sin(curMajorRadian)
        vertexOrgList.add(x.toFloat())
        vertexOrgList.add(y.toFloat())
        vertexOrgList.add(z.toFloat())
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

        val s = 360F / 360F
        textureOrgList.add(s)
        textureOrgList.add(t)
    }

    private fun assembleVertex(
        vertexList: ArrayList<Float>,
        curVertexIndex: Int,
        nextVertexIndex: Int,
        minorIndex: Int
    ) {
        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 0])
        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 1])
        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 2])

        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 0])
        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 1])
        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 2])

        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 3])
        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 4])
        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 5])

        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 3])
        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 4])
        vertexList.add(mVertexOrgList[curVertexIndex + minorIndex * 3 + 5])

        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 3])
        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 4])
        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 5])

        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 0])
        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 1])
        vertexList.add(mVertexOrgList[nextVertexIndex + minorIndex * 3 + 2])
    }

    private fun assembleTexture(textureList: java.util.ArrayList<Float>, majorIndex: Int, minorIndex: Int) {
        // 组装纹理顶点
        val curTextureIndex = majorIndex * (minorSegment + 1) * 2
        val nextTextureIndex = (majorIndex + 1) * (minorSegment + 1) * 2

        textureList.add(mTextureOrgList[curTextureIndex + minorIndex * 2 + 0])
        textureList.add(mTextureOrgList[curTextureIndex + minorIndex * 2 + 1])

        textureList.add(mTextureOrgList[nextTextureIndex + minorIndex * 2 + 0])
        textureList.add(mTextureOrgList[nextTextureIndex + minorIndex * 2 + 1])

        textureList.add(mTextureOrgList[nextTextureIndex + minorIndex * 2 + 2])
        textureList.add(mTextureOrgList[nextTextureIndex + minorIndex * 2 + 3])

        textureList.add(mTextureOrgList[nextTextureIndex + minorIndex * 2 + 2])
        textureList.add(mTextureOrgList[nextTextureIndex + minorIndex * 2 + 3])

        textureList.add(mTextureOrgList[curTextureIndex + minorIndex * 2 + 2])
        textureList.add(mTextureOrgList[curTextureIndex + minorIndex * 2 + 3])

        textureList.add(mTextureOrgList[curTextureIndex + minorIndex * 2 + 0])
        textureList.add(mTextureOrgList[curTextureIndex + minorIndex * 2 + 1])
    }

    private fun assembleNormal(
        normalList: ArrayList<Float>,
        curVertexIndex: Int,
        nextVertexIndex: Int,
        minorIndex: Int
    ) {
        // 组装法向量
        val normalVector1 = VectorUtil.cross(
            floatArrayOf(
                mVertexOrgList[curVertexIndex + minorIndex * 3 + 0] - mVertexOrgList[nextVertexIndex + minorIndex * 3 + 0],
                mVertexOrgList[curVertexIndex + minorIndex * 3 + 1] - mVertexOrgList[nextVertexIndex + minorIndex * 3 + 1],
                mVertexOrgList[curVertexIndex + minorIndex * 3 + 2] - mVertexOrgList[nextVertexIndex + minorIndex * 3 + 2]
            ),
            floatArrayOf(
                mVertexOrgList[curVertexIndex + minorIndex * 3 + 3] - mVertexOrgList[curVertexIndex + minorIndex * 3 + 0],
                mVertexOrgList[curVertexIndex + minorIndex * 3 + 4] - mVertexOrgList[curVertexIndex + minorIndex * 3 + 1],
                mVertexOrgList[curVertexIndex + minorIndex * 3 + 5] - mVertexOrgList[curVertexIndex + minorIndex * 3 + 2]
            ),
        )
        val normalVector2 = VectorUtil.cross(
            floatArrayOf(
                mVertexOrgList[nextVertexIndex + minorIndex * 3 + 3] - mVertexOrgList[curVertexIndex + minorIndex * 3 + 3],
                mVertexOrgList[nextVertexIndex + minorIndex * 3 + 4] - mVertexOrgList[curVertexIndex + minorIndex * 3 + 4],
                mVertexOrgList[nextVertexIndex + minorIndex * 3 + 5] - mVertexOrgList[curVertexIndex + minorIndex * 3 + 5]
            ),
            floatArrayOf(
                mVertexOrgList[nextVertexIndex + minorIndex * 3 + 0] - mVertexOrgList[nextVertexIndex + minorIndex * 3 + 3],
                mVertexOrgList[nextVertexIndex + minorIndex * 3 + 1] - mVertexOrgList[nextVertexIndex + minorIndex * 3 + 4],
                mVertexOrgList[nextVertexIndex + minorIndex * 3 + 2] - mVertexOrgList[nextVertexIndex + minorIndex * 3 + 5]
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
    }
}