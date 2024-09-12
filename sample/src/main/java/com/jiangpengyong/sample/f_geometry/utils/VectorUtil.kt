package com.jiangpengyong.sample.f_geometry.utils

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * @author jiang peng yong
 * @date 2024/8/31 14:48
 * @email 56002982@qq.com
 * @des 向量工具
 */
object VectorUtil {
    /**
     * 将向量归一化
     */
    fun normalize(vec: FloatArray): FloatArray {
        val mod = module(vec)
        return floatArrayOf(vec[0] / mod, vec[1] / mod, vec[2] / mod)
    }

    /**
     * 计算向量的长度
     */
    fun module(vec: FloatArray): Float {
        return sqrt((vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]).toDouble()).toFloat()
    }

    /**
     * 计算两个向量的叉积
     */
    fun cross(
        a: FloatArray,
        b: FloatArray
    ): FloatArray {
        val x = a[1] * b[2] - a[2] * b[1]
        val y = a[2] * b[0] - a[0] * b[2]
        val z = a[0] * b[1] - a[1] * b[0]
        return floatArrayOf(x, y, z)
    }

    fun cullTexture(
        textureList: ArrayList<Float>,
        indexList: ArrayList<Int>
    ): FloatArray {
        val result = FloatArray(indexList.size * 2)
        var count = 0
        for (i in indexList) {
            result[count++] = textureList[2 * i]
            result[count++] = textureList[2 * i + 1]
        }
        return result
    }

    /**
     * 根据顶点编号 indexList 从 vertexList 获取数据，进行组装坐标数据
     */
    fun cullVertex(
        vertexList: ArrayList<Float>,
        indexList: ArrayList<Int>
    ): FloatArray {
        val result = FloatArray(indexList.size * 3)
        var vCount = 0
        for (i in indexList) {
            result[vCount++] = vertexList[3 * i]
            result[vCount++] = vertexList[3 * i + 1]
            result[vCount++] = vertexList[3 * i + 2]
        }
        return result
    }

    fun split20Face(
        r: Float,           // 球的半径
        start: FloatArray,  // 指向圆弧起点的向量
        end: FloatArray,    // 指向圆弧终点的向量
        n: Int,             // 圆弧分的份数
        i: Int              // 求第 i 份在圆弧上的坐标（ i 为 0 和 n 时分别代表起点和终点坐标）
    ): FloatArray {
        val s = normalize(start)
        val e = normalize(end)
        if (n == 0) {
            return floatArrayOf(s[0] * r, s[1] * r, s[2] * r)
        }

        // 通过点积可以求出两个向量的 cos 值，反 cos 即可获取的夹角
        val angrad = acos(dotTwoVectors(s, e).toDouble())
        val angrad1 = angrad * i / n
        val angrad2 = angrad - angrad1

        // 通过叉积计算垂直于 s 和 e 两个向量平面的向量
        val normal = crossTwoVectors(s, e)

        // doolittle 分解算法求 n 元一次线性方程组
        val matrix = arrayOf(
            doubleArrayOf(s[0].toDouble(), s[1].toDouble(), s[2].toDouble(), cos(angrad1)),
            doubleArrayOf(e[0].toDouble(), e[1].toDouble(), e[2].toDouble(), cos(angrad2)),
            doubleArrayOf(normal[0].toDouble(), normal[1].toDouble(), normal[2].toDouble(), 0.0)
        )
        val result = MathUtil.doolittle(matrix)
        val x = result[0].toFloat()
        val y = result[1].toFloat()
        val z = result[2].toFloat()
        return floatArrayOf(x * r, y * r, z * r)
    }

    fun splitTexture(
        start: FloatArray,  // 线段起点坐标
        end: FloatArray,    // 线段终点坐标
        n: Int,             // 线段分的份数
        i: Int              // 求第 i 份在线段上的坐标（ i 为 0 和 n 时分别代表起点和终点坐标）
    ): FloatArray {
        if (n == 0) {
            return start
        }
        val ab = floatArrayOf(end[0] - start[0], end[1] - start[1], end[2] - start[2])
        val vecRatio = i / n.toFloat()
        val ac = floatArrayOf(ab[0] * vecRatio, ab[1] * vecRatio, ab[2] * vecRatio)
        val x = start[0] + ac[0]
        val y = start[1] + ac[1]
        val z = start[2] + ac[2]
        return floatArrayOf(x, y, z)
    }

    /**
     * 叉积
     */
    private fun crossTwoVectors(a: FloatArray, b: FloatArray): FloatArray {
        val x = a[1] * b[2] - a[2] * b[1]
        val y = a[2] * b[0] - a[0] * b[2]
        val z = a[0] * b[1] - a[1] * b[0]
        return floatArrayOf(x, y, z)
    }

    /**
     * 点积
     */
    private fun dotTwoVectors(a: FloatArray, b: FloatArray): Float {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
    }
}
