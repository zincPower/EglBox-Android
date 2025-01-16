package com.jiangpengyong.sample.f_geometry.geometry.shape

import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.program.DrawMode
import com.jiangpengyong.eglbox_filter.utils.VectorUtil
import com.jiangpengyong.sample.f_geometry.geometry.GeometryInfo
import kotlin.math.sqrt

/**
 * @author jiang peng yong
 * @date 2024/9/9 21:46
 * @email 56002982@qq.com
 * @des 基于正二十面体的外接球
 */
class GeometryBall(
    val length: Float,  // 长边长度
    val segment: Int,   // 二十面体每个面的边裁剪份
) {
    private var mRadius = 0F
    private var mShortSideHalfLength = 0F
    private var mGeometryInfo: GeometryInfo? = null

    fun create(): GeometryInfo {
        val geometryInfo = mGeometryInfo
        if (geometryInfo != null) {
            return geometryInfo
        }

        // 长边的一半
        val mLongSideHalfLength = length / 2
        // 短边的一半，正二十面体长边和短边的比例是黄金比例
        mShortSideHalfLength = mLongSideHalfLength * 0.618034F
        // 计算几何球的半径，因为是外接球，所以半径为长方形对角线长度的一半
        mRadius = sqrt((mLongSideHalfLength * mLongSideHalfLength + mShortSideHalfLength * mShortSideHalfLength).toDouble()).toFloat()
        // 几何球的顶点个数
        val vCount = 3 * 20 * segment * segment

        // 正二十面体的顶点列表
        val vertex20List = ArrayList<Float>()
        // 正二十面体三角形顶点索引
        val vertex20IndexList = ArrayList<Int>()

        // 组装正二十面体的顶点坐标
        assembleVertex20(vertex20List, mLongSideHalfLength, mShortSideHalfLength)
        // 组装正二十面体的三角形顶点，卷绕方向为逆时针
        assembleVertex20Index(vertex20IndexList)
        // 正二十面体的三角形顶点
        val vertices20 = VectorUtil.cullVertex(vertex20List, vertex20IndexList)

        // 几何球顶点列表（外接球的真是列表）
        val vertexList = ArrayList<Float>()
        // 几何球三角形顶点索引
        val indexList = ArrayList<Int>()
        var count = 0
        run {
            var k = 0
            while (k < vertices20.size) {
                val v1 = floatArrayOf(vertices20[k + 0], vertices20[k + 1], vertices20[k + 2])
                val v2 = floatArrayOf(vertices20[k + 3], vertices20[k + 4], vertices20[k + 5])
                val v3 = floatArrayOf(vertices20[k + 6], vertices20[k + 7], vertices20[k + 8])

                // 计算二十面体每个面上的拆分的点
                for (i in 0..segment) {
                    // 切分二十面体的边
                    val viStart = VectorUtil.split20Face(mRadius, v1, v2, segment, i)
                    val viEnd = VectorUtil.split20Face(mRadius, v1, v3, segment, i)
                    for (j in 0..i) {
                        val vi = VectorUtil.split20Face(mRadius, viStart, viEnd, i, j)
                        vertexList.add(vi[0])
                        vertexList.add(vi[1])
                        vertexList.add(vi[2])
                    }
                }

                // 组装球的三角形顶点索引列表
                for (i in 0 until segment) {
                    // 第一行只有一个三角形，特殊处理
                    if (i == 0) {
                        indexList.add(count + 0)
                        indexList.add(count + 1)
                        indexList.add(count + 2)
                        count += 1
                        continue
                    }

                    // 当前行数据
                    val iStart = count
                    val viCount = i + 1
                    val iEnd = iStart + viCount - 1

                    // 下一行数据
                    val iStartNext = iStart + viCount
                    val viCountNext = viCount + 1
                    val iEndNext = iStartNext + viCountNext - 1

                    // 构造四边形
                    for (j in 0 until viCount - 1) {
                        val index0 = iStart + j
                        val index1 = index0 + 1
                        val index2 = iStartNext + j
                        val index3 = index2 + 1

                        indexList.add(index0)
                        indexList.add(index2)
                        indexList.add(index3)
                        indexList.add(index0)
                        indexList.add(index3)
                        indexList.add(index1)
                    }

                    // 添加最后的三角形
                    indexList.add(iEnd)
                    indexList.add(iEndNext - 1)
                    indexList.add(iEndNext)

                    count += viCount

                    // 如果是最后一次循环，将最后的所有顶点都加上
                    if (i == segment - 1) {
                        count += viCountNext
                    }
                }
                k += 9
            }
        }

        val vertexResultList = VectorUtil.cullVertex(vertexList, indexList)
        val normalResultList = vertexResultList

        val texture20List = ArrayList<Float>()
        val texture20IndexList = ArrayList<Int>()

        // 正二十面体三角形边长占纹理图宽的份数，
        val sSpan = 1 / 5.5F
        // 正二十面体三角形高占纹理图高的份数
        val tSpan = 1 / 3.0F
        // 二十面体的纹理
        for (i in 0..4) {
            texture20List.add(sSpan + sSpan * i)
            texture20List.add(1F)
        }
        for (i in 0..5) {
            texture20List.add(sSpan / 2 + sSpan * i)
            texture20List.add(tSpan * 2)
        }
        for (i in 0..5) {
            texture20List.add(sSpan * i)
            texture20List.add(tSpan)
        }
        for (i in 0..4) {
            texture20List.add(sSpan / 2 + sSpan * i)
            texture20List.add(0F)
        }

        // 组装正二十面体三角形的纹理坐标
        assembleTexture20Index(texture20IndexList)

        // 正二十面体的三角形纹理
        val texture20 = VectorUtil.cullTexture(texture20List, texture20IndexList)
        // 纹理坐标列表
        val textureList = ArrayList<Float>()
        // 对二十正面体进行划分
        var k = 0
        while (k < texture20.size) {
            val st1 = floatArrayOf(texture20[k + 0], texture20[k + 1], 0f)
            val st2 = floatArrayOf(texture20[k + 2], texture20[k + 3], 0f)
            val st3 = floatArrayOf(texture20[k + 4], texture20[k + 5], 0f)
            for (i in 0..segment) {
                val stiStart = VectorUtil.splitTexture(st1, st2, segment, i)
                val stiEnd = VectorUtil.splitTexture(st1, st3, segment, i)
                for (j in 0..i) {
                    val sti = VectorUtil.splitTexture(stiStart, stiEnd, i, j)

                    textureList.add(sti[0])
                    textureList.add(sti[1])
                }
            }
            k += 6
        }
        // 按照索引组装真实纹理数据
        val textures = VectorUtil.cullTexture(textureList, indexList)

        return GeometryInfo(
            vertexBuffer = allocateFloatBuffer(vertexResultList),
            textureBuffer = allocateFloatBuffer(textures),
            normalBuffer = allocateFloatBuffer(normalResultList),
            vertexCount = vCount,
            drawMode = DrawMode.Triangles,
            frontFace = FrontFace.CCW,
        ).apply { mGeometryInfo = this }
    }

    private fun assembleVertex20(vertex20List: ArrayList<Float>, longSideHalfLength: Float, shortSideHalfLength: Float) {
        vertex20List.add(0f)
        vertex20List.add(longSideHalfLength)
        vertex20List.add(-shortSideHalfLength)

        vertex20List.add(0f)
        vertex20List.add(longSideHalfLength)
        vertex20List.add(shortSideHalfLength)

        vertex20List.add(longSideHalfLength)
        vertex20List.add(shortSideHalfLength)
        vertex20List.add(0f)

        vertex20List.add(shortSideHalfLength)
        vertex20List.add(0f)
        vertex20List.add(-longSideHalfLength)

        vertex20List.add(-shortSideHalfLength)
        vertex20List.add(0f)
        vertex20List.add(-longSideHalfLength)

        vertex20List.add(-longSideHalfLength)
        vertex20List.add(shortSideHalfLength)
        vertex20List.add(0f)

        vertex20List.add(-shortSideHalfLength)
        vertex20List.add(0f)
        vertex20List.add(longSideHalfLength)

        vertex20List.add(shortSideHalfLength)
        vertex20List.add(0f)
        vertex20List.add(longSideHalfLength)

        vertex20List.add(longSideHalfLength)
        vertex20List.add(-shortSideHalfLength)
        vertex20List.add(0f)

        vertex20List.add(0f)
        vertex20List.add(-longSideHalfLength)
        vertex20List.add(-shortSideHalfLength)

        vertex20List.add(-longSideHalfLength)
        vertex20List.add(-shortSideHalfLength)
        vertex20List.add(0f)

        vertex20List.add(0f)
        vertex20List.add(-longSideHalfLength)
        vertex20List.add(shortSideHalfLength)
    }

    private fun assembleVertex20Index(vertex20IndexList: ArrayList<Int>) {
        vertex20IndexList.add(0)
        vertex20IndexList.add(1)
        vertex20IndexList.add(2)
        vertex20IndexList.add(0)
        vertex20IndexList.add(2)
        vertex20IndexList.add(3)
        vertex20IndexList.add(0)
        vertex20IndexList.add(3)
        vertex20IndexList.add(4)
        vertex20IndexList.add(0)
        vertex20IndexList.add(4)
        vertex20IndexList.add(5)
        vertex20IndexList.add(0)
        vertex20IndexList.add(5)
        vertex20IndexList.add(1)

        vertex20IndexList.add(1)
        vertex20IndexList.add(6)
        vertex20IndexList.add(7)
        vertex20IndexList.add(1)
        vertex20IndexList.add(7)
        vertex20IndexList.add(2)
        vertex20IndexList.add(2)
        vertex20IndexList.add(7)
        vertex20IndexList.add(8)
        vertex20IndexList.add(2)
        vertex20IndexList.add(8)
        vertex20IndexList.add(3)
        vertex20IndexList.add(3)
        vertex20IndexList.add(8)
        vertex20IndexList.add(9)
        vertex20IndexList.add(3)
        vertex20IndexList.add(9)
        vertex20IndexList.add(4)
        vertex20IndexList.add(4)
        vertex20IndexList.add(9)
        vertex20IndexList.add(10)
        vertex20IndexList.add(4)
        vertex20IndexList.add(10)
        vertex20IndexList.add(5)
        vertex20IndexList.add(5)
        vertex20IndexList.add(10)
        vertex20IndexList.add(6)
        vertex20IndexList.add(5)
        vertex20IndexList.add(6)
        vertex20IndexList.add(1)

        vertex20IndexList.add(6)
        vertex20IndexList.add(11)
        vertex20IndexList.add(7)
        vertex20IndexList.add(7)
        vertex20IndexList.add(11)
        vertex20IndexList.add(8)
        vertex20IndexList.add(8)
        vertex20IndexList.add(11)
        vertex20IndexList.add(9)
        vertex20IndexList.add(9)
        vertex20IndexList.add(11)
        vertex20IndexList.add(10)
        vertex20IndexList.add(10)
        vertex20IndexList.add(11)
        vertex20IndexList.add(6)
    }

    private fun assembleTexture20Index(alTexIndex20: ArrayList<Int>) {
        alTexIndex20.add(0)
        alTexIndex20.add(5)
        alTexIndex20.add(6)
        alTexIndex20.add(1)
        alTexIndex20.add(6)
        alTexIndex20.add(7)
        alTexIndex20.add(2)
        alTexIndex20.add(7)
        alTexIndex20.add(8)
        alTexIndex20.add(3)
        alTexIndex20.add(8)
        alTexIndex20.add(9)
        alTexIndex20.add(4)
        alTexIndex20.add(9)
        alTexIndex20.add(10)

        alTexIndex20.add(5)
        alTexIndex20.add(11)
        alTexIndex20.add(12)
        alTexIndex20.add(5)
        alTexIndex20.add(12)
        alTexIndex20.add(6)
        alTexIndex20.add(6)
        alTexIndex20.add(12)
        alTexIndex20.add(13)
        alTexIndex20.add(6)
        alTexIndex20.add(13)
        alTexIndex20.add(7)
        alTexIndex20.add(7)
        alTexIndex20.add(13)
        alTexIndex20.add(14)
        alTexIndex20.add(7)
        alTexIndex20.add(14)
        alTexIndex20.add(8)
        alTexIndex20.add(8)
        alTexIndex20.add(14)
        alTexIndex20.add(15)
        alTexIndex20.add(8)
        alTexIndex20.add(15)
        alTexIndex20.add(9)
        alTexIndex20.add(9)
        alTexIndex20.add(15)
        alTexIndex20.add(16)
        alTexIndex20.add(9)
        alTexIndex20.add(16)
        alTexIndex20.add(10)

        alTexIndex20.add(11)
        alTexIndex20.add(17)
        alTexIndex20.add(12)
        alTexIndex20.add(12)
        alTexIndex20.add(18)
        alTexIndex20.add(13)
        alTexIndex20.add(13)
        alTexIndex20.add(19)
        alTexIndex20.add(14)
        alTexIndex20.add(14)
        alTexIndex20.add(20)
        alTexIndex20.add(15)
        alTexIndex20.add(15)
        alTexIndex20.add(21)
        alTexIndex20.add(16)
    }
}
