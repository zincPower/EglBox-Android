package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.utils.toRadians
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/11/25 08:32
 * @email 56002982@qq.com
 * @des 轨道
 */
class OrbitProgram : GLProgram() {
    private var mAngleSpan = 5
    private var mRadius = 1F

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0

    private var mVertexCount = 0
    private var mMVPMatrix: GLMatrix = GLMatrix()
    private lateinit var mVertexBuffer: FloatBuffer

    init {
        calculateVertex()
    }

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setAngleSpan(angleSpan: Int) {
        mAngleSpan = angleSpan
        calculateVertex()
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
    }

    override fun getVertexShaderSource(): String = GLShaderExt.loadFromAssetsFile(App.context.resources, "glsl/texture/orbit/vertex.glsl")

    override fun getFragmentShaderSource(): String = GLShaderExt.loadFromAssetsFile(App.context.resources, "glsl/texture/orbit/fragment.glsl")

    private fun calculateVertex() {
        val vertexList = ArrayList<Float>()
        var angle = 0F
        while (angle < 360F) {     // 水平角度从 0 到 360
            val curAngle = angle.toRadians()

            val x = mRadius * cos(curAngle)
            val y = mRadius * sin(curAngle)
            val z = 0F

            vertexList.add(x.toFloat())
            vertexList.add(y.toFloat())
            vertexList.add(z)

            angle += mAngleSpan
        }
        mVertexCount = vertexList.size / 3
        mVertexBuffer = allocateFloatBuffer(vertexList.toFloatArray())
    }
}
