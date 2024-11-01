package com.jiangpengyong.sample.g_model.common

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.EGLBoxRuntime
import java.nio.FloatBuffer

class Model3DFilter : GLFilter() {
    private val mProgram = Model3DProgram()

    private var mModel3DInfo: Model3DInfo? = null
    private var mTexture: GLTexture? = null

    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mPreviewSize = Size(0, 0)
    private var mLightPosition = floatArrayOf(0F, 0F, 100F)
    private var mCameraPosition = floatArrayOf(0F, 0F, 100F)

    override fun onInit() {
        mProgram.init()
        mViewMatrix.setLookAtM(
            mCameraPosition[0], mCameraPosition[1], mCameraPosition[2],
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val model3DInfo = mModel3DInfo ?: return
        val texture = imageInOut.texture ?: return
        updateProjectionMatrix(Size(texture.width, texture.height))
        val fbo = context.getTexFBO(texture.width, texture.height)
        fbo.use {
            GLES20.glClearColor(0F, 0F, 0F, 1F)
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)

            context.texture2DProgram.reset()
            context.texture2DProgram.setTexture(texture)
            context.texture2DProgram.draw()

            mProgram.setLightPosition(mLightPosition)
            mProgram.setCameraPosition(mCameraPosition)
            mProgram.setData(
                model3DInfo.vertexBuffer,
                model3DInfo.textureBuffer,
                model3DInfo.normalBuffer,
                model3DInfo.count,
            )

            GLES20.glFrontFace(model3DInfo.frontFace.value)
            val modelMatrix = mModelMatrix * context.space3D.gestureMatrix
            mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * modelMatrix)
            mProgram.setMMatrix(modelMatrix)
            mProgram.draw()

            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }
        imageInOut.out(fbo)
    }

    override fun onRelease() {
        mProgram.release()
    }

    private fun updateProjectionMatrix(size: Size) {
        if (mPreviewSize.width != size.width || mPreviewSize.height != size.height) {
            mProjectMatrix.reset()
            if (size.width > size.height) {
                val ratio = size.width.toFloat() / size.height.toFloat()
                mProjectMatrix.setFrustumM(
                    -ratio, ratio,
                    -1F, 1F,
                    2F, 1000F
                )
            } else {
                val ratio = size.height.toFloat() / size.width.toFloat()
                mProjectMatrix.setFrustumM(
                    -1F, 1F,
                    -ratio, ratio,
                    2F, 1000F
                )
            }
            mPreviewSize = size
        }

    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            Model3DMessageType.SET_MODEL_DATA.value -> {
                mModel3DInfo = message.obj as? Model3DInfo
                mModelMatrix.reset()
                mModel3DInfo?.space?.apply {
                    mModelMatrix.translate(
                        -(left + right) / 2F,
                        -(top + bottom) / 2F,
                        -(near + far) / 2F,
                    )
                }
            }

            Model3DMessageType.SET_MODEL_TEXTURE_IMAGE.value -> {
                (message.obj as? Bitmap)?.apply {
                    mTexture?.release()
                    mTexture = GLTexture()
                    mTexture?.init()
                    mTexture?.setData(this)
                }
            }
        }
    }

    companion object {
        const val TAG = "Model3DFilter"
    }
}

/**
 * @author jiang peng yong
 * @date 2024/6/19 10:08
 * @email 56002982@qq.com
 * @des 模型 3D
 */
class Model3DProgram : GLProgram() {
    private var mVertexBuffer: FloatBuffer? = null
    private var mTextureBuffer: FloatBuffer? = null
    private var mNormalBuffer: FloatBuffer? = null
    private var mVertexCount = 0

    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mLightPositionHandle = 0
    private var mCameraPositionHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mIsAddAmbientLightHandle = 0
    private var mIsAddScatteredLightHandle = 0
    private var mIsAddSpecularHandle = 0

    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mMMatrix: GLMatrix = GLMatrix()

    private var mLightPosition = floatArrayOf(0F, 0F, 5F)
    private var mCameraPosition = floatArrayOf(0F, 0F, 10F)
    private var mShininess = 50F

    private var mIsAddAmbientLight = true
    private var mIsAddScatteredLight = true
    private var mIsAddSpecularLight = true

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setMMatrix(matrix: GLMatrix) {
        mMMatrix = matrix
    }

    fun setLightPosition(lightPosition: FloatArray) {
        mLightPosition = lightPosition
    }

    fun setCameraPosition(cameraPosition: FloatArray) {
        mCameraPosition = cameraPosition
    }

    fun setShininess(shininess: Float) {
        mShininess = shininess
    }

    fun isAddAmbientLight(value: Boolean) {
        mIsAddAmbientLight = value
    }

    fun isAddScatteredLight(value: Boolean) {
        mIsAddScatteredLight = value
    }

    fun isAddSpecularLight(value: Boolean) {
        mIsAddSpecularLight = value
    }

    fun setData(
        vertexBuffer: FloatBuffer?,
        textureBuffer: FloatBuffer?,
        normalBuffer: FloatBuffer?,
        vertexCount: Int,
    ) {
        mVertexBuffer = vertexBuffer
        mTextureBuffer = textureBuffer
        mNormalBuffer = normalBuffer
        mVertexCount = vertexCount
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mMMatrixHandle = getUniformLocation("uMMatrix")
        mLightPositionHandle = getUniformLocation("uLightPosition")
        mCameraPositionHandle = getUniformLocation("uCameraPosition")
        mPositionHandle = getAttribLocation("aPosition")
        mNormalHandle = getAttribLocation("aNormal")
        mShininessHandle = getAttribLocation("aShininess")
        mIsAddAmbientLightHandle = getUniformLocation("uIsAddAmbientLight")
        mIsAddScatteredLightHandle = getUniformLocation("uIsAddScatteredLight")
        mIsAddSpecularHandle = getUniformLocation("uIsAddSpecularLight")
    }

    override fun onDraw() {
        mVertexBuffer ?: return
        mNormalBuffer ?: return

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        // 模型矩阵
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mMMatrix.matrix, 0)
        // 光源位置
        GLES20.glUniform3f(mLightPositionHandle, mLightPosition[0], mLightPosition[1], mLightPosition[2])
        // 相机位置（观察位置）
        GLES20.glUniform3f(mCameraPositionHandle, mCameraPosition[0], mCameraPosition[1], mCameraPosition[2])
        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
        GLES20.glUniform1i(mIsAddAmbientLightHandle, if (mIsAddAmbientLight) 1 else 0)
        GLES20.glUniform1i(mIsAddScatteredLightHandle, if (mIsAddScatteredLight) 1 else 0)
        GLES20.glUniform1i(mIsAddSpecularHandle, if (mIsAddSpecularLight) 1 else 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        // 法向量
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mNormalBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mMMatrixHandle = 0
        mLightPositionHandle = 0
        mCameraPositionHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mIsAddAmbientLightHandle = 0
        mIsAddScatteredLightHandle = 0
        mIsAddSpecularHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(EGLBoxRuntime.context.resources, "glsl/light/full_light/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(EGLBoxRuntime.context.resources, "glsl/light/full_light/fragment.glsl")
}

enum class Model3DMessageType(val value: Int) {
    SET_MODEL_DATA(90000),          // 设置模型数据
    SET_MODEL_TEXTURE_IMAGE(90001), // 设置模型纹理数据
}