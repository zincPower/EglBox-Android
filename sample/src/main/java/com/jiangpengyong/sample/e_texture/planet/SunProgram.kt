package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.sample.App

/**
 * @author jiang peng yong
 * @date 2024/8/14 21:41
 * @email 56002982@qq.com
 * @des 太阳
 */
class SunProgram : GLProgram() {
    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0
    private var mTextureHandle = 0

    private var mMVPMatrix = GLMatrix()

    private var mTexture: GLTexture? = null

    private var mModelData = ModelCreator.createBall()

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setTexture(texture: GLTexture) {
        mTexture = texture
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
        mTextureCoordHandle = getAttribLocation("aTextureCoord")
        mTextureHandle = getUniformLocation("sTexture")
    }

    override fun onDraw() {
        val texture = mTexture ?: return
        texture.bind{
            GLES20.glFrontFace(mModelData.frontFace.value)
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.vertexBuffer)
            GLES20.glVertexAttribPointer(mTextureCoordHandle, mModelData.textureStep, GLES20.GL_FLOAT, false, mModelData.textureStep * 4, mModelData.textureBuffer)
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mModelData.count)
            GLES20.glDisableVertexAttribArray(mPositionHandle)
            GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
        }
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
        mTextureHandle = 0
        mTextureCoordHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/sun/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/sun/fragment.glsl")
}
