package com.jiangpengyong.sample.d_light.diffuse

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.sample.App

/**
 * @author jiang peng yong
 * @date 2024/6/19 10:08
 * @email 56002982@qq.com
 * @des 绘制球 —— 散射光
 */
class DiffuseLightBallProgram : GLProgram() {
    enum class DrawMode(val value: Int) {
        Point(1),
        Line(2),
        Face(3)
    }

    private var mAngleSpan = 10
    private var mRadius = 1F

    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mLightPositionHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0

    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mMMatrix: GLMatrix = GLMatrix()
    private var mDrawMode: DrawMode = DrawMode.Face

    private var mLightPoint = Point(0F, 0F, 0F)

    private var mModelData: ModelData = ModelCreator.createBall(mAngleSpan, mRadius)

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setMMatrix(matrix: GLMatrix) {
        mMMatrix = matrix
    }

    fun setLightPoint(lightPoint: Point) {
        mLightPoint = lightPoint
    }

    fun setDrawMode(mode: DrawMode) {
        mDrawMode = mode
    }

    fun setAngleSpan(angleSpan: Int) {
        mAngleSpan = angleSpan
        mModelData = ModelCreator.createBall(mAngleSpan, mRadius)
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mMMatrixHandle = getUniformLocation("uMMatrix")
        mLightPositionHandle = getUniformLocation("uLightPosition")
        mPositionHandle = getAttribLocation("aPosition")
        mNormalHandle = getAttribLocation("aNormal")
    }

    override fun onDraw() {
        if (!mModelData.hasNormal) return

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        // 模型矩阵
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mMMatrix.matrix, 0)
        GLES20.glUniform3f(mLightPositionHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.vertexBuffer)
        // 法向量
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.normalBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        when (mDrawMode) {
            DrawMode.Point -> GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mModelData.count)
            DrawMode.Line -> GLES20.glDrawArrays(GLES20.GL_LINES, 0, mModelData.count)
            DrawMode.Face -> GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mModelData.count)
        }
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mMMatrixHandle = 0
        mLightPositionHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/light/diffuse/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/light/diffuse/fragment.glsl")
}
