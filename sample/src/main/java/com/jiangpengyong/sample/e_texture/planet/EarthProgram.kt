package com.jiangpengyong.sample.e_texture.planet

import android.graphics.BitmapFactory
import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.sample.App
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/8/12 22:04
 * @email 56002982@qq.com
 * @des 地球
 */
class EarthProgram : GLProgram() {
    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mLightPointHandle = 0
    private var mViewPointHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mLightSourceTypeHandle = 0
    private var mDayTextureHandle = 0
    private var mNightTextureHandle = 0
    private var mTextureCoordHandle = 0

    private var mMVPMatrix = GLMatrix()
    private var mModelMatrix = GLMatrix()

    private var mLightPoint = Point(0F, 0F, 0F)
    private var mViewPoint = Point(0F, 0F, 0F)
    private var mShininess = 50F

    private var mDayTexture: GLTexture? = null
    private var mNightTexture = GLTexture()

    private var mModelData = ModelCreator.createBall()

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setModelMatrix(matrix: GLMatrix) {
        mModelMatrix = matrix
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

    fun setDayTexture(texture: GLTexture) {
        mDayTexture = texture
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
        mDayTextureHandle = getUniformLocation("sTextureDay")
        mNightTextureHandle = getUniformLocation("sTextureNight")

        mNightTexture.init()
        BitmapFactory.decodeFile(File(App.context.filesDir, "images/celestial_body/2k_earth_nightmap.jpg").absolutePath).let { bitmap ->
            mNightTexture.setData(bitmap)
            bitmap.recycle()
        }
    }

    override fun onDraw() {
        val dayTexture = mDayTexture ?: return
        mModelData.frontFace.use()
        dayTexture.bind(textureUnit = GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(mDayTextureHandle, 0)
        mNightTexture.bind(textureUnit = GLES20.GL_TEXTURE1)
        GLES20.glUniform1i(mNightTextureHandle, 1)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mModelMatrix.matrix, 0)
        GLES20.glUniform3f(mLightPointHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
        GLES20.glUniform3f(mViewPointHandle, mViewPoint.x, mViewPoint.y, mViewPoint.z)
        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.vertexBuffer)
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.normalBuffer)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, mModelData.textureStep, GLES20.GL_FLOAT, false, mModelData.textureStep * 4, mModelData.textureBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mModelData.count)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
        dayTexture.unbind()
        mNightTexture.unbind()
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
        mDayTextureHandle = 0
        mNightTextureHandle = 0
        mTextureCoordHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/earth/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/earth/fragment.glsl")
}
