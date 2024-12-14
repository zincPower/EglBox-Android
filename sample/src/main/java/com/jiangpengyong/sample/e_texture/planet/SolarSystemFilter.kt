package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.blend
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.space3d.Space3D
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.program.LightProgram
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

    // 视点矩阵
    private val mViewMatrix = ViewMatrix()

    // 绘制程序
    private val mLightProgram = LightProgram()
    private val mSunProgram = SunProgram()
    private val mPlanetProgram = PlanetProgram()
    private val mEarthProgram = EarthProgram()
    private val mOrbitProgram = OrbitProgram()
    private val mEarthCloudProgram = EarthCloudProgram()

    private lateinit var mRingModelData: ModelData

    // 地球信息，作为其他天体基准
    private var mEarthRatio = 1 / 3F
    private var mEarthOrbitSpeed = 1 / 2F
    private var mEarthRotationSpeed = 1F

    // 光源
    private var mSunPoint = Point(0F, 0F, 0F)

    // 观察位置和转换过程
    private var mCurrentViewPoint = SOLAR_VIEW_POINT
    private var mTargetViewPoint = mCurrentViewPoint
    private var mSourceViewPoint = mCurrentViewPoint
    private var mProgress = 0F

    // 观察目标
    private var mEyeTarget = Target.SolarSystem

    // 轨道矩阵
    private val mOrbitModelMatrix = ModelMatrix()

    // 天体信息
    private val mMoonInfo = CelestialBodyInfo(CelestialBody.Moon, 2F, -0.5F, 0.25F, mEarthOrbitSpeed * 4F, mEarthOrbitSpeed * 4F)
    private val mSunInfo = CelestialBodyInfo(CelestialBody.Sun, 0F, 0F, 1.5F, 0F, 0F)
    private val mSaturnRingInfo = CelestialBodyInfo(CelestialBody.SaturnRing, 0F, -27F, 1.2F, 0.12F, 1.2F, 0F, mEarthRotationSpeed * 2F)
    private val mEarthCloudInfo = CelestialBodyInfo(CelestialBody.EarthCloud, 6.5F, -23.44F, mEarthRatio * 1.1F, mEarthOrbitSpeed, mEarthRotationSpeed * 1 / 3F)
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
        mLightProgram.init()
        mOrbitProgram.init()
        mEarthCloudProgram.init()

        mRingModelData = ModelCreator.createRing(majorSegment = 36)

        mSunInfo.init()
        mMoonInfo.init()
        mSaturnRingInfo.init()
        mEarthCloudInfo.init()
        for (item in mPlanetInfo) {
            item.value.init()
        }

        GLES20.glLineWidth(SizeUtils.dp2px(0.5F))
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        val fbo = context.getTexFBO(texture.width, texture.height, DepthType.Texture)
        updateCelestialData(context.space3D)
        updateViewMatrix()
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
        mEarthProgram.release()
        mLightProgram.release()
        mOrbitProgram.release()
        mEarthCloudProgram.release()

        mSunInfo.release()
        mMoonInfo.release()
        mSaturnRingInfo.release()
        mEarthCloudInfo.release()
        for (item in mPlanetInfo) {
            item.value.release()
        }
    }

    /**
     * 更新所有天体的数据
     */
    private fun updateCelestialData(space3D: Space3D) {
        mSunInfo.setOutsideMatrix(space3D.gestureMatrix)
        mEarthCloudInfo.setOutsideMatrix(space3D.gestureMatrix)
        for (planet in mPlanetInfo) {
            planet.value.setOutsideMatrix(space3D.gestureMatrix)
            when (planet.key) {
                CelestialBody.Earth -> mMoonInfo.setOutsideMatrix(planet.value.modelMatrix)
                CelestialBody.Saturn -> mSaturnRingInfo.setOutsideMatrix(planet.value.modelMatrix)
                else -> {}  // nothing to do
            }
        }
    }

    /**
     * 更新视图矩阵
     */
    private fun updateViewMatrix() {
        val distance = if (mEyeTarget == Target.SolarSystem) {
            SOLAR_DISTANCE
        } else {
            PLANET_DISTANCE
        }.let { distance ->
            distance / (mContext?.space3D?.scale?.scaleX ?: 1F)
        }

        when (mEyeTarget) {
            Target.SolarSystem -> Point(0F, 0F, distance)
            Target.Mercury -> (mPlanetInfo[CelestialBody.Mercury]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
            Target.Venus -> (mPlanetInfo[CelestialBody.Venus]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
            Target.Earth -> (mPlanetInfo[CelestialBody.Earth]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
            Target.Mars -> (mPlanetInfo[CelestialBody.Mars]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
            Target.Jupiter -> (mPlanetInfo[CelestialBody.Jupiter]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
            Target.Saturn -> (mPlanetInfo[CelestialBody.Saturn]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
            Target.Uranus -> (mPlanetInfo[CelestialBody.Uranus]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
            Target.Neptune -> (mPlanetInfo[CelestialBody.Neptune]?.position ?: mCurrentViewPoint).let { Point(it.x, it.y, it.z + distance) }
        }.let { viewPoint ->
            mTargetViewPoint = viewPoint
            if (mProgress <= 0F) {
                mTargetViewPoint
            } else if (mProgress >= 1F) {
                mSourceViewPoint
            } else {
                val x = mTargetViewPoint.x + (mSourceViewPoint.x - mTargetViewPoint.x) * mProgress
                val y = mTargetViewPoint.y + (mSourceViewPoint.y - mTargetViewPoint.y) * mProgress
                val z = mTargetViewPoint.z + (mSourceViewPoint.z - mTargetViewPoint.z) * mProgress
                Point(x, y, z)
            }
        }.let { viewPoint ->
            mCurrentViewPoint = viewPoint
            mViewMatrix.setLookAtM(
                viewPoint.x, viewPoint.y, viewPoint.z,
                viewPoint.x, viewPoint.y, viewPoint.z - distance,
                0F, 1F, 0F
            )
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
        mEarthCloudInfo.orbitAndRotation()
    }

    /**
     * 绘制太阳
     */
    private fun drawSun() {
        val space3D = mContext?.space3D ?: return
        val vpMatrix = space3D.projectionMatrix * mViewMatrix
        mSunProgram.setMVPMatrix(vpMatrix * mSunInfo.modelMatrix)
        mSunProgram.setTexture(mSunInfo.texture)
        mSunProgram.draw()
    }

    /**
     * 绘制行星
     */
    private fun drawPlanet() {
        val space3D = mContext?.space3D ?: return
        val vpMatrix = space3D.projectionMatrix * mViewMatrix

        for (item in mPlanetInfo) {
            val planetInfo = item.value
            drawOrbit(planetInfo, vpMatrix, space3D.gestureMatrix)

            if (planetInfo.celestialBody == CelestialBody.Earth) {
                drawEarth(planetInfo, vpMatrix)
                blend(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA) {
                    drawEarthCloud(vpMatrix)
                }
                drawMoon(vpMatrix)
            } else {
                val modelMatrix = planetInfo.modelMatrix
                mPlanetProgram.setModelMatrix(modelMatrix)
                mPlanetProgram.setMVPMatrix(vpMatrix * modelMatrix)
                mPlanetProgram.setTexture(planetInfo.texture)
                mPlanetProgram.setLightPoint(mSunPoint)
                mPlanetProgram.setShininess(3F)
                mPlanetProgram.draw()
                if (planetInfo.celestialBody == CelestialBody.Saturn) {
                    drawSaturnRing(vpMatrix)
                }
            }
        }
    }

    /**
     * 绘制行星轨道
     */
    private fun drawOrbit(planetInfo: CelestialBodyInfo, vpMatrix: GLMatrix, gestureMatrix: GLMatrix) {
        mOrbitModelMatrix.reset()
        mOrbitModelMatrix.apply {
            rotate(90F, -1F, 0F, 0F)
            scale(planetInfo.tranX, planetInfo.tranX, 1F)
        }
        mOrbitProgram.setMVPMatrix(vpMatrix * gestureMatrix * mOrbitModelMatrix)
        mOrbitProgram.draw()
    }

    /**
     * 绘制月亮
     */
    private fun drawMoon(vpMatrix: GLMatrix) {
        val modelMatrix = mMoonInfo.modelMatrix
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
    private fun drawSaturnRing(vpMatrix: GLMatrix) {
        val modelMatrix = mSaturnRingInfo.modelMatrix
        mLightProgram.setModelData(mRingModelData)
        mLightProgram.setModelMatrix(modelMatrix)
        mLightProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mLightProgram.setTexture(mSaturnRingInfo.texture)
        mLightProgram.setLightPoint(mSunPoint)
        mLightProgram.setShininess(1F)
        mLightProgram.draw()
    }

    /**
     * 绘制地球
     */
    private fun drawEarth(planetInfo: CelestialBodyInfo, vpMatrix: GLMatrix) {
        val modelMatrix = planetInfo.modelMatrix
        mEarthProgram.setModelMatrix(modelMatrix)
        mEarthProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mEarthProgram.setDayTexture(planetInfo.texture)
        mEarthProgram.setLightPoint(mSunPoint)
        mEarthProgram.setShininess(3F)
        mEarthProgram.draw()
    }

    /**
     * 绘制地球
     */
    private fun drawEarthCloud(vpMatrix: GLMatrix) {
        val modelMatrix = mEarthCloudInfo.modelMatrix
        mEarthCloudProgram.setModelMatrix(modelMatrix)
        mEarthCloudProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mEarthCloudProgram.setLightPoint(mSunPoint)
        mEarthCloudProgram.setShininess(10F)
        mEarthCloudProgram.setTexture(mEarthCloudInfo.texture)
        mEarthCloudProgram.draw()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            SolarSystemMessageType.CHANGE_TARGET -> {
                mSourceViewPoint = mCurrentViewPoint
                mEyeTarget = when (message.arg1) {
                    Target.SolarSystem.value -> Target.SolarSystem
                    Target.Mercury.value -> Target.Mercury
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

            SolarSystemMessageType.UPDATE_TRANSITION_ANIMATION -> {
                mProgress = message.obj as? Float ?: return
            }

            SolarSystemMessageType.UPDATE_ORBIT_AND_ROTATION -> {
                updateOrbitAndRotation()
            }
        }
    }

    companion object {
        const val TAG = "SolarSystemFilter"
        private const val SOLAR_DISTANCE = 40F
        private const val PLANET_DISTANCE = 12F
        private val SOLAR_VIEW_POINT = Point(0F, 0F, SOLAR_DISTANCE)
    }
}

object SolarSystemMessageType {
    const val CHANGE_TARGET = 1_000
    const val UPDATE_TRANSITION_ANIMATION = 1_001
    const val UPDATE_ORBIT_AND_ROTATION = 1_002
}