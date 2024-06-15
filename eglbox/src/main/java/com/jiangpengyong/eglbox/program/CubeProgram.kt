package com.jiangpengyong.eglbox.program

import android.opengl.GLES20
import com.jiangpengyong.eglbox.gles.GLProgram
import com.jiangpengyong.eglbox.utils.GLMatrix
import com.jiangpengyong.eglbox.utils.ModelMatrix
import com.jiangpengyong.eglbox.utils.allocateFloatBuffer
import java.nio.FloatBuffer

/**
 * @author jiang peng yong
 * @date 2024/6/15 19:22
 * @email 56002982@qq.com
 * @des 绘制立方体
 */
class CubeProgram : GLProgram() {
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
             *  1-----------0
             *  ᐱ +z        │
             *  │           │
             *  │    原点    │
             *  │           │
             *  │           │
             *  2---------->3
             *             +x
             */
            halfSideLength, halfSideLength, halfSideLength,     // 0
            -halfSideLength, halfSideLength, halfSideLength,    // 1
            -halfSideLength, halfSideLength, -halfSideLength,   // 2
            halfSideLength, halfSideLength, halfSideLength,     // 0
            -halfSideLength, halfSideLength, -halfSideLength,   // 2
            halfSideLength, halfSideLength, -halfSideLength,    // 3
            /**
             *  上面 向量向屏幕外
             *
             *  3-----------0
             *  ᐱ +z        │
             *  │           │
             *  │    原点    │
             *  │           │
             *  │           │
             *  2---------->1
             *             +x
             */
            halfSideLength, -halfSideLength, halfSideLength,    // 0
            halfSideLength, -halfSideLength, -halfSideLength,   // 1
            -halfSideLength, -halfSideLength, -halfSideLength,  // 2
            halfSideLength, -halfSideLength, halfSideLength,    // 0
            -halfSideLength, -halfSideLength, -halfSideLength,  // 2
            -halfSideLength, -halfSideLength, halfSideLength,   // 3
        )
    )
    private val mColorBuffer: FloatBuffer

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0

    private val mVertexCount = 6 * 6

    private var mMatrix = GLMatrix()

    init {
        val colors = FloatArray(mVertexCount * 4)
        for (i in 0 until 6) {
            for (j in 0 until 6) {
                colors[i * 24 + j * 4 + 0] = (10F + 200 / 6 * i) / 255F
                colors[i * 24 + j * 4 + 1] = (100F + 90 / 6 * i) / 255F
                colors[i * 24 + j * 4 + 2] = (50F + 150 / 6 * i) / 255F
                colors[i * 24 + j * 4 + 3] = 0F
            }
        }
        mColorBuffer = allocateFloatBuffer(colors)
    }

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
        mColorHandle = getAttribLocation("aColor")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
    }

    override fun onRelease() {
        mVertexBuffer.clear()
        mColorBuffer.clear()
        mMVPMatrixHandle = 0
        mPositionHandle = 0
        mColorHandle = 0
    }

    override fun getVertexShaderSource(): String = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        in vec3 aPosition;
        in vec4 aColor;
        out vec4 vColor;
        void main() {
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            vColor = aColor;
        }
    """.trimIndent()

    override fun getFragmentShaderSource(): String = """
        #version 300 es
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        void main() {
            fragColor = vColor;
        }
    """.trimIndent()
}