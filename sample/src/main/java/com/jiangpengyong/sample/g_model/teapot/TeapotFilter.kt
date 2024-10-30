package com.jiangpengyong.sample.g_model.teapot

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
import com.jiangpengyong.eglbox_filter.toRadians
import com.jiangpengyong.sample.g_model.common.FrontFace
import com.jiangpengyong.sample.g_model.common.Model3DInfo
import com.jiangpengyong.sample.g_model.common.Model3DMessageType
import com.jiangpengyong.sample.g_model.common.Space
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class TeapotFilter : GLFilter() {
    private val mProgram = TeapotProgram()

    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mPreviewSize = Size(0, 0)

    private var mLightPosition = floatArrayOf(0F, 0F, 100F)
    private var mCameraPosition = floatArrayOf(0F, 0F, 100F)

    private var mModel3DInfo: Model3DInfo? = null
    private var mTexture: GLTexture? = null

    override fun onInit() {
        mProgram.init()
        mViewMatrix.setLookAtM(
            mCameraPosition[0], mCameraPosition[1], mCameraPosition[2],
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        imageInOut.texture?.let { texture ->
            updateProjectionMatrix(Size(texture.width, texture.height))
            val fbo = context.getTexFBO(texture.width, texture.height)
            fbo.use {
                GLES20.glClearColor(0F,0F,0F,1F)
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)

                context.texture2DProgram.reset()
                context.texture2DProgram.setTexture(texture)
                context.texture2DProgram.draw()

                updateProjectionMatrix(Size(imageInOut.texture?.width ?: 0, imageInOut.texture?.height ?: 0))
                mProgram.setLightPosition(mLightPosition)
                mProgram.setCameraPosition(mCameraPosition)
                mProgram.setData(
                    mModel3DInfo?.vertexBuffer,
                    mModel3DInfo?.textureBuffer,
                    mModel3DInfo?.normalBuffer,
                    mModel3DInfo?.count ?: 0,
                )

                GLES20.glFrontFace(mModel3DInfo?.frontFace?.value ?: FrontFace.CCW.value)
                mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mModelMatrix * context.space3D.gestureMatrix)
                mProgram.setMMatrix(mModelMatrix * context.space3D.gestureMatrix)
                mProgram.draw()
                GLES20.glDisable(GLES20.GL_DEPTH_TEST)
                GLES20.glDisable(GLES20.GL_CULL_FACE)
            }
            fbo.unbindTexture()?.let { imageInOut.out(it) }
        }
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
//                calculateVertex()
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
        const val TAG = "TeapotFilter"
    }

    private fun calculateVertex() {
        val vertexList = ArrayList<Float>()

        var verticalAngle = -90.0

        val mAngleSpan = 10
        val mRadius = 1F

        // 计算中间每一层的点
        while (verticalAngle < 90F) {   // 垂直角度从 -90 到 90
            // 这一层的半径
            val curLayerAngle = verticalAngle.toRadians()
            val layerRadius = mRadius * cos(curLayerAngle)

            // 下一层的半径
            val nextLayerAngle = (verticalAngle + mAngleSpan).toRadians()
            val nextLayerRadius = mRadius * cos(nextLayerAngle)

            val curLayerY = mRadius * sin(curLayerAngle)
            val nextLayerY = mRadius * sin(nextLayerAngle)

            var horizontalAngle = 0.0
            while (horizontalAngle < 360) {     // 水平角度从 0 到 360
                val curHorAngle = horizontalAngle.toRadians()
                val nextHorAngle = (horizontalAngle + mAngleSpan).toRadians()

                /**
                 *     P2(x0, y0, z0)   P3(x0, y0, z0)
                 *      ------------------
                 *      ｜              ╱｜
                 *      ｜            ╱  ｜
                 *      ｜          ╱    ｜
                 *      ｜        ╱      ｜
                 *      ｜      ╱        ｜
                 *      ｜    ╱          ｜
                 *      ｜  ╱            ｜
                 *      ｜╱              ｜
                 *      ------------------
                 *     P1(x0, y0, z0)   P0(x0, y0, z0)
                 */
                val x0 = layerRadius * cos(curHorAngle)
                val y0 = curLayerY
                val z0 = layerRadius * sin(curHorAngle)

                val x1 = layerRadius * cos(nextHorAngle)
                val y1 = curLayerY
                val z1 = layerRadius * sin(nextHorAngle)

                val x2 = nextLayerRadius * cos(nextHorAngle)
                val y2 = nextLayerY
                val z2 = nextLayerRadius * sin(nextHorAngle)

                val x3 = nextLayerRadius * cos(curHorAngle)
                val y3 = nextLayerY
                val z3 = nextLayerRadius * sin(curHorAngle)

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                vertexList.add(x0.toFloat())
                vertexList.add(y0.toFloat())
                vertexList.add(z0.toFloat())

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x2.toFloat())
                vertexList.add(y2.toFloat())
                vertexList.add(z2.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                horizontalAngle += mAngleSpan
            }
            verticalAngle += mAngleSpan
        }
        mModel3DInfo = Model3DInfo(
            vertexList.toFloatArray(),
            null,
            0,
            vertexList.toFloatArray(),
            FrontFace.CCW,
            Space(1F, -1F, -1F, 1F, 1F, -1F)
        )
    }
}

/**
 * @author jiang peng yong
 * @date 2024/6/19 10:08
 * @email 56002982@qq.com
 * @des 绘制球 —— 环境光、散射光、镜面光
 */
class TeapotProgram : GLProgram() {
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
