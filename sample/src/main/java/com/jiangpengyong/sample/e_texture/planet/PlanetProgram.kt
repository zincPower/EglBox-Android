package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.App
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/8/12 22:04
 * @email 56002982@qq.com
 * @des 天体
 */
class PlanetProgram : GLProgram() {
    private var mAngleSpan = 10
    private var mRadius = 1F

    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mLightPositionHandle = 0
    private var mCameraPositionHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mLightSourceTypeHandle = 0
    private var mTextureHandle = 0
    private var mTextureCoordHandle = 0

    private var mVertexCount = 0
    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mMMatrix: GLMatrix = GLMatrix()

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mNormalBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    private var mLightPosition = FloatArray(3)
    private var mCameraPosition = FloatArray(3)
    private var mShininess = 50F

    private var mTexture: GLTexture? = null

    init {
        calculateVertex()
    }

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setMMatrix(matrix: GLMatrix) {
        mMMatrix = matrix
    }

    fun setLightPosition(lightPosition: FloatArray) {
        mLightPosition = lightPosition
    }

    fun setCameraPosition(cameraPosition: FloatArray) {
        mCameraPosition = cameraPosition
    }

    fun setShininess(shininess: Float) {
        mShininess = shininess
    }

    fun setAngleSpan(angleSpan: Int) {
        mAngleSpan = angleSpan
        calculateVertex()
    }

    fun setTexture(texture: GLTexture) {
        mTexture = texture
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mMMatrixHandle = getUniformLocation("uMMatrix")
        mLightPositionHandle = getUniformLocation("uLightPosition")
        mCameraPositionHandle = getUniformLocation("uCameraPosition")
        mPositionHandle = getAttribLocation("aPosition")
        mTextureCoordHandle = getAttribLocation("aTextureCoord")
        mNormalHandle = getAttribLocation("aNormal")
        mShininessHandle = getAttribLocation("aShininess")
        mLightSourceTypeHandle = getUniformLocation("uLightSourceType")
        mTextureHandle = getUniformLocation("sTexture")
    }

    override fun onDraw() {
        mTexture?.bind()
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mMMatrix.matrix, 0)
        GLES20.glUniform3f(mLightPositionHandle, mLightPosition[0], mLightPosition[1], mLightPosition[2])
        GLES20.glUniform3f(mCameraPositionHandle, mCameraPosition[0], mCameraPosition[1], mCameraPosition[2])
        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mNormalBuffer)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
        mTexture?.unbind()
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mMMatrixHandle = 0
        mLightPositionHandle = 0
        mCameraPositionHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mLightSourceTypeHandle = 0
        mTextureHandle = 0
        mTextureCoordHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/heavenly_body/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/heavenly_body/fragment.glsl")

    private fun calculateVertex() {
        val vertexList = ArrayList<Float>()

        var verticalAngle = -90.0

        // 计算中间每一层的点
        while (verticalAngle < 90F) {   // 垂直角度从 -90 到 90
            // 这一层的半径
            val curLayerAngle = verticalAngle.toRadians()
            val layerRadius = mRadius * cos(curLayerAngle)

            // 下一层的半径
            val nextLayerAngle = (verticalAngle + mAngleSpan).toRadians()
            val nextLayerRadius = mRadius * cos(nextLayerAngle)

            val curLayerY = mRadius * sin(curLayerAngle)
            val nextLayerY = mRadius * sin(nextLayerAngle)

            var horizontalAngle = 0.0
            while (horizontalAngle < 360) {     // 水平角度从 0 到 360
                val curHorAngle = horizontalAngle.toRadians()
                val nextHorAngle = (horizontalAngle + mAngleSpan).toRadians()

                /**
                 *     P2(x2, y2, z2)   P3(x3, y3, z3)
                 *      ------------------
                 *      ｜              ╱｜
                 *      ｜            ╱  ｜
                 *      ｜          ╱    ｜
                 *      ｜        ╱      ｜
                 *      ｜      ╱        ｜
                 *      ｜    ╱          ｜
                 *      ｜  ╱            ｜
                 *      ｜╱              ｜
                 *      ------------------
                 *     P1(x1, y1, z1)   P0(x0, y0, z0)
                 */
                val x0 = layerRadius * cos(curHorAngle)
                val y0 = curLayerY
                val z0 = layerRadius * sin(curHorAngle)

                val x1 = layerRadius * cos(nextHorAngle)
                val y1 = curLayerY
                val z1 = layerRadius * sin(nextHorAngle)

                val x2 = nextLayerRadius * cos(nextHorAngle)
                val y2 = nextLayerY
                val z2 = nextLayerRadius * sin(nextHorAngle)

                val x3 = nextLayerRadius * cos(curHorAngle)
                val y3 = nextLayerY
                val z3 = nextLayerRadius * sin(curHorAngle)

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                vertexList.add(x0.toFloat())
                vertexList.add(y0.toFloat())
                vertexList.add(z0.toFloat())

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x2.toFloat())
                vertexList.add(y2.toFloat())
                vertexList.add(z2.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                horizontalAngle += mAngleSpan
            }
            verticalAngle += mAngleSpan
        }
        mVertexCount = vertexList.size / 3
        mVertexBuffer = allocateFloatBuffer(vertexList.toFloatArray())
        // 因为球体的几何体征，球心在原点，所以各个点法向量和顶点位置刚好一致，不用再次计算
        mNormalBuffer = allocateFloatBuffer(vertexList.toFloatArray())

        val textureList = ArrayList<Float>()
        val vCounts = 180 / mAngleSpan
        val vSpan = 1 / vCounts.toFloat()
        val hCounts = 360 / mAngleSpan
        val hSpan = 1 / hCounts.toFloat()
        for (v in 0 until vCounts) {
            for (h in 0 until hCounts) {
                /**
                 *     P2(x2, y2)   P3(x3, y3)
                 *      ------------------
                 *      ｜              ╱｜
                 *      ｜            ╱  ｜
                 *      ｜          ╱    ｜
                 *      ｜        ╱      ｜
                 *      ｜      ╱        ｜
                 *      ｜    ╱          ｜
                 *      ｜  ╱            ｜
                 *      ｜╱              ｜
                 *      ------------------
                 *     P1(x1, y1)   P0(x0, y0)
                 */
                val x0 = 1 - h * hSpan
                val y0 = 1 - v * vSpan

                val x1 = 1 - (h + 1) * hSpan
                val y1 = 1 - v * vSpan

                val x2 = 1 - (h + 1) * hSpan
                val y2 = 1 - (v + 1) * vSpan

                val x3 = 1 - h * hSpan
                val y3 = 1 - (v + 1) * vSpan

                textureList.add(x1)
                textureList.add(y1)

                textureList.add(x3)
                textureList.add(y3)

                textureList.add(x0)
                textureList.add(y0)

                textureList.add(x1)
                textureList.add(y1)

                textureList.add(x2)
                textureList.add(y2)

                textureList.add(x3)
                textureList.add(y3)
            }
        }
        mTextureBuffer = allocateFloatBuffer(textureList.toFloatArray())
    }

    private fun Double.toRadians(): Double {
        return Math.toRadians(this)
    }
}
