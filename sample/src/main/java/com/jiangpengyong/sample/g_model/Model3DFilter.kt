package com.jiangpengyong.sample.g_model

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_filter.EglBoxRuntime
import com.jiangpengyong.eglbox_filter.model.ModelData
import java.nio.FloatBuffer

/**
 * @author jiang peng yong
 * @date 2024/11/7 06:59
 * @email 56002982@qq.com
 * @des 3D 模型滤镜，支持顶点、法向量、纹理
 */
class Model3DFilter : GLFilter() {
    private val mVertProgram = Model3DProgram(CalculateLightingType.Vertex)
    private val mFragProgram = Model3DProgram(CalculateLightingType.Fragment)

    private var mSideRenderingType = SideRenderingType.Single
    private var mModelData: ModelData? = null
    private var mTexture: GLTexture? = null
    private var mIsVertexCalculateLighting = true

    private val mModelMatrix = ModelMatrix()

    override fun onInit(context: FilterContext) {
        mVertProgram.init()
        mFragProgram.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val modelData = mModelData ?: return
        val texture = imageInOut.texture ?: return
        val vertexBuffer = modelData.vertexBuffer
        val textureBuffer = modelData.textureBuffer
        val normalBuffer = modelData.normalBuffer ?: return

        val fbo = context.getTexFBO(texture.width, texture.height, DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            if (mSideRenderingType == SideRenderingType.Double) {
                GLES20.glDisable(GLES20.GL_CULL_FACE)
            } else {
                GLES20.glEnable(GLES20.GL_CULL_FACE)
            }

            val modelMatrix = context.space3D.gestureMatrix * mModelMatrix
            val mvpMatrix = context.space3D.projectionMatrix * context.space3D.viewMatrix * modelMatrix

//            context.texture2DProgram.reset()
//            context.texture2DProgram.setVertexMatrix(mvpMatrix.matrix)
//            context.texture2DProgram.setTexture(texture)
//            context.texture2DProgram.draw()

            GLES20.glFrontFace(modelData.frontFace.value)

            val program = if (mIsVertexCalculateLighting) mVertProgram else mFragProgram
            program.setLightPoint(context.space3D.lightPoint)
            program.setViewPoint(context.space3D.viewPoint)
            program.setData(
                vertexBuffer = vertexBuffer,
                textureBuffer = textureBuffer,
                normalBuffer = normalBuffer,
                vertexCount = modelData.count,
            )
            program.setTexture(mTexture)
            program.setMVPMatrix(mvpMatrix)
            program.setModelMatrix(modelMatrix)
            program.setSideRendering(mSideRenderingType)
            program.draw()

            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            if (mSideRenderingType != SideRenderingType.Double) {
                GLES20.glDisable(GLES20.GL_CULL_FACE)
            }
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mVertProgram.release()
        mFragProgram.release()
        mTexture?.release()
        mTexture = null
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            Model3DMessageType.SET_MODEL_DATA.value -> {
                mModelMatrix.reset()
                mModelData = message.obj as? ModelData
                mModelData?.apply {
                    space.apply {
                        mModelMatrix.translate(
                            -(left + right) / 2F,
                            -(top + bottom) / 2F,
                            -(near + far) / 2F,
                        )
                    }
                }
            }

            Model3DMessageType.SET_MODEL_TEXTURE_IMAGE.value -> {
                (message.obj as? Bitmap)?.apply {
                    mTexture?.release()
                    mTexture = GLTexture.createColorTexture()
                    mTexture?.init()
                    mTexture?.setData(this)
                }
            }

            Model3DMessageType.SET_SIDE_RENDERING_TYPE.value->{
                mSideRenderingType = message.obj as SideRenderingType
            }

            Model3DMessageType.RESET_ALL_DATA.value -> {
                mModelData = null
                mTexture?.release()
                mTexture = null
                mIsVertexCalculateLighting = false
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
 * @des 模型 3D，支持顶点、法向量、纹理
 */
class Model3DProgram(private val calculateLightingType: CalculateLightingType = CalculateLightingType.Vertex) : GLProgram() {
    private var mVertexBuffer: FloatBuffer? = null
    private var mTextureBuffer: FloatBuffer? = null
    private var mNormalBuffer: FloatBuffer? = null
    private var mVertexCount = 0

    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mLightPointHandle = 0
    private var mViewPointHandle = 0
    private var mPositionHandle = 0
    private var mTextureCoordHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mIsAddAmbientLightHandle = 0
    private var mIsAddDiffuseLightHandle = 0
    private var mIsAddSpecularHandle = 0
    private var mIsUseTextureHandle = 0
    private var mIsDoubleSideRenderingHandle = 0

    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mModelMatrix: GLMatrix = GLMatrix()

    private var mLightPoint = Point(0F, 0F, 5F)
    private var mViewPoint = Point(0F, 0F, 10F)
    private var mShininess = 50F
    private var mTexture: GLTexture? = null

    private var mIsAddAmbientLight = true
    private var mIsAddDiffuseLight = true
    private var mIsAddSpecularLight = true
    private var mSideRenderingType = SideRenderingType.Single

    fun setTexture(texture: GLTexture?) {
        mTexture = texture
    }

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

    fun isAddAmbientLight(value: Boolean) {
        mIsAddAmbientLight = value
    }

    fun isAddDiffuseLight(value: Boolean) {
        mIsAddDiffuseLight = value
    }

    fun isAddSpecularLight(value: Boolean) {
        mIsAddSpecularLight = value
    }

    fun setSideRendering(value: SideRenderingType) {
        mSideRenderingType = value
    }

    fun setData(
        vertexBuffer: FloatBuffer,
        textureBuffer: FloatBuffer?,
        normalBuffer: FloatBuffer,
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
        mLightPointHandle = getUniformLocation("uLightPoint")
        mViewPointHandle = getUniformLocation("uViewPoint")
        mPositionHandle = getAttribLocation("aPosition")
        mNormalHandle = getAttribLocation("aNormal")
        mShininessHandle = getAttribLocation("aShininess")
        mTextureCoordHandle = getAttribLocation("aTextureCoord")
        mIsAddAmbientLightHandle = getUniformLocation("uIsAddAmbientLight")
        mIsAddDiffuseLightHandle = getUniformLocation("uIsAddDiffuseLight")
        mIsAddSpecularHandle = getUniformLocation("uIsAddSpecularLight")
        mIsUseTextureHandle = getUniformLocation("uIsUseTexture")
        mIsDoubleSideRenderingHandle = getUniformLocation("uIsDoubleSideRendering")
    }

    override fun onDraw() {
        mVertexBuffer ?: return
        mNormalBuffer ?: return

        mTexture?.bind()

        // MVP 矩阵
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)

        // 模型矩阵
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mModelMatrix.matrix, 0)

        // 光源位置
        GLES20.glUniform3f(mLightPointHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)

        // 相机位置（观察位置）
        GLES20.glUniform3f(mViewPointHandle, mViewPoint.x, mViewPoint.y, mViewPoint.z)

        // 光效控制
        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
        GLES20.glUniform1i(mIsAddAmbientLightHandle, if (mIsAddAmbientLight) 1 else 0)
        GLES20.glUniform1i(mIsAddDiffuseLightHandle, if (mIsAddDiffuseLight) 1 else 0)
        GLES20.glUniform1i(mIsAddSpecularHandle, if (mIsAddSpecularLight) 1 else 0)

        // 控制双面渲染
        GLES20.glUniform1i(mIsDoubleSideRenderingHandle, if (mSideRenderingType == SideRenderingType.Single) 0 else 1)

        // 顶点
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // 法向量
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mNormalBuffer)
        GLES20.glEnableVertexAttribArray(mNormalHandle)

        // 纹理
        GLES20.glUniform1i(mIsUseTextureHandle, if (mTextureBuffer == null) 0 else 1)
        if (mTextureBuffer != null) {
            GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer)
            GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
        }

        // 渲染方式
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
        if (mTextureBuffer != null) {
            GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
        }
        mTexture?.unbind()
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mMMatrixHandle = 0
        mLightPointHandle = 0
        mViewPointHandle = 0
        mPositionHandle = 0
        mTextureCoordHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mIsAddAmbientLightHandle = 0
        mIsAddDiffuseLightHandle = 0
        mIsAddSpecularHandle = 0
        mIsUseTextureHandle = 0
        mIsDoubleSideRenderingHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        when (calculateLightingType) {
            CalculateLightingType.Vertex -> "glsl/model/vert/vertex.glsl"
            CalculateLightingType.Fragment -> "glsl/model/frag/vertex.glsl"
        },
    )

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        when (calculateLightingType) {
            CalculateLightingType.Vertex -> "glsl/model/vert/fragment.glsl"
            CalculateLightingType.Fragment -> "glsl/model/frag/fragment.glsl"
        },
    )
}

enum class CalculateLightingType {
    Vertex,
    Fragment,
}

enum class Model3DMessageType(val value: Int) {
    SET_MODEL_DATA(10000),               // 设置模型数据
    SET_SIDE_RENDERING_TYPE(10001),      // 设置渲染面类型
    SET_MODEL_TEXTURE_IMAGE(10002),      // 设置模型纹理数据
    RESET_ALL_DATA(10003),               // 重置
}