package com.jiangpengyong.sample.i_scene.grayscale_terrain

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Range
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.WrapMode
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.EglBoxRuntime
import com.jiangpengyong.eglbox_filter.model.ModelData

class GrayscaleTerrainFilter : GLFilter() {
    private var mGrayscaleTerrainData: GrayscaleTerrainData? = null
    private var mGrassTexture = GLTexture(
        wrapS = WrapMode.REPEAT,
        wrapT = WrapMode.REPEAT,
    )
    private var mRockTexture = GLTexture(
        wrapS = WrapMode.REPEAT,
        wrapT = WrapMode.REPEAT,
    )

    private var mProgram = Program()

    private val mViewMatrix = ViewMatrix()

    override fun onInit(context: FilterContext) {
        mGrassTexture.init()
        mRockTexture.init()

        mProgram.init()

        val space3D = context.space3D
        mViewMatrix.reset()
        mViewMatrix.setLookAtM(
            eyeX = space3D.viewPoint.x, eyeY = space3D.viewPoint.y, eyeZ = space3D.viewPoint.z,
            centerX = 0F, centerY = 0F, centerZ = 0F,
            upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val grayscaleTerrainData = mGrayscaleTerrainData ?: return
        if (!mGrassTexture.isInit()) return
        if (!mRockTexture.isInit()) return
        val texture = imageInOut.texture ?: return

        val space3D = context.space3D
        val vpMatrix = space3D.projectionMatrix * mViewMatrix

        val fbo = context.getTexFBO(texture.width, texture.height)
        fbo.use {
            mProgram.setGrassTexture(mGrassTexture)
            mProgram.setRockTexture(mRockTexture)
            mProgram.setMVPMatrix(vpMatrix)
            mProgram.setModelData(grayscaleTerrainData.modelData)
            mProgram.setBoundaryRange(grayscaleTerrainData.boundaryRange)
            mProgram.draw()
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mProgram.release()

        mGrayscaleTerrainData = null
        mGrassTexture.release()
        mRockTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}

    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            Type.UPDATE_DATA.value -> {
                mGrayscaleTerrainData = message.obj as GrayscaleTerrainData
            }

            Type.UPDATE_GRASS_TEXTURE.value -> {
                (message.obj as Bitmap).let {
                    mGrassTexture.setData(it)
                    it.recycle()
                }
            }

            Type.UPDATE_ROCK_TEXTURE.value -> {
                (message.obj as Bitmap).let {
                    mRockTexture.setData(it)
                    it.recycle()
                }
            }
        }
    }

    enum class Type(val value: Int) {
        UPDATE_DATA(10000),
        UPDATE_GRASS_TEXTURE(10001),
        UPDATE_ROCK_TEXTURE(10002),
    }

    data class GrayscaleTerrainData(
        val modelData: ModelData,
        val boundaryRange: Range<Float>,
    )

    companion object {
        const val TAG = "GrayscaleTerrainFilter"
    }
}

private class Program : GLProgram() {
    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0
    private var mTexCoordHandle = 0
    private var mTextureGrassHandle = 0
    private var mTextureRockHandle = 0
    private var mBoundaryStartYHandle = 0
    private var mBoundaryEndYHandle = 0

    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mBoundaryRange: Range<Float> = Range(10F, 20F)
    private var mGrassTexture: GLTexture? = null
    private var mRockTexture: GLTexture? = null
    private var mModelData: ModelData? = null

    fun setMVPMatrix(matrix: GLMatrix): Program {
        mMVPMatrix = matrix
        return this
    }

    fun setBoundaryRange(range: Range<Float>): Program {
        mBoundaryRange = range
        return this
    }

    fun setGrassTexture(texture: GLTexture): Program {
        mGrassTexture = texture
        return this
    }

    fun setRockTexture(texture: GLTexture): Program {
        mRockTexture = texture
        return this
    }

    fun setModelData(modelData: ModelData): Program {
        mModelData = modelData
        return this
    }

    override fun onInit() {
        mPositionHandle = getAttribLocation("aPosition")
        mTexCoordHandle = getAttribLocation("aTexCoord")
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mTextureGrassHandle = getUniformLocation("sTextureGrass")
        mTextureRockHandle = getUniformLocation("sTextureRock")
        mBoundaryStartYHandle = getUniformLocation("uBoundaryStartY")
        mBoundaryEndYHandle = getUniformLocation("uBoundaryEndY")
    }

    override fun onDraw() {
        val grassTexture = mGrassTexture ?: return
        val rockTexture = mRockTexture ?: return
        val modelData = mModelData ?: return

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glFrontFace(modelData.frontFace.value)

        grassTexture.bind(textureUnit = GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(mTextureGrassHandle, 0)
        rockTexture.bind(textureUnit = GLES20.GL_TEXTURE1)
        GLES20.glUniform1i(mTextureRockHandle, 1)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, modelData.vertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mTexCoordHandle, modelData.textureStep, GLES20.GL_FLOAT, false, modelData.textureStep * 4, modelData.textureBuffer)
        GLES20.glEnableVertexAttribArray(mTexCoordHandle)
        GLES20.glUniform1f(mBoundaryStartYHandle, mBoundaryRange.lower)
        GLES20.glUniform1f(mBoundaryEndYHandle, mBoundaryRange.upper)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, modelData.count)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTexCoordHandle)

        grassTexture.unbind()
        rockTexture.unbind()

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
        mTexCoordHandle = 0
        mTextureGrassHandle = 0
        mTextureRockHandle = 0
        mBoundaryStartYHandle = 0
        mBoundaryEndYHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/grayscale_terrain/vertex.glsl"
    )

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        "glsl/grayscale_terrain/fragment.glsl"
    )
}