package com.jiangpengyong.sample.f_geometry.geometry.filter

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectionMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.sample.f_geometry.geometry.GeometryInfo
import com.jiangpengyong.sample.f_geometry.geometry.GeometryProgram
import com.jiangpengyong.sample.f_geometry.geometry.shape.GeometryBall

/**
 * @author jiang peng yong
 * @date 2024/8/31 17:25
 * @email 56002982@qq.com
 * @des 几何球
 */
class GeometryBallFilter(
    val length: Float = 2F,
    val n: Int = 5
) : GLFilter() {
    private val mProgram = GeometryProgram()

    private var mTopTexture: GLTexture? = null
    private var mBottomTexture: GLTexture? = null
    private var mSideTexture: GLTexture? = null

    private val mSpringInfo: GeometryInfo = GeometryBall(length, n).create()

    private val mProjectMatrix = ProjectionMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mXAngle = 0F
    private var mYAngle = 0F

    private var mPreviewSize = Size(0, 0)
    private var mLightPoint = floatArrayOf(0F, 0F, 5F)
    private var mViewPoint = floatArrayOf(0F, 0F, 10F)

    fun setTexture(
        topTexture: GLTexture,
        sideTexture: GLTexture,
        bottomTexture: GLTexture,
    ) {
        this.mTopTexture = topTexture
        this.mSideTexture = sideTexture
        this.mBottomTexture = bottomTexture
    }

    override fun onInit(context: FilterContext) {
        mProgram.init()
        updateViewMatrix()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        updateProjectionMatrix(context)
        drawTorus()
    }

    override fun onRelease(context: FilterContext) {
        mProgram.release()
    }

    override fun onUpdateData(updateData: Bundle) {
        synchronized(this) {
            mXAngle += updateData.getFloat("xAngle", 0F)
            mYAngle += updateData.getFloat("yAngle", 0F)
            mModelMatrix.reset()
            mModelMatrix.rotate(mXAngle, 0F, 1F, 0F)
            mModelMatrix.rotate(mYAngle, 1F, 0F, 0F)
        }
    }

    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) = synchronized(this) {
        if (message.what == RESET) {
            mXAngle = 0F
            mYAngle = 0F
            mModelMatrix.reset()
        }
    }

    private fun drawTorus() {
        mTopTexture?.let { mProgram.setTexture(it) }
        mProgram.setViewPoint(mViewPoint)
        mProgram.setLightPoint(mLightPoint)
        mProgram.setData(
            vertexBuffer = mSpringInfo.vertexBuffer,
            textureBuffer = mSpringInfo.textureBuffer,
            normalBuffer = mSpringInfo.normalBuffer,
            vertexCount = mSpringInfo.vertexCount
        )
        GLES20.glFrontFace(mSpringInfo.frontFace.value)
        mProgram.setDrawMode(mSpringInfo.drawMode)
//        mProgram.setDrawMode(DrawMode.LineStrip)
        mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
        mProgram.setMMatrix(mModelMatrix)
        mProgram.draw()
    }

    private fun updateProjectionMatrix(context: FilterContext) {
        val previewSize = context.previewSize
        if (mPreviewSize.width != previewSize.width || mPreviewSize.height != previewSize.height) {
            val ratio = previewSize.width.toFloat() / previewSize.height.toFloat()
            if (previewSize.width > previewSize.height) {
                mProjectMatrix.setFrustumM(
                    -ratio, ratio,
                    -1F, 1F,
                    5F, 20F
                )
            } else {
                mProjectMatrix.setFrustumM(
                    -1F, 1F,
                    -ratio, ratio,
                    5F, 20F
                )
            }
            mPreviewSize = previewSize
        }
    }

    private fun updateViewMatrix() {
        mViewMatrix.setLookAtM(
            mViewPoint[0], mViewPoint[1], mViewPoint[2],
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    companion object {
        const val RESET = 10000
    }
}