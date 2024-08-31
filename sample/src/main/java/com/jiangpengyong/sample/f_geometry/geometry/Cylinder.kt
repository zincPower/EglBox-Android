package com.jiangpengyong.sample.f_geometry.geometry

import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix

class CylinderFilter(
    val radius: Float = 0.5F,
    val height: Float = 1F,
    val segment: Int = 30,
) : GLFilter() {
    private val mProgram = GeometryProgram()

    private var mTopTexture: GLTexture? = null
    private var mBottomTexture: GLTexture? = null
    private var mSideTexture: GLTexture? = null

    private val mCircleInfo: GeometryInfo
    private val mCylinderSide: GeometryInfo

    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mXAngle = 0F
    private var mYAngle = 0F

    private var mDisplaySize = Size(0, 0)
    private var mLightPosition = floatArrayOf(0F, 0F, 5F)
    private var mCameraPosition = floatArrayOf(0F, 0F, 10F)

    private var mTopMatrix = ModelMatrix().apply {
        translate(0F, 0F, height / 2F)
    }
    private var mBottomMatrix = ModelMatrix().apply {
        translate(0F, 0F, -height / 2F)
        rotate(180F, 1F, 0F, 0F)
    }

    init {
        val circle = Circle(radius, segment)
        mCircleInfo = circle.create()

        val cylinderSide = CylinderSide(radius, height, segment)
        mCylinderSide = cylinderSide.create()
    }

    fun setTexture(
        topTexture: GLTexture,
        sideTexture: GLTexture,
        bottomTexture: GLTexture,
    ) {
        this.mTopTexture = topTexture
        this.mSideTexture = sideTexture
        this.mBottomTexture = bottomTexture
    }

    override fun onInit() {
        mProgram.init()
        updateViewMatrix()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        updateProjectionMatrix(context)
        drawCircle(mTopMatrix)
        drawSide()
        drawCircle(mBottomMatrix)
    }

    override fun onRelease() {
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
    override fun onReceiveMessage(message: Message) {}

    private fun drawCircle(matrix: ModelMatrix) {
        mTopTexture?.let { mProgram.setTexture(it) }
        mProgram.setCameraPosition(mCameraPosition)
        mProgram.setLightPosition(mLightPosition)
        mProgram.setData(
            vertexBuffer = mCircleInfo.vertexBuffer,
            textureBuffer = mCircleInfo.textureBuffer,
            normalBuffer = mCircleInfo.normalBuffer,
            vertexCount = mCircleInfo.vertexCount
        )
        mProgram.setDrawMode(mCircleInfo.drawMode)
        mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mModelMatrix * matrix)
        mProgram.setMMatrix(mModelMatrix * matrix)
        mProgram.draw()
    }

    private fun drawSide() {
        mTopTexture?.let { mProgram.setTexture(it) }
        mProgram.setCameraPosition(mCameraPosition)
        mProgram.setLightPosition(mLightPosition)
        mProgram.setData(
            vertexBuffer = mCylinderSide.vertexBuffer,
            textureBuffer = mCylinderSide.textureBuffer,
            normalBuffer = mCylinderSide.normalBuffer,
            vertexCount = mCylinderSide.vertexCount
        )
        mProgram.setDrawMode(mCylinderSide.drawMode)
        mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
        mProgram.setMMatrix(mModelMatrix)
        mProgram.draw()
    }

    private fun updateProjectionMatrix(context: FilterContext) {
        val displaySize = context.displaySize
        if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
            val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
            if (displaySize.width > displaySize.height) {
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
            mDisplaySize = displaySize
        }
    }

    private fun updateViewMatrix() {
        mViewMatrix.setLookAtM(
            mCameraPosition[0], mCameraPosition[1], mCameraPosition[2],
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }
}