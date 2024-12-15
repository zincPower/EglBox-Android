package com.jiangpengyong.eglbox_filter.program

import android.opengl.GLES20

enum class TextureType(val value: Int) { SolidColor(1), CheckeredColor(2), Texture(3) }
enum class ColorType { SolidColor, CheckeredColor }
enum class LightCalculateType { Vertex, Fragment }
enum class LightSourceType { PointLight, DirectionalLight }
enum class DrawMode(val value: Int) {
    Triangles(GLES20.GL_TRIANGLES),
    TriangleFan(GLES20.GL_TRIANGLE_FAN),
    TriangleStrip(GLES20.GL_TRIANGLE_STRIP),
    Lines(GLES20.GL_LINES),
    LineStrip(GLES20.GL_LINE_STRIP),
    LineLoop(GLES20.GL_LINE_LOOP),
}

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float)
data class Light(val red: Float, val green: Float, val blue: Float, val alpha: Float)