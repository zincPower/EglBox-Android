package com.jiangpengyong.sample.e_texture.planet

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.sample.App
import java.io.File

class SolarSystemFilter : GLFilter() {
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mXAngle = 0F
    private var mYAngle = 0F

    private var mDisplaySize = Size(0, 0)

    private var mCameraPosition = floatArrayOf(0F, 0F, 30F)

    private val mPlanetProgram = PlanetProgram()
    private var mEarthRatio = 1 / 3F

    private val mSunProgram = SunProgram()
    private val mSunTexture = GLTexture()
    private val mSunMatrix = ModelMatrix()
    private var mSunPosition = floatArrayOf(0F, 0F, 0F)

    private val mPlanetInfo = listOf(
        PlanetInfo(PlanetType.Mercury, 2F, -0.034F, mEarthRatio * 0.5F),
        PlanetInfo(PlanetType.Venus, 3F, -177.4F, mEarthRatio * 0.949F),
        PlanetInfo(PlanetType.Earth, 4F, -23.44F, mEarthRatio),
        PlanetInfo(PlanetType.Mars, 5F, -25.19F, mEarthRatio * 0.8F),
        PlanetInfo(PlanetType.Jupiter, 6.5F, -3.13F, mEarthRatio * 2F),
        PlanetInfo(PlanetType.Saturn, 8F, -26.73F, mEarthRatio * 1.5F),
        PlanetInfo(PlanetType.Uranus, 9.5F, -97.86F, mEarthRatio * 1.3F),
        PlanetInfo(PlanetType.Neptune, 11F, -28.32F, mEarthRatio * 1.3F),
    )

    override fun onInit() {
        mViewMatrix.setLookAtM(
            mCameraPosition[0], mCameraPosition[1], mCameraPosition[2], 0F, 0F, 0F, 0F, 1F, 0F
        )

        mPlanetProgram.init()

        mSunProgram.init()
        mSunTexture.init()
        BitmapFactory.decodeFile(File(App.context.filesDir, "images/heavenly_body/2k_sun.jpg").absolutePath).let { bitmap ->
            mSunTexture.setData(bitmap)
            bitmap.recycle()
        }
        mSunMatrix.scale(1.2F, 1.2F, 1.2F)

        for (planetInfo in mPlanetInfo) {
            planetInfo.init()
        }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        GLES20.glClearColor(0F, 0F, 0F, 1F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        updateProjectionMatrix(context)

        val mvpMatrix: GLMatrix
        synchronized(this) {
            mvpMatrix = mProjectMatrix * mViewMatrix * mModelMatrix
        }

        mSunProgram.setMVPMatrix(mvpMatrix * mSunMatrix)
        mSunProgram.setTexture(mSunTexture)
        mSunProgram.draw()

        for (planetInfo in mPlanetInfo) {
            mPlanetProgram.setTexture(planetInfo.texture)
            mPlanetProgram.setLightPosition(mSunPosition)
            mPlanetProgram.setMVPMatrix(mvpMatrix * planetInfo.matrix)
            mPlanetProgram.setMMatrix(mModelMatrix * planetInfo.matrix)
            mPlanetProgram.setShininess(3F)
            mPlanetProgram.draw()
        }
    }

    override fun onRelease() {
        mSunProgram.release()
    }

    private fun updateProjectionMatrix(context: FilterContext) {
        val displaySize = context.displaySize
        if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
            val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
            if (displaySize.width > displaySize.height) {
                mProjectMatrix.setFrustumM(
                    -ratio, ratio, -1F, 1F, 5F, 1000F
                )
            } else {
                mProjectMatrix.setFrustumM(
                    -1F, 1F, -ratio, ratio, 5F, 1000F
                )
            }
            mDisplaySize = displaySize
        }
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
}