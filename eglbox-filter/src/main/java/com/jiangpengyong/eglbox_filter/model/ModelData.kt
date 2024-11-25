package com.jiangpengyong.eglbox_filter.model

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import java.nio.FloatBuffer

data class ModelData(
    val vertexData: FloatArray,            // 顶点数据
    val textureData: FloatArray?,          // 纹理数据
    val textureStep: Int = 0,              // 纹理跨度
    val normalData: FloatArray?,           // 法向量数据
    val frontFace: FrontFace,              // 卷绕方向
    val space: Space,                      // 空间
    val sideRenderingType: SideRenderingType,// 面渲染方式
    val normalVectorType: NormalVectorType,  // 法向量计算方式
) {
    /**
     * 顶点数据
     */
    val count: Int
        get() = vertexData.size / 3

    /**
     * 是否有法向量数据
     */
    val hasNormal: Boolean
        get() = (normalData?.size ?: 0) > 0

    /**
     * 是否有纹理数据
     */
    val hasTexture: Boolean
        get() = (textureData?.size ?: 0) > 0

    val vertexBuffer: FloatBuffer
        get() = allocateFloatBuffer(vertexData)

    val textureBuffer: FloatBuffer?
        get() = textureData?.let { allocateFloatBuffer(it) }

    val normalBuffer: FloatBuffer?
        get() = normalData?.let { allocateFloatBuffer(it) }
}

/**
 * 数据索引方式
 */
enum class DataIndexType(){

}

/**
 * 卷绕方向
 */
enum class FrontFace(val value: Int) {
    CW(GLES20.GL_CW),
    CCW(GLES20.GL_CCW),
}

/**
 * 面渲染方式
 */
enum class SideRenderingType {
    Single,     // 单面
    Double,     // 双面
}

/**
 * 法向量类型
 */
enum class NormalVectorType {
    Vertex,     // 点法向量
    Surface,    // 面法向量
}


/**
 * 空间
 */
data class Space(
    val top: Float,
    val bottom: Float,
    val left: Float,
    val right: Float,
    val near: Float,
    val far: Float,
)