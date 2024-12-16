package com.jiangpengyong.sample.d_light.normal_type

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.App
import java.nio.FloatBuffer

/**
 * @author jiang peng yong
 * @date 2024/6/15 19:22
 * @email 56002982@qq.com
 * @des 绘制立方体
 */
class NormalTypeCubeProgram : GLProgram() {
    enum class NormalType(val value: Int) {
        VERTEX_NORMAL(1),
        FACE_NORMAL(2),
    }

    private val sideLength = 1F
    private val halfSideLength = sideLength / 2F
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            /**
             * 正面 向量向屏幕外
             *
             *  1-----------0
             *  ᐱ +y        │
             *  │           │
             *  │    原点    │
             *  │           │
             *  │           │
             *  2---------->3
             *             +x
             */
            halfSideLength, halfSideLength, halfSideLength,     // 0
            -halfSideLength, halfSideLength, halfSideLength,    // 1
            -halfSideLength, -halfSideLength, halfSideLength,   // 2
            halfSideLength, halfSideLength, halfSideLength,     // 0
            -halfSideLength, -halfSideLength, halfSideLength,   // 2
            halfSideLength, -halfSideLength, halfSideLength,    // 3
            /**
             * 后面 向量向屏幕里
             *
             *  3-----------0
             *  ᐱ +y        │
             *  │           │
             *  │    原点    │
             *  │           │
             *  │           │
             *  2---------->1
             *             +x
             */
            halfSideLength, halfSideLength, -halfSideLength,    // 0
            halfSideLength, -halfSideLength, -halfSideLength,   // 1
            -halfSideLength, -halfSideLength, -halfSideLength,  // 2
            halfSideLength, halfSideLength, -halfSideLength,    // 0
            -halfSideLength, -halfSideLength, -halfSideLength,  // 2
            -halfSideLength, halfSideLength, -halfSideLength,   // 3
            /**
             *  左面 向量向屏幕外
             *
             *  1-----------0
             *  ᐱ +y        │
             *  │           │
             *  │    原点    │
             *  │           │
             *  │           │
             *  2---------->3
             *             +z
             */
            -halfSideLength, halfSideLength, halfSideLength,    // 0
            -halfSideLength, halfSideLength, -halfSideLength,   // 1
            -halfSideLength, -halfSideLength, -halfSideLength,  // 2
            -halfSideLength, halfSideLength, halfSideLength,    // 0
            -halfSideLength, -halfSideLength, -halfSideLength,  // 2
            -halfSideLength, -halfSideLength, halfSideLength,   // 3
            /**
             * 右面 向量向屏幕内
             *
             *  3-----------0
             *  ᐱ +y        │
             *  │           │
             *  │    原点    │
             *  │           │
             *  │           │
             *  2---------->1
             *             +z
             */
            halfSideLength, halfSideLength, halfSideLength,     // 0
            halfSideLength, -halfSideLength, halfSideLength,    // 1
            halfSideLength, -halfSideLength, -halfSideLength,   // 2
            halfSideLength, halfSideLength, halfSideLength,     // 0
            halfSideLength, -halfSideLength, -halfSideLength,   // 2
            halfSideLength, halfSideLength, -halfSideLength,    // 3
            /**
             * 上面 向量向屏幕外
             *
             *  3---------->0
             *  │           │
             *  │           │
             *  │    原点    │
             *  │           │
             *  ᐯ +z        │
             *  2-----------1
             *             +x
             */
            halfSideLength, halfSideLength, -halfSideLength,    // 0
            -halfSideLength, halfSideLength, halfSideLength,    // 2
            halfSideLength, halfSideLength, halfSideLength,   // 1
            halfSideLength, halfSideLength, -halfSideLength,     // 0
            -halfSideLength, halfSideLength, -halfSideLength,   // 3
            -halfSideLength, halfSideLength, halfSideLength,    // 2
            /**
             *  下面 向量向屏幕内
             *
             *  3-----------0
             *  │           │
             *  │           │
             *  │    原点    │
             *  │           │
             *  ᐯ +z        │
             *  2---------->1
             *             +x
             */
            halfSideLength, -halfSideLength, -halfSideLength,    // 0
            halfSideLength, -halfSideLength, halfSideLength,   // 1
            -halfSideLength, -halfSideLength, halfSideLength,  // 2
            halfSideLength, -halfSideLength, -halfSideLength,    // 0
            -halfSideLength, -halfSideLength, halfSideLength,  // 2
            -halfSideLength, -halfSideLength, -halfSideLength,   // 3
        )
    )
    private val mFaceNormalBuffer = allocateFloatBuffer(
        floatArrayOf(
            // 正面
            0F, 0F, 1F,
            0F, 0F, 1F,
            0F, 0F, 1F,
            0F, 0F, 1F,
            0F, 0F, 1F,
            0F, 0F, 1F,

            // 后面
            0F, 0F, -1F,
            0F, 0F, -1F,
            0F, 0F, -1F,
            0F, 0F, -1F,
            0F, 0F, -1F,
            0F, 0F, -1F,

            // 左面
            -1F, 0F, 0F,
            -1F, 0F, 0F,
            -1F, 0F, 0F,
            -1F, 0F, 0F,
            -1F, 0F, 0F,
            -1F, 0F, 0F,

            // 右面
            1F, 0F, 0F,
            1F, 0F, 0F,
            1F, 0F, 0F,
            1F, 0F, 0F,
            1F, 0F, 0F,
            1F, 0F, 0F,

            // 上面
            0F, 1F, 0F,
            0F, 1F, 0F,
            0F, 1F, 0F,
            0F, 1F, 0F,
            0F, 1F, 0F,
            0F, 1F, 0F,

            // 下面
            0F, -1F, 0F,
            0F, -1F, 0F,
            0F, -1F, 0F,
            0F, -1F, 0F,
            0F, -1F, 0F,
            0F, -1F, 0F
        )
    )
    private val mVertexNormalBuffer: FloatBuffer = mVertexBuffer

    private val mVertexCount = 36

    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mNormalMatrixHandle = 0
    private var mLightPointHandle = 0
    private var mViewPointHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mNormalType: NormalType = NormalType.FACE_NORMAL
    private var mIsAddAmbientLightHandle = 0
    private var mIsAddDiffuseLightHandle = 0
    private var mIsAddSpecularHandle = 0
    private var mLightSourceTypeHandle = 0

    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mMMatrix: GLMatrix = GLMatrix()

    private var mLightPoint = Point(0F, 0F, 0F)
    private var mViewPoint = Point(0F, 0F, 0F)
    private var mShininess = 50F

    private var mIsAddAmbientLight = true
    private var mIsAddDiffuseLight = true
    private var mIsAddSpecularLight = true

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setModelMatrix(matrix: GLMatrix) {
        mMMatrix = matrix
    }

    fun setLightPoint(lightPoint: Point) {
        mLightPoint = lightPoint
    }

    fun setViewPoint(viewPoint: Point) {
        mViewPoint = viewPoint
    }

    fun setShininess(shininess: Float) {
        mShininess = shininess
    }

    fun isAddAmbientLight(value: Boolean) {
        mIsAddAmbientLight = value
    }

    fun isAddDiffuseLight(value: Boolean) {
        mIsAddDiffuseLight = value
    }

    fun isAddSpecularLight(value: Boolean) {
        mIsAddSpecularLight = value
    }

    fun setNormalType(vertexNormal: NormalType) {
        mNormalType = vertexNormal
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/light/normal_type/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/light/normal_type/fragment.glsl")


    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mMMatrixHandle = getUniformLocation("uMMatrix")
        mNormalMatrixHandle = getUniformLocation("uNormalMatrix")
        mLightPointHandle = getUniformLocation("uLightPoint")
        mViewPointHandle = getUniformLocation("uViewPoint")
        mPositionHandle = getAttribLocation("aPosition")
        mNormalHandle = getAttribLocation("aNormal")
        mShininessHandle = getAttribLocation("aShininess")
        mIsAddAmbientLightHandle = getUniformLocation("uIsAddAmbientLight")
        mIsAddDiffuseLightHandle = getUniformLocation("uIsAddDiffuseLight")
        mIsAddSpecularHandle = getUniformLocation("uIsAddSpecularLight")
        mLightSourceTypeHandle = getUniformLocation("uLightSourceType")
    }

    override fun onDraw() {
        GLES20.glUniform3f(mLightPointHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
        GLES20.glUniform3f(mViewPointHandle, mViewPoint.x, mViewPoint.y, mViewPoint.z)
        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
        GLES20.glUniform1i(mIsAddAmbientLightHandle, if (mIsAddAmbientLight) 1 else 0)
        GLES20.glUniform1i(mIsAddDiffuseLightHandle, if (mIsAddDiffuseLight) 1 else 0)
        GLES20.glUniform1i(mIsAddSpecularHandle, if (mIsAddSpecularLight) 1 else 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        when (mNormalType) {
            NormalType.VERTEX_NORMAL -> GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexNormalBuffer)
            NormalType.FACE_NORMAL -> GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mFaceNormalBuffer)
        }
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mNormalHandle)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mMMatrix.matrix, 0)
        GLES20.glUniformMatrix4fv(mNormalMatrixHandle, 1, false, mMMatrix.matrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)

        GLES20.glDisableVertexAttribArray(mNormalHandle)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mMMatrixHandle = 0
        mLightPointHandle = 0
        mViewPointHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mIsAddAmbientLightHandle = 0
        mIsAddDiffuseLightHandle = 0
        mIsAddSpecularHandle = 0
        mLightSourceTypeHandle = 0
        mNormalMatrixHandle = 0
    }
}