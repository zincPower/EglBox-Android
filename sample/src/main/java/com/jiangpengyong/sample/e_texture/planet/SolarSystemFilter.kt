package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.sample.e_texture.planet.SolarSystemActivity.Companion.MESSAGE_RUN
import com.jiangpengyong.sample.e_texture.planet.SolarSystemActivity.Companion.MESSAGE_TARGET

class SolarSystemFilter : GLFilter() {
    enum class Target(val value: Int) {
        SolarSystem(0),
        Earth(1),
        Saturn(2),
    }

    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mGestureMatrix = ModelMatrix()

    private var mXAngle = 0F
    private var mYAngle = 0F
    private var mRotation = 0F
    private var mDisplaySize = Size(0, 0)

    private val mSunProgram = SunProgram()
    private val mPlanetProgram = PlanetProgram()
    private val mRingProgram = RingProgram(smallRadius = 1.3F)

    private var mEarthRatio = 1 / 3F
    private var mEarthOrbitSpeed = 1 / 2F
    private var mEarthRotationSpeed = 1F

    private var mSunPosition = floatArrayOf(0F, 0F, 0F)

    private var mEyePosition = floatArrayOf(0F, 0F, 30F)
    private var mEyeTargetPosition = mEyePosition
    private var mEyeSourcePosition = mEyePosition

    private var mEyeTarget = Target.SolarSystem

    private val mMoonInfo = CelestialBodyInfo(CelestialBody.Moon, 2F, -0.5F, mEarthRatio * 0.5F, mEarthOrbitSpeed * 4F, mEarthOrbitSpeed * 4F)
    private val mSunInfo = CelestialBodyInfo(CelestialBody.Sun, 0F, 0F, 1.5F, 0F, 0F)
    private val mSaturnRingInfo = CelestialBodyInfo(CelestialBody.SaturnRing, 0F, -27F, 1.2F, 0F, mEarthRotationSpeed * 2F)
    private val mPlanetInfo = mapOf(
        CelestialBody.Mercury to CelestialBodyInfo(CelestialBody.Mercury, 2.2F, -0.034F, mEarthRatio * 0.5F, mEarthOrbitSpeed / 0.24F, mEarthRotationSpeed * 2F),
        CelestialBody.Venus to CelestialBodyInfo(CelestialBody.Venus, 3.3F, -177.4F, mEarthRatio * 0.949F, mEarthOrbitSpeed / 0.62F, mEarthRotationSpeed * 1.2F),
        CelestialBody.Earth to CelestialBodyInfo(CelestialBody.Earth, 4.5F, -23.44F, mEarthRatio, mEarthOrbitSpeed, mEarthRotationSpeed),
        CelestialBody.Mars to CelestialBodyInfo(CelestialBody.Mars, 5.5F, -25.19F, mEarthRatio * 0.8F, mEarthOrbitSpeed / 1.88F, mEarthRotationSpeed),
        CelestialBody.Jupiter to CelestialBodyInfo(CelestialBody.Jupiter, 7F, -3.13F, mEarthRatio * 2F, mEarthOrbitSpeed / 11.86F * 5F, mEarthRotationSpeed * 0.41F),
        CelestialBody.Saturn to CelestialBodyInfo(CelestialBody.Saturn, 9F, -26.73F, mEarthRatio * 1.9F, mEarthOrbitSpeed / 29.46F * 5F, mEarthRotationSpeed * 0.45F),
        CelestialBody.Uranus to CelestialBodyInfo(CelestialBody.Uranus, 11F, -97.86F, mEarthRatio * 1.6F, mEarthOrbitSpeed / 84.01F * 5F, mEarthRotationSpeed * 0.72F),
        CelestialBody.Neptune to CelestialBodyInfo(CelestialBody.Neptune, 12.5F, -28.32F, mEarthRatio * 1.6F, mEarthOrbitSpeed / 164.79F * 5F, mEarthRotationSpeed * 0.67F),
    )

    override fun onInit() {
        mSunProgram.init()
        mPlanetProgram.init()
        mRingProgram.init()

        mSunInfo.init()
        mMoonInfo.init()
        mSaturnRingInfo.init()
        mSaturnRingInfo.matrix.scale(1F, 0.8F, 1F)
        for (item in mPlanetInfo) {
            item.value.init()
        }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        GLES20.glClearColor(0F, 0F, 0F, 1F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)


        synchronized(this) {
            for (item in mPlanetInfo) {
                item.value.orbitAndRotation()
            }
            mMoonInfo.orbitAndRotation()
            mSaturnRingInfo.orbitAndRotation()

            updateProjectionMatrix(context)
            when (mEyeTarget) {
                Target.SolarSystem -> updateViewMatrix(mEyePosition, 30F)
                Target.Earth -> {
                    (mPlanetInfo[CelestialBody.Earth]?.position ?: mEyePosition).let {
                        Log.i("jiang", "position ${it[0]} ${it[1]} ${it[2]}")
                        updateViewMatrix(floatArrayOf(it[0], it[1], it[2] + 8F), 8F)
                    }
                }

                Target.Saturn -> {
                    (mPlanetInfo[CelestialBody.Saturn]?.position ?: mEyePosition).let {
                        Log.i("jiang", "position ${it[0]} ${it[1]} ${it[2]}")
                        updateViewMatrix(floatArrayOf(it[0], it[1], it[2] + 8F), 8F)
                    }
                }
            }

            mSunProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * mSunInfo.matrix)
            mSunInfo.updatePosition(mGestureMatrix * mSunInfo.matrix)
            mSunProgram.setTexture(mSunInfo.texture)
            mSunProgram.draw()

            for (item in mPlanetInfo) {
                val planetInfo = item.value
                GLES20.glFrontFace(GLES20.GL_CW)
                mPlanetProgram.setTexture(planetInfo.texture)
                mPlanetProgram.setLightPosition(mSunPosition)
                mPlanetProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * planetInfo.matrix)
                (mGestureMatrix * planetInfo.matrix).let { modelMatrix ->
                    mPlanetProgram.setMMatrix(modelMatrix)
                    planetInfo.updatePosition(modelMatrix)
                }
                mPlanetProgram.setShininess(3F)
                mPlanetProgram.draw()

                if (planetInfo.celestialBody == CelestialBody.Earth) {
                    mPlanetProgram.setTexture(mMoonInfo.texture)
                    mPlanetProgram.setLightPosition(mSunPosition)
                    mPlanetProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * planetInfo.matrix * mMoonInfo.matrix)
                    (mGestureMatrix * planetInfo.matrix * mMoonInfo.matrix).let { modelMatrix ->
                        mPlanetProgram.setMMatrix(modelMatrix)
                        mMoonInfo.updatePosition(modelMatrix)
                    }
                    mPlanetProgram.setShininess(3F)
                    mPlanetProgram.draw()
                } else if (planetInfo.celestialBody == CelestialBody.Saturn) {
                    GLES20.glFrontFace(GLES20.GL_CCW)
                    mRingProgram.setTexture(mSaturnRingInfo.texture)
                    mRingProgram.setLightPosition(mSunPosition)
                    mRingProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * planetInfo.matrix * mSaturnRingInfo.matrix)
                    (mGestureMatrix * planetInfo.matrix * mSaturnRingInfo.matrix).let { modelMatrix ->
                        mRingProgram.setMMatrix(modelMatrix)
                        mSaturnRingInfo.updatePosition(modelMatrix)
                    }
                    mRingProgram.setShininess(1F)
                    mRingProgram.draw()
                }
            }
        }
    }

    override fun onRelease() {
        mSunProgram.release()
        mPlanetProgram.release()
        mRingProgram.release()

        mSunInfo.release()
        mMoonInfo.release()
        mSaturnRingInfo.release()
        for (item in mPlanetInfo) {
            item.value.release()
        }
    }

    private fun updateProjectionMatrix(context: FilterContext) {
        val displaySize = context.displaySize
        if (mDisplaySize.width != displaySize.width || mDisplaySize.height != displaySize.height) {
            val ratio = displaySize.width.toFloat() / displaySize.height.toFloat()
            if (displaySize.width > displaySize.height) {
                mProjectMatrix.setFrustumM(
                    -ratio, ratio,
                    -1F, 1F,
                    5F, 1000F
                )
            } else {
                mProjectMatrix.setFrustumM(
                    -1F, 1F,
                    -ratio, ratio,
                    5F, 1000F
                )
            }
            mDisplaySize = displaySize
        }
    }

    private fun updateViewMatrix(eyePosition: FloatArray, distance: Float) {
        mViewMatrix.setLookAtM(
            eyePosition[0], eyePosition[1], eyePosition[2],
            eyePosition[0], eyePosition[1], eyePosition[2] - distance,
            0F, 1F, 0F
        )
    }

    override fun onUpdateData(updateData: Bundle) = synchronized(this) {
        mXAngle += updateData.getFloat("xAngle", 0F)
        mYAngle += updateData.getFloat("yAngle", 0F)
        mGestureMatrix.reset()
        mGestureMatrix.rotate(mXAngle, 0F, 1F, 0F)
        mGestureMatrix.rotate(mYAngle, 1F, 0F, 0F)

        mRotation += updateData.getFloat("rotation", 0F)
    }

    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            MESSAGE_RUN -> synchronized(this) {
//                for (item in mPlanetInfo) {
//                    item.value.orbitAndRotation()
//                }
//                mMoonInfo.orbitAndRotation()
//                mSaturnRingInfo.orbitAndRotation()
            }

            MESSAGE_TARGET -> synchronized(this) {
//                mEyeSourcePosition = mEyePosition
//                mEyeTargetPosition = when (message.arg1) {
//                    Target.SolarSystem.value -> defaultEyePosition
//                    Target.Earth.value -> {
//                        mPlanetInfo[CelestialBody.Earth]?.position?.get(2)?.let {
//                            Log.i("jiang", "jiang1 ${it}")
//                            floatArrayOf(0F, 0F, it + defaultDistance)
//                        } ?: defaultEyePosition
//                    }
//
//                    Target.Saturn.value -> {
//                        mPlanetInfo[CelestialBody.Saturn]?.position?.get(2)?.let {
//                            floatArrayOf(0F, 0F, it + defaultDistance)
//                        } ?: defaultEyePosition
//                    }
//
//                    else -> defaultEyePosition
//                }
//                val progress = message.obj as? Float ?: 0F
//                mEyePosition[0] = (mEyeTargetPosition[0] - mEyeSourcePosition[0]) * progress + mEyeSourcePosition[0]
//                mEyePosition[1] = (mEyeTargetPosition[1] - mEyeSourcePosition[1]) * progress + mEyeSourcePosition[1]
//                mEyePosition[2] = (mEyeTargetPosition[2] - mEyeSourcePosition[2]) * progress + mEyeSourcePosition[2]
//                Log.i("jiang", "jiang=${progress} ${mEyePosition[0]} ${mEyePosition[1]} ${mEyePosition[2]} ${message.arg1}")
//                updateViewMatrix(mEyePosition)

                mEyeTarget = when (message.arg1) {
                    Target.SolarSystem.value -> Target.SolarSystem
                    Target.Earth.value -> Target.Earth
                    Target.Saturn.value -> Target.Saturn
                    else -> Target.SolarSystem
                }
            }
        }
    }

    companion object {
        private val defaultEyePosition = floatArrayOf(0F, 0F, 30F)
        private val defaultDistance = 30F
    }
}