package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.sample.e_texture.planet.SolarSystemActivity.Companion.MESSAGE_RUN

class SolarSystemFilter : GLFilter() {
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mGestureMatrix = ModelMatrix()

    private var mXAngle = 0F
    private var mYAngle = 0F
    private var mRotation = 0F
    private var mDisplaySize = Size(0, 0)

    private val mSunProgram = SunProgram()
    private val mPlanetProgram = PlanetProgram()

    private var mEarthRatio = 1 / 3F
    private var mEarthOrbitSpeed = 1 / 2F
    private var mEarthRotationSpeed = 1F

    private var mSunPosition = floatArrayOf(0F, 0F, 0F)
    private var mCameraPosition = floatArrayOf(0F, 0F, 30F)

    private val mMoonInfo = CelestialBodyInfo(CelestialBody.Moon, 2F, -0.5F, mEarthRatio * 0.5F, mEarthOrbitSpeed * 4F, mEarthOrbitSpeed * 4F)
    private val mSunInfo = CelestialBodyInfo(CelestialBody.Sun, 0F, 0F, 1.5F, 0F, 0F)
    private val mPlanetInfo = listOf(
        CelestialBodyInfo(CelestialBody.Mercury, 2.2F, -0.034F, mEarthRatio * 0.5F, mEarthOrbitSpeed / 0.24F, mEarthRotationSpeed * 2F),
        CelestialBodyInfo(CelestialBody.Venus, 3.3F, -177.4F, mEarthRatio * 0.949F, mEarthOrbitSpeed / 0.62F, mEarthRotationSpeed * 1.2F),
        CelestialBodyInfo(CelestialBody.Earth, 4.5F, -23.44F, mEarthRatio, mEarthOrbitSpeed, mEarthRotationSpeed),
        CelestialBodyInfo(CelestialBody.Mars, 5.5F, -25.19F, mEarthRatio * 0.8F, mEarthOrbitSpeed / 1.88F, mEarthRotationSpeed),
        CelestialBodyInfo(CelestialBody.Jupiter, 7F, -3.13F, mEarthRatio * 2F, mEarthOrbitSpeed / 11.86F * 5F, mEarthRotationSpeed * 0.41F),
        CelestialBodyInfo(CelestialBody.Saturn, 9F, -26.73F, mEarthRatio * 1.9F, mEarthOrbitSpeed / 29.46F * 5F, mEarthRotationSpeed * 0.45F),
        CelestialBodyInfo(CelestialBody.Uranus, 11F, -97.86F, mEarthRatio * 1.6F, mEarthOrbitSpeed / 84.01F * 5F, mEarthRotationSpeed * 0.72F),
        CelestialBodyInfo(CelestialBody.Neptune, 12.5F, -28.32F, mEarthRatio * 1.6F, mEarthOrbitSpeed / 164.79F * 5F, mEarthRotationSpeed * 0.67F),
    )

    override fun onInit() {
        mViewMatrix.setLookAtM(
            mCameraPosition[0], mCameraPosition[1], mCameraPosition[2], 0F, 0F, 0F, 0F, 1F, 0F
        )

        mSunProgram.init()
        mPlanetProgram.init()

        mSunInfo.init()
        mMoonInfo.init()
        for (planetInfo in mPlanetInfo) {
            planetInfo.init()
        }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        GLES20.glClearColor(0F, 0F, 0F, 1F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        updateProjectionMatrix(context)

        synchronized(this) {
            mSunProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * mSunInfo.matrix)
        }
        mSunProgram.setTexture(mSunInfo.texture)
        mSunProgram.draw()

        for (planetInfo in mPlanetInfo) {
            mPlanetProgram.setTexture(planetInfo.texture)
            mPlanetProgram.setLightPosition(mSunPosition)
            synchronized(this) {
                mPlanetProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * planetInfo.matrix)
                mPlanetProgram.setMMatrix(mGestureMatrix * planetInfo.matrix)
            }
            mPlanetProgram.setShininess(3F)
            mPlanetProgram.draw()

            if (planetInfo.celestialBody == CelestialBody.Earth) {
                mPlanetProgram.setTexture(mMoonInfo.texture)
                mPlanetProgram.setLightPosition(mSunPosition)
                synchronized(this) {
                    mPlanetProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * planetInfo.matrix * mMoonInfo.matrix)
                    mPlanetProgram.setMMatrix(mGestureMatrix * planetInfo.matrix * mMoonInfo.matrix)
                }
                mPlanetProgram.setShininess(3F)
                mPlanetProgram.draw()
            }
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
            mGestureMatrix.reset()
            mGestureMatrix.rotate(mXAngle, 0F, 1F, 0F)
            mGestureMatrix.rotate(mYAngle, 1F, 0F, 0F)

            mRotation += updateData.getFloat("rotation", 0F)
        }
    }

    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        if (message.what == MESSAGE_RUN) {
            synchronized(this) {
                for (planetInfo in mPlanetInfo) {
                    planetInfo.orbitAndRotation()
                }
                mMoonInfo.orbitAndRotation()
            }
        }
    }
}