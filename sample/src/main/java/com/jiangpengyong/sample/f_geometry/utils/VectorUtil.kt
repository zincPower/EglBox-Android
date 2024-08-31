package com.jiangpengyong.sample.f_geometry.utils

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
}
