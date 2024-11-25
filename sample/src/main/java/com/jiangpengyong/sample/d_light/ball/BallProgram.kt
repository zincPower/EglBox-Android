package com.jiangpengyong.sample.d_light.ball

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.sample.App

/**
 * @author jiang peng yong
 * @date 2024/6/19 10:08
 * @email 56002982@qq.com
 * @des 绘制球
 */
class TrigonometricBallProgram : GLProgram() {
    enum class DrawMode(val value: Int) {
        Point(1),
        Line(2),
        Face(3)
    }

    private var mAngleSpan = 10
    private var mRadius = 1F

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0

    private var mMatrix: GLMatrix = GLMatrix()
    private var mDrawMode: DrawMode = DrawMode.Face

    private var mModelData: ModelData = ModelCreator.createBall(mAngleSpan, mRadius)

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
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
        mPositionHandle = getAttribLocation("aPosition")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mModelData.vertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        when (mDrawMode) {
            DrawMode.Point -> GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mModelData.count)
            DrawMode.Line -> GLES20.glDrawArrays(GLES20.GL_LINES, 0, mModelData.count)
            DrawMode.Face -> GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mModelData.count)
        }
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/ball/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/ball/fragment.glsl")
}
