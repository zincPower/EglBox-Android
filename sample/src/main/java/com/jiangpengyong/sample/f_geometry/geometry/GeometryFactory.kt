package com.jiangpengyong.sample.f_geometry.geometry

import android.opengl.GLES20
import com.jiangpengyong.sample.f_geometry.geometry.shape.Circle
import java.nio.FloatBuffer

data class GeometryInfo(
    val vertexBuffer: FloatBuffer,  // 顶点数据
    val textureBuffer: FloatBuffer, // 纹理数据
    val normalBuffer: FloatBuffer,  // 法向量数据
    val vertexCount: Int,                 // 个数
    val drawMode: DrawMode,         // 绘制方式
    val frontFace: FrontFace,       // 正面方向
)

enum class DrawMode(val value: Int) {
    Triangles(GLES20.GL_TRIANGLES),
    TriangleFan(GLES20.GL_TRIANGLE_FAN),
    TriangleStrip(GLES20.GL_TRIANGLE_STRIP),
}

enum class FrontFace(val value: Int) {
    CW(GLES20.GL_CW),
    CCW(GLES20.GL_CCW),
}

/**
 * @author jiang peng yong
 * @date 2024/8/27 08:16
 * @email 56002982@qq.com
 * @des 几何体工厂
 */
object GeometryFactory {
    fun createCircle(
        radius: Float,      // 半径
        segment: Int,       // 裁剪份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
    ): GeometryInfo {
        return Circle(radius, segment).create()
    }
}