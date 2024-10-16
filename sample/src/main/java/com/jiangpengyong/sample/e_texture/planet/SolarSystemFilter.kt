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
import com.jiangpengyong.sample.e_texture.planet.SolarSystemActivity.Companion.MESSAGE_TARGET
import com.jiangpengyong.sample.utils.SizeUtils

class SolarSystemFilter : GLFilter() {
    enum class Target(val value: Int) {
        SolarSystem(0),
        Mercury(1),
        Venus(2),
        Earth(3),
        Mars(4),
        Jupiter(5),
        Saturn(6),
        Uranus(7),
        Neptune(8)
    }

    // 矩阵
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mGestureMatrix = ModelMatrix()

    // 手势旋转角度
    private var mXAngle = 45F
    private var mYAngle = 30F

    // 屏幕尺寸
    private var mPreviewSize = Size(0, 0)

    // 绘制程序
    private val mSunProgram = SunProgram()
    private val mPlanetProgram = PlanetProgram()
    private val mEarthProgram = EarthProgram()
    private val mRingProgram = RingProgram()
    private val mOrbitProgram = OrbitProgram()

    // 地球信息，作为其他天体基准
    private var mEarthRatio = 1 / 3F
    private var mEarthOrbitSpeed = 1 / 2F
    private var mEarthRotationSpeed = 1F

    // 光源
    private var mSunPosition = floatArrayOf(0F, 0F, 0F)

    // 观察位置和转换过程
    private var mEyePosition = floatArrayOf(0F, 0F, 30F)
    private var mEyeTargetPosition = mEyePosition
    private var mEyeSourcePosition = mEyePosition
    private var mProgress = 0F

    // 观察目标
    private var mEyeTarget = Target.SolarSystem

    private val mOrbitModelMatrix = ModelMatrix()

    // 天体信息
    private val mMoonInfo = CelestialBodyInfo(CelestialBody.Moon, 2F, -0.5F, 0.25F, mEarthOrbitSpeed * 4F, mEarthOrbitSpeed * 4F)
    private val mSunInfo = CelestialBodyInfo(CelestialBody.Sun, 0F, 0F, 1.5F, 0F, 0F)
    private val mSaturnRingInfo = CelestialBodyInfo(CelestialBody.SaturnRing, 0F, -27F, 1.2F, 0F, mEarthRotationSpeed * 2F)
    private val mPlanetInfo = mapOf(
        CelestialBody.Mercury to CelestialBodyInfo(CelestialBody.Mercury, 2.5F, -0.034F, mEarthRatio * 0.5F, mEarthOrbitSpeed / 0.24F, mEarthRotationSpeed * 2F),
        CelestialBody.Venus to CelestialBodyInfo(CelestialBody.Venus, 4.5F, -177.4F, mEarthRatio * 0.949F, mEarthOrbitSpeed / 0.62F, mEarthRotationSpeed * 1.2F),
        CelestialBody.Earth to CelestialBodyInfo(CelestialBody.Earth, 6.5F, -23.44F, mEarthRatio, mEarthOrbitSpeed, mEarthRotationSpeed),
        CelestialBody.Mars to CelestialBodyInfo(CelestialBody.Mars, 9F, -25.19F, mEarthRatio * 0.8F, mEarthOrbitSpeed / 1.88F, mEarthRotationSpeed),
        CelestialBody.Jupiter to CelestialBodyInfo(CelestialBody.Jupiter, 12F, -3.13F, mEarthRatio * 2F, mEarthOrbitSpeed / 11.86F * 5F, mEarthRotationSpeed * 0.41F),
        CelestialBody.Saturn to CelestialBodyInfo(CelestialBody.Saturn, 16F, -26.73F, mEarthRatio * 1.9F, mEarthOrbitSpeed / 29.46F * 5F, mEarthRotationSpeed * 0.45F),
        CelestialBody.Uranus to CelestialBodyInfo(CelestialBody.Uranus, 20F, -97.86F, mEarthRatio * 1.6F, mEarthOrbitSpeed / 84.01F * 5F, mEarthRotationSpeed * 0.72F),
        CelestialBody.Neptune to CelestialBodyInfo(CelestialBody.Neptune, 25F, -28.32F, mEarthRatio * 1.6F, mEarthOrbitSpeed / 164.79F * 5F, mEarthRotationSpeed * 0.67F),
    )

    override fun onInit() {
        mSunProgram.init()
        mPlanetProgram.init()
        mEarthProgram.init()
        mRingProgram.init()
        mOrbitProgram.apply {
            setAngleSpan(1)
            init()
        }

        mSunInfo.init()
        mMoonInfo.init()
        mSaturnRingInfo.init()
        for (item in mPlanetInfo) {
            item.value.init()
        }

        GLES20.glLineWidth(SizeUtils.dp2px(0.5F).toFloat())
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) = synchronized(this) {
        GLES20.glClearColor(0F, 0F, 0F, 1F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        updateGestureMatrix()
        updateOrbitAndRotation()
        updateProjectionMatrix(context)
        updateViewMatrix()

        drawSun()
        drawPlanet()
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

    /**
     * 更新投影矩阵
     */
    private fun updateProjectionMatrix(context: FilterContext) {
        val previewSize = context.previewSize
        if (mPreviewSize.width != previewSize.width || mPreviewSize.height != previewSize.height) {
            val ratio = previewSize.width.toFloat() / previewSize.height.toFloat()
            if (previewSize.width > previewSize.height) {
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
            mPreviewSize = previewSize
        }
    }

    /**
     * 更新视图矩阵
     */
    private fun updateViewMatrix() {
        when (mEyeTarget) {
            Target.SolarSystem -> defaultEyePosition
            Target.Mercury -> (mPlanetInfo[CelestialBody.Mercury]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
            Target.Venus -> (mPlanetInfo[CelestialBody.Venus]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
            Target.Earth -> (mPlanetInfo[CelestialBody.Earth]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
            Target.Mars -> (mPlanetInfo[CelestialBody.Mars]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
            Target.Jupiter -> (mPlanetInfo[CelestialBody.Jupiter]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
            Target.Saturn -> (mPlanetInfo[CelestialBody.Saturn]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
            Target.Uranus -> (mPlanetInfo[CelestialBody.Uranus]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
            Target.Neptune -> (mPlanetInfo[CelestialBody.Neptune]?.position ?: mEyePosition).let { floatArrayOf(it[0], it[1], it[2] + DEFAULT_DISTANCE) }
        }.let { eyeTargetPosition ->
            mEyeTargetPosition = eyeTargetPosition
            if (mProgress < 0F || mProgress >= 1F) {
                mEyeTargetPosition
            } else {
                val x = (mEyeTargetPosition[0] - mEyeSourcePosition[0]) * mProgress + mEyeSourcePosition[0]
                val y = (mEyeTargetPosition[1] - mEyeSourcePosition[1]) * mProgress + mEyeSourcePosition[1]
                val z = (mEyeTargetPosition[2] - mEyeSourcePosition[2]) * mProgress + mEyeSourcePosition[2]
                floatArrayOf(x, y, z)
            }.let { eyePosition ->
                mEyePosition = eyePosition
                mViewMatrix.setLookAtM(
                    eyePosition[0], eyePosition[1], eyePosition[2],
                    eyePosition[0], eyePosition[1], eyePosition[2] - DEFAULT_DISTANCE,
                    0F, 1F, 0F
                )
            }
        }
    }

    /**
     * 更新天体公转和自转
     */
    private fun updateOrbitAndRotation() {
        for (item in mPlanetInfo) {
            item.value.orbitAndRotation()
        }
        mMoonInfo.orbitAndRotation()
        mSaturnRingInfo.orbitAndRotation()
        mSaturnRingInfo.matrix.scale(1F, 0.1F, 1F)
    }

    /**
     * 更新手势旋转矩阵
     */
    private fun updateGestureMatrix() {
        mGestureMatrix.reset()
        mGestureMatrix.rotate(mXAngle, 0F, 1F, 0F)
        mGestureMatrix.rotate(mYAngle, 1F, 0F, 0F)
    }

    /**
     * 绘制太阳
     */
    private fun drawSun() {
        mSunProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * mSunInfo.matrix)
        mSunInfo.updatePosition(mGestureMatrix * mSunInfo.matrix)
        mSunProgram.setTexture(mSunInfo.texture)
        mSunProgram.draw()
    }

    /**
     * 绘制行星
     */
    private fun drawPlanet() {
        for (item in mPlanetInfo) {
            val planetInfo = item.value

            drawOrbit(planetInfo)

            if (planetInfo.celestialBody == CelestialBody.Earth) {
                drawEarth(planetInfo)
                drawMoon(planetInfo.matrix)
            } else {
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
                if (planetInfo.celestialBody == CelestialBody.Saturn) {
                    drawSaturnRing(planetInfo.matrix)
                }
            }
        }
    }

    /**
     * 绘制行星轨道
     */
    private fun drawOrbit(planetInfo: CelestialBodyInfo) {
        mOrbitModelMatrix.reset()
        mOrbitModelMatrix.apply {
            rotate(90F, -1F, 0F, 0F)
            scale(planetInfo.tranX, planetInfo.tranX, 1F)
        }
        mOrbitProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * mOrbitModelMatrix)
        mOrbitProgram.draw()
    }

    /**
     * 绘制月亮
     */
    private fun drawMoon(matrix: GLMatrix) {
        mPlanetProgram.setTexture(mMoonInfo.texture)
        mPlanetProgram.setLightPosition(mSunPosition)
        mPlanetProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * matrix * mMoonInfo.matrix)
        (mGestureMatrix * matrix * mMoonInfo.matrix).let { modelMatrix ->
            mPlanetProgram.setMMatrix(modelMatrix)
            mMoonInfo.updatePosition(modelMatrix)
        }
        mPlanetProgram.setShininess(3F)
        mPlanetProgram.draw()
    }

    /**
     * 绘制土星环
     */
    private fun drawSaturnRing(matrix: GLMatrix) {
        GLES20.glFrontFace(GLES20.GL_CCW)
        mRingProgram.setTexture(mSaturnRingInfo.texture)
        mRingProgram.setLightPosition(mSunPosition)
        mRingProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * matrix * mSaturnRingInfo.matrix)
        (mGestureMatrix * matrix * mSaturnRingInfo.matrix).let { modelMatrix ->
            mRingProgram.setMMatrix(modelMatrix)
            mSaturnRingInfo.updatePosition(modelMatrix)
        }
        mRingProgram.setShininess(1F)
        mRingProgram.draw()
    }

    /**
     * 绘制地球
     */
    private fun drawEarth(planetInfo: CelestialBodyInfo) {
        mEarthProgram.setDayTexture(planetInfo.texture)
        mEarthProgram.setLightPosition(mSunPosition)
        mEarthProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mGestureMatrix * planetInfo.matrix)
        (mGestureMatrix * planetInfo.matrix).let { modelMatrix ->
            mEarthProgram.setMMatrix(modelMatrix)
            planetInfo.updatePosition(modelMatrix)
        }
        mEarthProgram.setShininess(3F)
        mEarthProgram.draw()
    }

    override fun onUpdateData(updateData: Bundle) = synchronized(this) {
        mXAngle += updateData.getFloat("xAngle", 0F)
        mYAngle += updateData.getFloat("yAngle", 0F)
    }

    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            MESSAGE_TARGET -> synchronized(this) {
                mProgress = message.obj as? Float ?: return@synchronized
                mEyeSourcePosition = mEyePosition
                mEyeTarget = when (message.arg1) {
                    Target.SolarSystem.value -> Target.SolarSystem
                    Target.Mercury.value -> Target.Mercury
                    Target.SolarSystem.value -> Target.SolarSystem
                    Target.Venus.value -> Target.Venus
                    Target.Earth.value -> Target.Earth
                    Target.Mars.value -> Target.Mars
                    Target.Jupiter.value -> Target.Jupiter
                    Target.Saturn.value -> Target.Saturn
                    Target.Uranus.value -> Target.Uranus
                    Target.Neptune.value -> Target.Neptune
                    else -> Target.SolarSystem
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_DISTANCE = 8F
        private val defaultEyePosition = floatArrayOf(0F, 0F, 30F)
    }
}