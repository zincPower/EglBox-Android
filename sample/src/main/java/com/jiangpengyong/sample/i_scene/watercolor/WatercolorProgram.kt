package com.jiangpengyong.sample.i_scene.watercolor

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.EglBox
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.EglBoxRuntime
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.program.DrawMode
import com.jiangpengyong.eglbox_filter.program.Light
import com.jiangpengyong.eglbox_filter.program.LightSourceType

/**
 * @author jiang peng yong
 * @date 2025/1/13 08:47
 * @email 56002982@qq.com
 * @des 水彩
 */
class WatercolorProgram : GLProgram() {
    private var mTextureColorChartHandle = 0
    private var mMVPMatrixHandle = 0
    private var mModelMatrixHandle = 0
    private var mLightPointHandle = 0
    private var mViewPointHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mIsAddDiffuseLightHandle = 0
    private var mIsAddSpecularLightHandle = 0
    private var mDiffuseLightCoefficientHandle = 0
    private var mSpecularLightCoefficientHandle = 0
    private var mLightSourceTypeHandle = 0

    private var mMVPMatrix = GLMatrix()
    private var mModelMatrix = GLMatrix()

    private var mLightPoint = Point(1F, 1F, 1F)
    private var mViewPoint = Point(0F, 0F, 0F)
    private var mShininess = 50F

    private var mLightSourceType = LightSourceType.PointLight

    private var mIsAddDiffuseLight = true
    private var mIsAddSpecularLight = true

    private var mDiffuseLightCoefficient = Light(0.7F, 0.7F, 0.7F, 1.0F)
    private var mSpecularLightCoefficient = Light(0.6F, 0.6F, 0.6F, 1.0F)

    private var mDrawMode: DrawMode? = null

    private var mModelData: ModelData? = null

    private var mColorChartTexture: GLTexture? = null

    fun setColorChartTexture(texture: GLTexture): WatercolorProgram {
        mColorChartTexture = texture
        return this
    }

    fun setMVPMatrix(matrix: GLMatrix): WatercolorProgram {
        mMVPMatrix = matrix
        return this
    }

    fun setModelMatrix(matrix: GLMatrix): WatercolorProgram {
        mModelMatrix = matrix
        return this
    }

    fun setLightPoint(lightPoint: Point): WatercolorProgram {
        mLightPoint = lightPoint
        return this
    }

    fun setViewPoint(viewPoint: Point): WatercolorProgram {
        mViewPoint = viewPoint
        return this
    }

    fun setShininess(shininess: Float): WatercolorProgram {
        mShininess = shininess
        return this
    }

    fun setIsAddDiffuseLight(value: Boolean): WatercolorProgram {
        mIsAddDiffuseLight = value
        return this
    }

    fun setIsAddSpecularLight(value: Boolean): WatercolorProgram {
        mIsAddSpecularLight = value
        return this
    }

    fun setDiffuseLightCoefficient(coefficient: Light): WatercolorProgram {
        mDiffuseLightCoefficient = coefficient
        return this
    }

    fun setSpecularLightCoefficient(coefficient: Light): WatercolorProgram {
        mSpecularLightCoefficient = coefficient
        return this
    }

    fun setLightSourceType(lightSourceType: LightSourceType): WatercolorProgram {
        mLightSourceType = lightSourceType
        return this
    }

    fun setDrawMode(drawMode: DrawMode): WatercolorProgram {
        mDrawMode = drawMode
        return this
    }

    fun setModelData(modelData: ModelData): WatercolorProgram {
        mModelData = modelData
        return this
    }

    override fun onInit() {
        mTextureColorChartHandle = getUniformLocation("sTextureColorChart")
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mModelMatrixHandle = getUniformLocation("uModelMatrix")
        mLightPointHandle = getUniformLocation("uLightPoint")
        mViewPointHandle = getUniformLocation("uViewPoint")
        mPositionHandle = getAttribLocation("aPosition")
        mNormalHandle = getAttribLocation("aNormal")
        mShininessHandle = getUniformLocation("uShininess")
        mIsAddDiffuseLightHandle = getUniformLocation("uIsAddDiffuseLight")
        mIsAddSpecularLightHandle = getUniformLocation("uIsAddSpecularLight")
        mDiffuseLightCoefficientHandle = getUniformLocation("diffuseLightCoefficient")
        mSpecularLightCoefficientHandle = getUniformLocation("specularLightCoefficient")
        mLightSourceTypeHandle = getUniformLocation("uLightSourceType")
    }

    override fun onDraw() {
        val colorChartTexture = mColorChartTexture ?: return
        val modelData = mModelData ?: return
        colorChartTexture.bind {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            GLES20.glFrontFace(modelData.frontFace.value)

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
            GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix.matrix, 0)
            GLES20.glUniform3f(mLightPointHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
            GLES20.glUniform3f(mViewPointHandle, mViewPoint.x, mViewPoint.y, mViewPoint.z)
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, modelData.vertexBuffer)
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, modelData.normalBuffer ?: return@bind)
            GLES20.glEnableVertexAttribArray(mNormalHandle)
            GLES20.glUniform1f(mShininessHandle, mShininess)
            GLES20.glUniform1i(mIsAddDiffuseLightHandle, if (mIsAddDiffuseLight) 1 else 0)
            GLES20.glUniform1i(mIsAddSpecularLightHandle, if (mIsAddSpecularLight) 1 else 0)
            mDiffuseLightCoefficient.let {
                GLES20.glUniform4f(mDiffuseLightCoefficientHandle, it.red, it.green, it.blue, it.alpha)
            }
            mSpecularLightCoefficient.let {
                GLES20.glUniform4f(mSpecularLightCoefficientHandle, it.red, it.green, it.blue, it.alpha)
            }
            GLES20.glDrawArrays(mDrawMode?.value ?: GLES20.GL_TRIANGLES, 0, modelData.count)
            GLES20.glDisableVertexAttribArray(mPositionHandle)
            GLES20.glDisableVertexAttribArray(mNormalHandle)

            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }

        EglBox.checkError(TAG)
    }

    override fun onRelease() {
        mTextureColorChartHandle = 0
        mMVPMatrixHandle = 0
        mModelMatrixHandle = 0
        mLightPointHandle = 0
        mViewPointHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mIsAddDiffuseLightHandle = 0
        mIsAddSpecularLightHandle = 0
        mDiffuseLightCoefficientHandle = 0
        mSpecularLightCoefficientHandle = 0
        mLightSourceTypeHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/watercolor/vertex.glsl"
    )

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/watercolor/fragment.glsl"
    )

    companion object {
        const val TAG = "WatercolorProgram"
    }
}