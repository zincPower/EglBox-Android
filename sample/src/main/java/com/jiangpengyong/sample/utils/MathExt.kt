package com.jiangpengyong.sample.utils

fun Int.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}

fun Float.toRadians(): Double {
    return Math.toRadians(this.toDouble())
}

fun Double.toRadians(): Double {
    return Math.toRadians(this)
}