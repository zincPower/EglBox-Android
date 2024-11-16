package com.jiangpengyong.eglbox_core.utils

import com.jiangpengyong.eglbox_core.space3d.Vector
import kotlin.math.sqrt

object Math3D {
    fun crossProduct(v1: Vector, v2: Vector): Vector {
        return Vector(
            x = v1.y * v2.z - v2.y * v1.z,
            y = v1.z * v2.x - v2.z * v1.x,
            z = v1.x * v2.y - v2.x * v1.y,
        )
    }

    fun vectorNormal(vector: Vector): Vector {
        val module = sqrt((vector.x * vector.x + vector.y * vector.y + vector.z * vector.z).toDouble()).toFloat()
        return Vector(x = vector.x / module, y = vector.y / module, z = vector.z / module)
    }
}