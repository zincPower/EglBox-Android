package com.jiangpengyong.sample.i_scene.outline

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.EglBoxRuntime
import java.nio.FloatBuffer

/**
 * @author jiang peng yong
 * @date 2025/1/15 08:24
 * @email 56002982@qq.com
 * @des 描边
 */
class OutlineProgram : GLProgram() {
    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0

    private var mVertexBuffer: FloatBuffer? = null
    private var mNormalBuffer: FloatBuffer? = null
    private var mVertexCount = 0

    private var mMVPMatrix = GLMatrix()

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setData(
        vertexBuffer: FloatBuffer,
        normalBuffer: FloatBuffer,
        vertexCount: Int,
    ) {
        mVertexBuffer = vertexBuffer
        mNormalBuffer = normalBuffer
        mVertexCount = vertexCount
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
        mNormalHandle = getAttribLocation("aNormal")
    }

    override fun onDraw() {
        val vertexBuffer = mVertexBuffer ?: return
        val normalBuffer = mNormalBuffer ?: return

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, normalBuffer)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
    }

    override fun getVertexShaderSource() = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/outline/vertex.glsl",
    )

    override fun getFragmentShaderSource() = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/outline/fragment.glsl",
    )
}