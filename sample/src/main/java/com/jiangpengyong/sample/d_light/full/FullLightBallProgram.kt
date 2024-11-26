//package com.jiangpengyong.sample.d_light.full
//
//import android.opengl.GLES20
//import com.jiangpengyong.eglbox_core.gles.GLProgram
//import com.jiangpengyong.eglbox_core.utils.GLMatrix
//import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
//import com.jiangpengyong.eglbox_filter.model.ModelCreator
//import com.jiangpengyong.sample.App
//
///**
// * @author jiang peng yong
// * @date 2024/6/19 10:08
// * @email 56002982@qq.com
// * @des 绘制球 —— 环境光、散射光、镜面光
// */
//class FullLightBallProgram : GLProgram() {
//    private var mAngleSpan = 10
//    private var mRadius = 1F
//
//    private var mMVPMatrixHandle = 0
//    private var mMMatrixHandle = 0
//    private var mLightPointHandle = 0
//    private var mViewPointHandle = 0
//    private var mPositionHandle = 0
//    private var mNormalHandle = 0
//    private var mShininessHandle = 0
//    private var mIsAddAmbientLightHandle = 0
//    private var mIsAddDiffuseLightHandle = 0
//    private var mIsAddSpecularHandle = 0
//
//    private var mMVPMatrix = GLMatrix()
//    private var mModelMatrix = GLMatrix()
//
//    private var mLightPoint = FloatArray(3)
//    private var mViewPoint = FloatArray(3)
//    private var mShininess = 50F
//
//    private var mIsAddAmbientLight = true
//    private var mIsAddDiffuseLight = true
//    private var mIsAddSpecularLight = true
//
//    private var mModelData = ModelCreator.createBall(mAngleSpan, mRadius)
//
//    fun setMVPMatrix(matrix: GLMatrix) {
//        mMVPMatrix = matrix
//    }
//
//    fun setMMatrix(matrix: GLMatrix) {
//        mModelMatrix = matrix
//    }
//
//    fun setLightPoint(LightPoint: FloatArray) {
//        mLightPoint = LightPoint
//    }
//
//    fun setViewPoint(ViewPoint: FloatArray) {
//        mViewPoint = ViewPoint
//    }
//
//    fun setShininess(shininess: Float) {
//        mShininess = shininess
//    }
//
//    fun isAddAmbientLight(value: Boolean) {
//        mIsAddAmbientLight = value
//    }
//
//    fun isAddDiffuseLight(value: Boolean) {
//        mIsAddDiffuseLight = value
//    }
//
//    fun isAddSpecularLight(value: Boolean) {
//        mIsAddSpecularLight = value
//    }
//
//    fun setAngleSpan(angleSpan: Int) {
//        mAngleSpan = angleSpan
//        mModelData = ModelCreator.createBall(mAngleSpan, mRadius)
//    }
//
//    override fun onInit() {
//        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
//        mMMatrixHandle = getUniformLocation("uMMatrix")
//        mLightPointHandle = getUniformLocation("uLightPoint")
//        mViewPointHandle = getUniformLocation("uViewPoint")
//        mPositionHandle = getAttribLocation("aPosition")
//        mNormalHandle = getAttribLocation("aNormal")
//        mShininessHandle = getAttribLocation("aShininess")
//        mIsAddAmbientLightHandle = getUniformLocation("uIsAddAmbientLight")
//        mIsAddDiffuseLightHandle = getUniformLocation("uIsAddDiffuseLight")
//        mIsAddSpecularHandle = getUniformLocation("uIsAddSpecularLight")
//    }
//
//    override fun onDraw() {
//        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
//        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mModelMatrix.matrix, 0)
//        GLES20.glUniform3f(mLightPointHandle, mLightPoint[0], mLightPoint[1], mLightPoint[2])
//        GLES20.glUniform3f(mViewPointHandle, mViewPoint[0], mViewPoint[1], mViewPoint[2])
//        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
//        GLES20.glUniform1i(mIsAddAmbientLightHandle, if (mIsAddAmbientLight) 1 else 0)
//        GLES20.glUniform1i(mIsAddDiffuseLightHandle, if (mIsAddDiffuseLight) 1 else 0)
//        GLES20.glUniform1i(mIsAddSpecularHandle, if (mIsAddSpecularLight) 1 else 0)
//        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.vertexBuffer)
//        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.normalBuffer)
//        GLES20.glEnableVertexAttribArray(mPositionHandle)
//        GLES20.glEnableVertexAttribArray(mNormalHandle)
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mModelData.count)
//        GLES20.glDisableVertexAttribArray(mPositionHandle)
//        GLES20.glDisableVertexAttribArray(mNormalHandle)
//    }
//
//    override fun onRelease() {
//        mMVPMatrixHandle = 0
//        mMMatrixHandle = 0
//        mLightPointHandle = 0
//        mViewPointHandle = 0
//        mPositionHandle = 0
//        mNormalHandle = 0
//        mShininessHandle = 0
//        mIsAddAmbientLightHandle = 0
//        mIsAddDiffuseLightHandle = 0
//        mIsAddSpecularHandle = 0
//    }
//
//    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/light/full/vertex.glsl")
//
//    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/light/full/fragment.glsl")
//}
