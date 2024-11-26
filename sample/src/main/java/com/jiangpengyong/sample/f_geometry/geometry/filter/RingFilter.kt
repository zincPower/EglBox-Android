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
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.sample.f_geometry.geometry.GeometryProgram
import com.jiangpengyong.sample.f_geometry.geometry.DrawMode

/**
 * @author jiang peng yong
 * @date 2024/8/31 17:25
 * @email 56002982@qq.com
 * @des 圆环
 */
class RingFilter(
    majorRadius: Float = 0.7F,
    minorRadius: Float = 0.3F,
    majorSegment: Int = 72,
    minorSegment: Int = 30,
) : GLFilter() {
    private val mProgram = GeometryProgram()

    private var mTexture: GLTexture? = null

    private val mModelData: ModelData = ModelCreator.createRing(majorRadius, minorRadius, majorSegment, minorSegment)

    private val mProjectMatrix = ProjectionMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mXAngle = 0F
    private var mYAngle = 0F

    private var mPreviewSize = Size(0, 0)
    private var mLightPoint = floatArrayOf(0F, 0F, 5F)
    private var mViewPoint = floatArrayOf(0F, 0F, 10F)

    fun setTexture(texture: GLTexture) {
        this.mTexture = texture
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
        mTexture?.let { mProgram.setTexture(it) }
        mProgram.setViewPoint(mViewPoint)
        mProgram.setLightPoint(mLightPoint)
        mProgram.setData(
            vertexBuffer = mModelData.vertexBuffer,
            textureBuffer = mModelData.textureBuffer ?: return,
            normalBuffer = mModelData.normalBuffer ?: return,
            vertexCount = mModelData.count
        )
        GLES20.glFrontFace(mModelData.frontFace.value)
        mProgram.setDrawMode(DrawMode.Triangles)
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