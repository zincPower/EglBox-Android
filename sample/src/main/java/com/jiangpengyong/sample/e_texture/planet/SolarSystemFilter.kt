package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.program.RingProgram
import com.jiangpengyong.sample.utils.SizeUtils

/**
 * @author jiang peng yong
 * @date 2024/11/27 08:05
 * @email 56002982@qq.com
 * @des 太阳系
 */
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
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix().apply {
        rotate(45F, 0F, 1F, 0F)
        rotate(30F, 1F, 0F, 0F)
    }

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
    private var mSunPoint = Point(0F, 0F, 0F)

    // 观察位置和转换过程
    private var mEyePoint = Point(0F, 0F, 30F)
    private var mEyeTargetPoint = mEyePoint
    private var mEyeSourcePoint = mEyePoint
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

    override fun onInit(context: FilterContext) {
        mSunProgram.init()
        mPlanetProgram.init()
        mEarthProgram.init()
        mRingProgram.init()
        mOrbitProgram.init()

        mSunInfo.init()
        mMoonInfo.init()
        mSaturnRingInfo.init()
        for (item in mPlanetInfo) {
            item.value.init()
        }

        GLES20.glLineWidth(SizeUtils.dp2px(0.5F))
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        updateViewMatrix()
        val fbo = context.getTexFBO(texture.width, texture.height, DepthType.Texture)
        fbo.use {
            GLES20.glClearColor(0F, 0F, 0F, 1F)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            drawSun()
            drawPlanet()
            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
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
     * 更新视图矩阵
     */
    private fun updateViewMatrix() {
        when (mEyeTarget) {
            Target.SolarSystem -> DEFAULT_EYE_POSITION
            Target.Mercury -> (mPlanetInfo[CelestialBody.Mercury]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
            Target.Venus -> (mPlanetInfo[CelestialBody.Venus]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
            Target.Earth -> (mPlanetInfo[CelestialBody.Earth]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
            Target.Mars -> (mPlanetInfo[CelestialBody.Mars]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
            Target.Jupiter -> (mPlanetInfo[CelestialBody.Jupiter]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
            Target.Saturn -> (mPlanetInfo[CelestialBody.Saturn]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
            Target.Uranus -> (mPlanetInfo[CelestialBody.Uranus]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
            Target.Neptune -> (mPlanetInfo[CelestialBody.Neptune]?.position ?: mEyePoint).let { Point(it.x, it.y, it.z + DEFAULT_DISTANCE) }
        }.let { eyeTargetPosition ->
            mEyeTargetPoint = eyeTargetPosition
            if (mProgress < 0F || mProgress >= 1F) {
                mEyeTargetPoint
            } else {
                val x = (mEyeTargetPoint.x - mEyeSourcePoint.x) * mProgress + mEyeSourcePoint.x
                val y = (mEyeTargetPoint.y - mEyeSourcePoint.y) * mProgress + mEyeSourcePoint.y
                val z = (mEyeTargetPoint.z - mEyeSourcePoint.z) * mProgress + mEyeSourcePoint.z
                Point(x, y, z)
            }.let { eyePosition ->
                mEyePoint = eyePosition
                mViewMatrix.setLookAtM(
                    eyePosition.x, eyePosition.y, eyePosition.z,
                    eyePosition.x, eyePosition.y, eyePosition.z - DEFAULT_DISTANCE,
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
     * 绘制太阳
     */
    private fun drawSun() {
        val space3D = mContext?.space3D ?: return
        val vpMatrix = space3D.projectionMatrix * mViewMatrix
        val modelMatrix = space3D.gestureMatrix * mModelMatrix * mSunInfo.matrix
        mSunInfo.updatePosition(modelMatrix)
        mSunProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mSunProgram.setTexture(mSunInfo.texture)
        mSunProgram.draw()
    }

    /**
     * 绘制行星
     */
    private fun drawPlanet() {
        val space3D = mContext?.space3D ?: return
        val vpMatrix = space3D.projectionMatrix * mViewMatrix
        val commonModelMatrix = space3D.gestureMatrix * mModelMatrix
        for (item in mPlanetInfo) {
            val planetInfo = item.value
            drawOrbit(planetInfo, vpMatrix, commonModelMatrix)

            if (planetInfo.celestialBody == CelestialBody.Earth) {
                drawEarth(planetInfo, vpMatrix, commonModelMatrix)
                drawMoon(vpMatrix, commonModelMatrix, planetInfo.matrix)
            } else {
                val modelMatrix = commonModelMatrix * planetInfo.matrix
                planetInfo.updatePosition(modelMatrix)
                mPlanetProgram.setModelMatrix(modelMatrix)
                mPlanetProgram.setMVPMatrix(vpMatrix * modelMatrix)
                mPlanetProgram.setTexture(planetInfo.texture)
                mPlanetProgram.setLightPoint(mSunPoint)
                mPlanetProgram.setShininess(3F)
                mPlanetProgram.draw()
                if (planetInfo.celestialBody == CelestialBody.Saturn) {
                    drawSaturnRing(vpMatrix, commonModelMatrix, planetInfo.matrix)
                }
            }
        }
    }

    /**
     * 绘制行星轨道
     */
    private fun drawOrbit(planetInfo: CelestialBodyInfo, vpMatrix: GLMatrix, commonModelMatrix: GLMatrix) {
        mOrbitModelMatrix.reset()
        mOrbitModelMatrix.apply {
            rotate(90F, -1F, 0F, 0F)
            scale(planetInfo.tranX, planetInfo.tranX, 1F)
        }
        mOrbitProgram.setMVPMatrix(vpMatrix * commonModelMatrix * mOrbitModelMatrix)
        mOrbitProgram.draw()
    }

    /**
     * 绘制月亮
     */
    private fun drawMoon(vpMatrix: GLMatrix, commonModelMatrix: GLMatrix, earthMatrix: GLMatrix) {
        val modelMatrix = commonModelMatrix * earthMatrix * mMoonInfo.matrix
        mMoonInfo.updatePosition(modelMatrix)
        mPlanetProgram.setModelMatrix(modelMatrix)
        mPlanetProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mPlanetProgram.setTexture(mMoonInfo.texture)
        mPlanetProgram.setLightPoint(mSunPoint)
        mPlanetProgram.setShininess(3F)
        mPlanetProgram.draw()
    }

    /**
     * 绘制土星环
     */
    private fun drawSaturnRing(vpMatrix: GLMatrix, commonModelMatrix: GLMatrix, saturnMatrix: ModelMatrix) {
        val modelMatrix = commonModelMatrix * saturnMatrix * mSaturnRingInfo.matrix
        mSaturnRingInfo.updatePosition(modelMatrix)
        mRingProgram.setModelMatrix(modelMatrix)
        mRingProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mRingProgram.setTexture(mSaturnRingInfo.texture)
        mRingProgram.setLightPoint(mSunPoint)
        mRingProgram.setShininess(1F)
        mRingProgram.draw()
    }

    /**
     * 绘制地球
     */
    private fun drawEarth(planetInfo: CelestialBodyInfo, vpMatrix: GLMatrix, commonModelMatrix: GLMatrix) {
        val modelMatrix = commonModelMatrix * planetInfo.matrix
        planetInfo.updatePosition(modelMatrix)
        mEarthProgram.setModelMatrix(modelMatrix)
        mEarthProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mEarthProgram.setDayTexture(planetInfo.texture)
        mEarthProgram.setLightPoint(mSunPoint)
        mEarthProgram.setShininess(3F)
        mEarthProgram.draw()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            SolarSystemMessageType.CHANGE_TARGET -> {
                mProgress = message.obj as? Float ?: return
                mEyeSourcePoint = mEyePoint
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

            SolarSystemMessageType.UPDATE_ORBIT_AND_ROTATION -> {
                updateOrbitAndRotation()
            }
        }
    }

    companion object {
        const val TAG = "SolarSystemFilter"
        private const val DEFAULT_DISTANCE = 8F
        private val DEFAULT_EYE_POSITION = Point(0F, 0F, 30F)
    }
}

object SolarSystemMessageType {
    const val CHANGE_TARGET = 1_000
    const val UPDATE_ORBIT_AND_ROTATION = 1_001
}