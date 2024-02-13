package com.jiangpengyong.eglbox

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 申请 ByteBuffer
 *
 * @param capacity 字节长度
 */
fun allocateByteBuffer(capacity: Int): ByteBuffer {
    val allocateDirect = ByteBuffer.allocateDirect(capacity)
    // 设置字节顺序
    allocateDirect.order(ByteOrder.nativeOrder())
    return allocateDirect
}

/**
 * 申请 FloatBuffer
 *
 * @param capacity float个数
 */
fun allocateFloatBuffer(capacity: Int): FloatBuffer {
    // 因为一个浮点数四个字节
    return allocateByteBuffer(capacity * 4).asFloatBuffer()
}

/**
 * 申请 FloatBuffer，同时放入数据
 *
 * @param data 需要放入的数据
 */
fun allocateFloatBuffer(data: FloatArray): FloatBuffer {
    // 一个浮点数四个字节
    val floatBuffer = allocateFloatBuffer(data.size)
    // 向缓冲区中放入顶点坐标数据
    floatBuffer.put(data)
    // 设置缓冲区起始位置
    floatBuffer.position(0)
    return floatBuffer
}