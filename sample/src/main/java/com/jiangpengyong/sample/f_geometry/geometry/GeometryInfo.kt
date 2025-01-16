package com.jiangpengyong.sample.f_geometry.geometry

import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.program.DrawMode
import java.nio.FloatBuffer

data class GeometryInfo(
    val vertexBuffer: FloatBuffer,  // 顶点数据
    val textureBuffer: FloatBuffer, // 纹理数据
    val normalBuffer: FloatBuffer,  // 法向量数据
    val vertexCount: Int,                 // 个数
    val drawMode: DrawMode,         // 绘制方式
    val frontFace: FrontFace,       // 正面方向
)