package com.jiangpengyong.eglbox_filter

fun Int.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}

fun Float.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}

fun Double.toRadians(): Double {
    return Math.toRadians(this)
}

fun Int.toBoolean(): Boolean {
    return this != 0
}