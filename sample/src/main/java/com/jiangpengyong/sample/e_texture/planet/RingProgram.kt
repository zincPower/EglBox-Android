package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.sample.App

/**
 * @author jiang peng yong
 * @date 2024/8/12 22:04
 * @email 56002982@qq.com
 * @des 环形
 */
class RingProgram(
    majorRadius: Float = 1.5F,     // 主半径，圆环中心到圆环切面中心距离
    minorRadius: Float = 0.5F,     // 子半径，圆环切面半径
    majorSegment: Int = 36,      // 裁剪主圈份数，数值越大越光滑但顶点数量越多，数值越小则棱角明显顶点数量少
    minorSegment: Int = 36,
) : GLProgram() {
    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mLightPointHandle = 0
    private var mViewPointHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mLightSourceTypeHandle = 0
    private var mTextureHandle = 0
    private var mTextureCoordHandle = 0

    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mModelMatrix: GLMatrix = GLMatrix()

    private var mLightPoint = Point(0F, 0F, 0F)
    private var mViewPoint = Point(0F, 0F, 0F)
    private var mShininess = 50F

    private var mTexture: GLTexture? = null

    private var mModelData: ModelData = ModelCreator.createRing(majorRadius, minorRadius, majorSegment, minorSegment)

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setModelMatrix(matrix: GLMatrix) {
        mModelMatrix = matrix
    }

    fun setLightPoint(lightPoint: Point) {
        mLightPoint = lightPoint
    }

    fun setViewPoint(cameraPosition: Point) {
        mViewPoint = cameraPosition
    }

    fun setShininess(shininess: Float) {
        mShininess = shininess
    }

    fun setTexture(texture: GLTexture) {
        mTexture = texture
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mMMatrixHandle = getUniformLocation("uMMatrix")
        mLightPointHandle = getUniformLocation("uLightPoint")
        mViewPointHandle = getUniformLocation("uViewPoint")
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
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mModelMatrix.matrix, 0)
        GLES20.glUniform3f(mLightPointHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
        GLES20.glUniform3f(mViewPointHandle, mViewPoint.x, mViewPoint.y, mViewPoint.z)
        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.vertexBuffer)
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.normalBuffer ?: return)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mModelData.textureBuffer ?: return)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mModelData.count)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
        mTexture?.unbind()
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mMMatrixHandle = 0
        mLightPointHandle = 0
        mViewPointHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mLightSourceTypeHandle = 0
        mTextureHandle = 0
        mTextureCoordHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/celestial_body/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/celestial_body/fragment.glsl")
}
