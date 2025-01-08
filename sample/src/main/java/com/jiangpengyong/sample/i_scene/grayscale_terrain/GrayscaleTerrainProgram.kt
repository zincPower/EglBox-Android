package com.jiangpengyong.sample.i_scene.grayscale_terrain

import android.opengl.GLES20
import android.util.Range
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
 * @date 2025/1/5 18:29
 * @email 56002982@qq.com
 * @des 灰度图程序
 */
open class GrayscaleTerrainProgram : GLProgram() {
    private var mTextureLandHandle = 0
    private var mTextureMountainHandle = 0
    private var mTextureSnowHandle = 0
    private var mMountainBoundaryStartHandle = 0
    private var mMountainBoundaryEndHandle = 0
    private var mSnowBoundaryStartHandle = 0
    private var mSnowBoundaryEndHandle = 0
    private var mMVPMatrixHandle = 0
    private var mModelMatrixHandle = 0
    private var mLightPointHandle = 0
    private var mViewPointHandle = 0
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mIsAddAmbientLightHandle = 0
    private var mIsAddDiffuseLightHandle = 0
    private var mIsAddSpecularLightHandle = 0
    private var mAmbientLightCoefficientHandle = 0
    private var mDiffuseLightCoefficientHandle = 0
    private var mSpecularLightCoefficientHandle = 0
    private var mLightSourceTypeHandle = 0

    private var mMVPMatrix = GLMatrix()
    private var mModelMatrix = GLMatrix()

    private var mLightPoint = Point(1F, 1F, 1F)
    private var mViewPoint = Point(0F, 0F, 0F)
    private var mShininess = 50F

    private var mLightSourceType = LightSourceType.PointLight

    private var mIsAddAmbientLight = true
    private var mIsAddDiffuseLight = true
    private var mIsAddSpecularLight = true

    private var mAmbientLightCoefficient = Light(0.3F, 0.3F, 0.3F, 1.0F)
    private var mDiffuseLightCoefficient = Light(0.7F, 0.7F, 0.7F, 1.0F)
    private var mSpecularLightCoefficient = Light(0.6F, 0.6F, 0.6F, 1.0F)

    private var mDrawMode: DrawMode? = null

    private var mModelData: ModelData? = null

    private var mMountainBoundaryRange: Range<Float> = Range(10F, 20F)
    private var mSnowBoundaryRange: Range<Float> = Range(30F, 32.5F)
    private var mLandTexture: GLTexture? = null
    private var mMountainTexture: GLTexture? = null
    private var mSnowTexture: GLTexture? = null

    fun setMountainBoundaryRange(range: Range<Float>): GrayscaleTerrainProgram {
        mMountainBoundaryRange = range
        return this
    }

    fun setSnowBoundaryRange(range: Range<Float>): GrayscaleTerrainProgram {
        mSnowBoundaryRange = range
        return this
    }

    fun setLandTexture(texture: GLTexture): GrayscaleTerrainProgram {
        mLandTexture = texture
        return this
    }

    fun setMountainTexture(texture: GLTexture): GrayscaleTerrainProgram {
        mMountainTexture = texture
        return this
    }

    fun setSnowTexture(texture: GLTexture): GrayscaleTerrainProgram {
        mSnowTexture = texture
        return this
    }

    fun setMVPMatrix(matrix: GLMatrix): GrayscaleTerrainProgram {
        mMVPMatrix = matrix
        return this
    }

    fun setModelMatrix(matrix: GLMatrix): GrayscaleTerrainProgram {
        mModelMatrix = matrix
        return this
    }

    fun setLightPoint(lightPoint: Point): GrayscaleTerrainProgram {
        mLightPoint = lightPoint
        return this
    }

    fun setViewPoint(viewPoint: Point): GrayscaleTerrainProgram {
        mViewPoint = viewPoint
        return this
    }

    fun setShininess(shininess: Float): GrayscaleTerrainProgram {
        mShininess = shininess
        return this
    }

    fun setIsAddAmbientLight(value: Boolean): GrayscaleTerrainProgram {
        mIsAddAmbientLight = value
        return this
    }

    fun setIsAddDiffuseLight(value: Boolean): GrayscaleTerrainProgram {
        mIsAddDiffuseLight = value
        return this
    }

    fun setIsAddSpecularLight(value: Boolean): GrayscaleTerrainProgram {
        mIsAddSpecularLight = value
        return this
    }

    fun setAmbientLightCoefficient(coefficient: Light): GrayscaleTerrainProgram {
        mAmbientLightCoefficient = coefficient
        return this
    }

    fun setDiffuseLightCoefficient(coefficient: Light): GrayscaleTerrainProgram {
        mDiffuseLightCoefficient = coefficient
        return this
    }

    fun setSpecularLightCoefficient(coefficient: Light): GrayscaleTerrainProgram {
        mSpecularLightCoefficient = coefficient
        return this
    }

    fun setLightSourceType(lightSourceType: LightSourceType): GrayscaleTerrainProgram {
        mLightSourceType = lightSourceType
        return this
    }

    fun setDrawMode(drawMode: DrawMode): GrayscaleTerrainProgram {
        mDrawMode = drawMode
        return this
    }

    fun setModelData(modelData: ModelData): GrayscaleTerrainProgram {
        mModelData = modelData
        return this
    }

    override fun onInit() {
        mTextureLandHandle = getUniformLocation("sTextureLand")
        mTextureMountainHandle = getUniformLocation("sTextureMountain")
        mTextureSnowHandle = getUniformLocation("sTextureSnow")
        mMountainBoundaryStartHandle = getUniformLocation("uMountainBoundaryStart")
        mMountainBoundaryEndHandle = getUniformLocation("uMountainBoundaryEnd")
        mSnowBoundaryStartHandle = getUniformLocation("uSnowBoundaryStart")
        mSnowBoundaryEndHandle = getUniformLocation("uSnowBoundaryEnd")
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mModelMatrixHandle = getUniformLocation("uModelMatrix")
        mLightPointHandle = getUniformLocation("uLightPoint")
        mViewPointHandle = getUniformLocation("uViewPoint")
        mPositionHandle = getAttribLocation("aPosition")
        mTextureCoordHandle = getAttribLocation("aTextureCoord")
        mNormalHandle = getAttribLocation("aNormal")
        mShininessHandle = getUniformLocation("uShininess")
        mIsAddAmbientLightHandle = getUniformLocation("uIsAddAmbientLight")
        mIsAddDiffuseLightHandle = getUniformLocation("uIsAddDiffuseLight")
        mIsAddSpecularLightHandle = getUniformLocation("uIsAddSpecularLight")
        mAmbientLightCoefficientHandle = getUniformLocation("ambientLightCoefficient")
        mDiffuseLightCoefficientHandle = getUniformLocation("diffuseLightCoefficient")
        mSpecularLightCoefficientHandle = getUniformLocation("specularLightCoefficient")
        mLightSourceTypeHandle = getUniformLocation("uLightSourceType")
    }

    override fun onDraw() {
        val landTexture = mLandTexture ?: return
        val mountainTexture = mMountainTexture ?: return
        val snowTexture = mSnowTexture ?: return
        val modelData = mModelData ?: return

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glFrontFace(modelData.frontFace.value)

        landTexture.bind(textureUnit = GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(mTextureLandHandle, 0)
        mountainTexture.bind(textureUnit = GLES20.GL_TEXTURE1)
        GLES20.glUniform1i(mTextureMountainHandle, 1)
        snowTexture.bind(textureUnit = GLES20.GL_TEXTURE2)
        GLES20.glUniform1i(mTextureSnowHandle, 2)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix.matrix, 0)
        GLES20.glUniform3f(mLightPointHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
        GLES20.glUniform3f(mViewPointHandle, mViewPoint.x, mViewPoint.y, mViewPoint.z)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, modelData.vertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, modelData.textureStep, GLES20.GL_FLOAT, false, modelData.textureStep * 4, modelData.textureBuffer)
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
        GLES20.glUniform1f(mMountainBoundaryStartHandle, mMountainBoundaryRange.lower)
        GLES20.glUniform1f(mMountainBoundaryEndHandle, mMountainBoundaryRange.upper)
        GLES20.glUniform1f(mSnowBoundaryStartHandle, mSnowBoundaryRange.lower)
        GLES20.glUniform1f(mSnowBoundaryEndHandle, mSnowBoundaryRange.upper)
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, modelData.normalBuffer ?: return)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLES20.glUniform1f(mShininessHandle, mShininess)
        GLES20.glUniform1i(mIsAddAmbientLightHandle, if (mIsAddAmbientLight) 1 else 0)
        GLES20.glUniform1i(mIsAddDiffuseLightHandle, if (mIsAddDiffuseLight) 1 else 0)
        GLES20.glUniform1i(mIsAddSpecularLightHandle, if (mIsAddSpecularLight) 1 else 0)
        mAmbientLightCoefficient.let {
            GLES20.glUniform4f(mAmbientLightCoefficientHandle, it.red, it.green, it.blue, it.alpha)
        }
        mDiffuseLightCoefficient.let {
            GLES20.glUniform4f(mDiffuseLightCoefficientHandle, it.red, it.green, it.blue, it.alpha)
        }
        mSpecularLightCoefficient.let {
            GLES20.glUniform4f(mSpecularLightCoefficientHandle, it.red, it.green, it.blue, it.alpha)
        }
        GLES20.glDrawArrays(mDrawMode?.value ?: GLES20.GL_TRIANGLES, 0, modelData.count)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)

        landTexture.unbind()
        mountainTexture.unbind()

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        EglBox.checkError(TAG)
    }

    override fun onRelease() {
        mTextureLandHandle = 0
        mTextureMountainHandle = 0
        mTextureSnowHandle = 0
        mMountainBoundaryStartHandle = 0
        mMountainBoundaryEndHandle = 0
        mSnowBoundaryStartHandle = 0
        mSnowBoundaryEndHandle = 0
        mMVPMatrixHandle = 0
        mModelMatrixHandle = 0
        mLightPointHandle = 0
        mViewPointHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mIsAddAmbientLightHandle = 0
        mIsAddDiffuseLightHandle = 0
        mIsAddSpecularLightHandle = 0
        mAmbientLightCoefficientHandle = 0
        mDiffuseLightCoefficientHandle = 0
        mSpecularLightCoefficientHandle = 0
        mLightSourceTypeHandle = 0
        mTextureCoordHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/grayscale_terrain/vertex.glsl"
    )

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/grayscale_terrain/fragment.glsl"
    )

    companion object {
        const val TAG = "GrayscaleTerrainProgram"
    }
}