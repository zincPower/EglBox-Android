package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.sample.App

/**
 * @author jiang peng yong
 * @date 2024/11/25 08:32
 * @email 56002982@qq.com
 * @des 轨道
 */
class OrbitProgram : GLProgram() {
    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0

    private var mMVPMatrix = GLMatrix()

    private var mModelData = ModelCreator.createCircle()

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.vertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, mModelData.count)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
    }

    override fun getVertexShaderSource(): String = GLShaderExt.loadFromAssetsFile(App.context.resources, "glsl/texture/orbit/vertex.glsl")

    override fun getFragmentShaderSource(): String = GLShaderExt.loadFromAssetsFile(App.context.resources, "glsl/texture/orbit/fragment.glsl")
}
