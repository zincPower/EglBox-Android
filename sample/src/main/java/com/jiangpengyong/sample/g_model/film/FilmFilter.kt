package com.jiangpengyong.sample.g_model.film

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.EGLBox
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import javax.microedition.khronos.opengles.GL

/**
 * @author jiang peng yong
 * @date 2024/10/10 08:56
 * @email 56002982@qq.com
 * @des 茶壶滤镜
 */
class FilmFilter : GLFilter() {
    private val mProgram = Model3DProgram()

    private var mModel3DInfo: Model3DInfo? = null
    private var mTexture: GLTexture? = null

    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mXAngle = 0F
    private var mYAngle = 0F

    private var mPreviewSize = Size(0, 0)
    private var mLightPosition = floatArrayOf(0F, 0F, 500F)
    private var mCameraPosition = floatArrayOf(0F, 0F, 500F)

    override fun onInit() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        mProgram.init()
        updateViewMatrix()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val srcTexture = imageInOut.texture ?: return
        val model3DInfo = mModel3DInfo ?: return
        val texture = mTexture ?: return
        updateProjectionMatrix(context)
        val fbo = context.getTexFBO(srcTexture.width, srcTexture.height)
        fbo.use {
            mProgram.setTexture(texture)
            mProgram.setCameraPosition(mCameraPosition)
            mProgram.setLightPosition(mLightPosition)
            mProgram.setData(
                vertexBuffer = allocateFloatBuffer(model3DInfo.vertexData),
                textureBuffer = model3DInfo.textureBuffer,
                normalBuffer = model3DInfo.normalBuffer,
                vertexCount = model3DInfo.count
            )
            GLES20.glFrontFace(model3DInfo.frontFace.value)

            mProgram.setDrawMode(DrawMode.Triangles)
            mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * context.space3D.gestureMatrix * mModelMatrix)
            mProgram.setMMatrix(context.space3D.gestureMatrix * mModelMatrix)
            mProgram.draw()
        }
        imageInOut.out(fbo)
    }

    override fun onRelease() {
        mProgram.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        synchronized(this) {
            when (message.what) {
                MessageWhat.RESET.value -> {
                    mXAngle = 0F
                    mYAngle = 0F
                    mModelMatrix.reset()
                }

                MessageWhat.OBJ_DATA.value -> {
                    mModel3DInfo = message.obj as? Model3DInfo
                    mModelMatrix.reset()
                    mModelMatrix.rotate(-45F, 1F, 0F, 0F)
                    mModel3DInfo?.space?.apply {
                        mModelMatrix.translate(
                            -(left + right) / 2F,
                            -(top + bottom) / 2F,
                            -(near + far) / 2F,
                        )
                    }
                }

                MessageWhat.OBJ_TEXTURE.value -> {
                    (message.obj as? Bitmap)?.apply {
                        mTexture?.release()
                        mTexture = GLTexture()
                        mTexture?.init()
                        mTexture?.setData(this)
                    }
                }

                else -> {}
            }
        }
    }

    private fun updateProjectionMatrix(context: FilterContext) {
        val previewSize = context.previewSize
        if (mPreviewSize.width != previewSize.width || mPreviewSize.height != previewSize.height) {
            if (previewSize.width > previewSize.height) {
                val ratio = previewSize.width.toFloat() / previewSize.height.toFloat()
                mProjectMatrix.setFrustumM(
                    -ratio, ratio,
                    -1F, 1F,
                    10F, 1000F
                )
            } else {
                val ratio = previewSize.height.toFloat() / previewSize.width.toFloat()
                mProjectMatrix.setFrustumM(
                    -1F, 1F,
                    -ratio, ratio,
                    10F, 1000F
                )
            }
            mPreviewSize = previewSize
        }
    }

    private fun updateViewMatrix() {
        mViewMatrix.setLookAtM(
            mCameraPosition[0], mCameraPosition[1], mCameraPosition[2],
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    companion object {
        const val TAG = "FilmFilter"
    }
}

enum class MessageWhat(val value: Int) {
    RESET(10000),
    OBJ_DATA(10001),
    OBJ_TEXTURE(10002),
}