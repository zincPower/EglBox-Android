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

    val space: Space
        get() {
            if (count <= 0) return Space(0F, 0F, 0F, 0F, 0F, 0F)
            var top = Float.MIN_VALUE
            var bottom = Float.MAX_VALUE
            var left = Float.MAX_VALUE
            var right = Float.MIN_VALUE
            var near = Float.MIN_VALUE
            var far = Float.MAX_VALUE
            for (i in 0 until count) {
                val x = vertexData[i * 3 + 0]
                val y = vertexData[i * 3 + 1]
                val z = vertexData[i * 3 + 2]
                top = Math.max(y, top)
                bottom = Math.min(y, bottom)
                left = Math.min(x, left)
                right = Math.max(x, right)
                near = Math.max(z, near)
                far = Math.min(z, far)
            }
            return Space(top, bottom, left, right, near, far)
        }
}

/**
 * 数据索引方式
 */
enum class DataIndexType() {

}

/**
 * 卷绕方向
 */
enum class FrontFace(val value: Int) {
    CW(GLES20.GL_CW),
    CCW(GLES20.GL_CCW);

    fun use() {
        GLES20.glFrontFace(value)
    }
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