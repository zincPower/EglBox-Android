package com.jiangpengyong.eglbox_filter.program

import android.opengl.GLES20
import android.util.Log
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.EglBoxRuntime
import com.jiangpengyong.eglbox_filter.model.ModelData

/**
 * @author jiang peng yong
 * @date 2024/12/6 21:17
 * @email 56002982@qq.com
 * @des 光效 Program
 */
open class LightProgram(
    var modelData: ModelData,
    val lightCalculateType: LightCalculateType,
) : GLProgram() {
    enum class TextureType { Color, Texture }
    enum class LightCalculateType { Vertex, Fragment }
    enum class LightSourceType { PointLight, DirectionalLight }
    enum class DrawMode(val value: Int) {
        Triangles(GLES20.GL_TRIANGLES),
        TriangleFan(GLES20.GL_TRIANGLE_FAN),
        TriangleStrip(GLES20.GL_TRIANGLE_STRIP),
        Lines(GLES20.GL_LINES),
        LineStrip(GLES20.GL_LINE_STRIP),
        LineLoop(GLES20.GL_LINE_LOOP),
    }

    data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float)
    data class Light(val red: Float, val green: Float, val blue: Float, val alpha: Float)

    private var mMVPMatrixHandle = 0
    private var mModelMatrixHandle = 0
    private var mLightPointHandle = 0
    private var mViewPointHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mIsAddAmbientLightHandle = 0
    private var mIsAddDiffuseLightHandle = 0
    private var mIsAddSpecularLightHandle = 0
    private var mAmbientLightCoefficientHandle = 0
    private var mDiffuseLightCoefficientHandle = 0
    private var mSpecularLightCoefficientHandle = 0
    private var mLightSourceTypeHandle = 0
    private var mTextureHandle = 0
    private var mTextureCoordHandle = 0
    private var mColorHandle = 0
    private var mIsUseTextureHandle = 0

    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mModelMatrix: GLMatrix = GLMatrix()

    private var mLightPoint = Point(0F, 0F, 0F)
    private var mViewPoint = Point(0F, 0F, 0F)
    private var mShininess = 50F

    private var mTexture: GLTexture? = null
    private var mColor = Color(1F, 1F, 1F, 1F)
    private var mTextureType = TextureType.Color

    private var mLightSourceType = LightSourceType.PointLight

    private var mIsAddAmbientLight = true
    private var mIsAddDiffuseLight = true
    private var mIsAddSpecularLight = true

    private var mAmbientLightCoefficient = Light(0.3F, 0.3F, 0.3F, 1.0F)
    private var mDiffuseLightCoefficient = Light(0.7F, 0.7F, 0.7F, 1.0F)
    private var mSpecularLightCoefficient = Light(0.6F, 0.6F, 0.6F, 1.0F)

    private var mDrawMode: DrawMode? = null

    fun setMVPMatrix(matrix: GLMatrix): LightProgram {
        mMVPMatrix = matrix
        return this
    }

    fun setModelMatrix(matrix: GLMatrix): LightProgram {
        mModelMatrix = matrix
        return this
    }

    fun setLightPoint(lightPoint: Point): LightProgram {
        mLightPoint = lightPoint
        return this
    }

    fun setViewPoint(viewPoint: Point): LightProgram {
        mViewPoint = viewPoint
        return this
    }

    fun setShininess(shininess: Float): LightProgram {
        mShininess = shininess
        return this
    }

    fun setIsAddAmbientLight(value: Boolean): LightProgram {
        mIsAddAmbientLight = value
        return this
    }

    fun setIsAddDiffuseLight(value: Boolean): LightProgram {
        mIsAddDiffuseLight = value
        return this
    }

    fun setIsAddSpecularLight(value: Boolean): LightProgram {
        mIsAddSpecularLight = value
        return this
    }

    fun setAmbientLightCoefficient(coefficient: Light): LightProgram {
        mAmbientLightCoefficient = coefficient
        return this
    }

    fun setDiffuseLightCoefficient(coefficient: Light): LightProgram {
        mDiffuseLightCoefficient = coefficient
        return this
    }

    fun setSpecularLightCoefficient(coefficient: Light): LightProgram {
        mSpecularLightCoefficient = coefficient
        return this
    }

    fun setLightSourceType(lightSourceType: LightSourceType): LightProgram {
        mLightSourceType = lightSourceType
        return this
    }

    fun setTexture(texture: GLTexture): LightProgram {
        mTexture = texture
        mTextureType = TextureType.Texture
        return this
    }

    fun setColor(color: Color): LightProgram {
        mColor = color
        mTextureType = TextureType.Color
        return this
    }

    fun setDrawMode(drawMode: DrawMode): LightProgram {
        mDrawMode = drawMode
        return this
    }

    override fun onInit() {
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
        mTextureHandle = getUniformLocation("uTexture")
        mColorHandle = getUniformLocation("uColor")
        mIsUseTextureHandle = getUniformLocation("uIsUseTexture")
    }

    override fun onDraw() {
        if (mTextureType == TextureType.Texture) {
            val texture = mTexture
            if (texture == null) {
                Log.e(TAG, "Texture is null.")
            } else {
                texture.bind { realDraw() }
            }
        } else {
            realDraw()
        }
    }

    override fun onRelease() {
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
        mTextureHandle = 0
        mTextureCoordHandle = 0
        mColorHandle = 0
        mIsUseTextureHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        when (lightCalculateType) {
            LightCalculateType.Vertex -> "light/gouraud/vertex.glsl"
            LightCalculateType.Fragment -> "light/phong/vertex.glsl"
        }
    )

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        when (lightCalculateType) {
            LightCalculateType.Vertex -> "light/gouraud/fragment.glsl"
            LightCalculateType.Fragment -> "light/phong/fragment.glsl"
        }
    )

    private fun realDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix.matrix, 0)
        GLES20.glUniform3f(mLightPointHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
        GLES20.glUniform3f(mViewPointHandle, mViewPoint.x, mViewPoint.y, mViewPoint.z)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, modelData.vertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        if (mTextureType == TextureType.Texture) {
            val textureBuffer = modelData.textureBuffer
            if (textureBuffer == null) {
                Log.e(TAG, "Texture buffer is null.")
            } else {
                GLES20.glVertexAttribPointer(mTextureCoordHandle, modelData.textureStep, GLES20.GL_FLOAT, false, modelData.textureStep * 4, textureBuffer)
                GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
            }
        } else {
            GLES20.glUniform4f(mColorHandle, mColor.red, mColor.green, mColor.blue, mColor.alpha)
        }
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
    }

    companion object {
        const val TAG = "LightProgram"
    }
}