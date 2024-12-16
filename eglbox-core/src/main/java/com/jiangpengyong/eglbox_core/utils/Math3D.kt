package com.jiangpengyong.eglbox_core.utils

import com.jiangpengyong.eglbox_core.space3d.Point
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
        val module = length(vector)
        return Vector(x = vector.x / module, y = vector.y / module, z = vector.z / module)
    }

    fun length(vector: Vector): Float {
        return sqrt((vector.x * vector.x + vector.y * vector.y + vector.z * vector.z).toDouble()).toFloat()
    }

    fun length(point1: Point, point2: Point): Float {
        return length(Vector(Math.abs(point2.x - point1.x), Math.abs(point2.y - point1.y), Math.abs(point2.z - point1.z)))
    }
}