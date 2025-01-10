package com.jiangpengyong.eglbox_filter.utils

import com.jiangpengyong.eglbox_core.space3d.Vector
import com.jiangpengyong.eglbox_core.utils.Math3D

/**
 * @author jiang peng yong
 * @date 2025/1/9 08:24
 * @email 56002982@qq.com
 * @des 法向量，计算平均法向量
 */
class Normal {
    private val vectors = HashSet<Vector>()

    fun add(vector: Vector) {
        for (item in vectors) {
            if (
                (item.x - THRESHOLD) <= vector.x && vector.x <= (item.x + THRESHOLD) &&
                (item.y - THRESHOLD) <= vector.y && vector.y <= (item.y + THRESHOLD) &&
                (item.z - THRESHOLD) <= vector.z && vector.z <= (item.z + THRESHOLD)
            ) {
//                Log.i(TAG, "添加的 Vector 已存在集合中，vector=${vector} 相同 vector=${item}")
                return
            }
        }
        vectors.add(vector)
    }

    fun getAvg(): Vector {
        var x = 0F
        var y = 0F
        var z = 0F
        for (vector in vectors) {
            x += vector.x
            y += vector.y
            z += vector.z
        }
        return Math3D.vectorNormal(Vector(x = x, y = y, z = z))
    }

    companion object {
        private const val TAG = "Normal"

        // 向量阈值偏差
        private const val THRESHOLD = 0.000_001F
    }
}