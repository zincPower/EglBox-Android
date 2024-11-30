package com.jiangpengyong.sample.e_texture.planet

import android.graphics.BitmapFactory
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.sample.App
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/8/17 11:12
 * @email 56002982@qq.com
 * @des 八大行星
 */
enum class CelestialBody(val textureFile: String) {
    Mercury("images/celestial_body/2k_mercury.jpg"),                 // 水星
    Venus("images/celestial_body/2k_venus_surface.jpg"),             // 金星
    Earth("images/celestial_body/2k_earth_daymap.jpg"),              // 地球
    Mars("images/celestial_body/2k_mars.jpg"),                       // 火星
    Jupiter("images/celestial_body/2k_jupiter.jpg"),                 // 木星
    Saturn("images/celestial_body/2k_saturn.jpg"),                   // 土星
    Uranus("images/celestial_body/2k_uranus.jpg"),                   // 天王星
    Neptune("images/celestial_body/2k_neptune.jpg"),                 // 海王星
    Moon("images/celestial_body/2k_moon.jpg"),                       // 月亮
    Sun("images/celestial_body/2k_sun.jpg"),                         // 太阳
    SaturnRing("images/celestial_body/2k_saturn_ring_alpha.png"),    // 土星环
}

/**
 * @author jiang peng yong
 * @date 2024/8/17 11:12
 * @email 56002982@qq.com
 * @des 行星数据
 */
data class CelestialBodyInfo(
    val celestialBody: CelestialBody,
    val tranX: Float,
    val angle: Float,
    val scaleX: Float,
    val scaleY: Float,
    val scaleZ: Float,
    var orbitSpeed: Float,
    var rotationSpeed: Float
) {
    constructor(
        celestialBody: CelestialBody,
        tranX: Float,
        angle: Float,
        scale: Float,
        orbitSpeed: Float,
        rotationSpeed: Float
    ) : this(celestialBody, tranX, angle, scale, scale, scale, orbitSpeed, rotationSpeed)

    // 自身矩阵，不考虑外部旋转
    private val selfMatrix = ModelMatrix()

    // 外部矩阵，作用于该天体
    private var outsideMatrix = GLMatrix()

    // 最终天体矩阵
    val modelMatrix: GLMatrix
        get() {
            return outsideMatrix * selfMatrix
        }

    // 天体纹理
    val texture = GLTexture()

    // 获取最终位置
    val position: Point
        get() {
            return modelMatrix * originalPoint
        }

    // 公转
    private var mOrbit = 0F

    // 自转
    private var mRotation = 0F

    fun init() {
        texture.init()
        BitmapFactory.decodeFile(File(App.context.filesDir, celestialBody.textureFile).absolutePath).apply {
            texture.setData(this)
            recycle()
        }
        updateMatrix()
    }

    fun release() {
        texture.release()
    }

    fun orbitAndRotation() {
        mOrbit = (mOrbit + orbitSpeed) % 360
        mRotation = (mRotation + rotationSpeed) % 360
        updateMatrix()
    }

    fun setOutsideMatrix(matrix: GLMatrix) {
        outsideMatrix = matrix
    }

    private fun updateMatrix() {
        selfMatrix.apply {
            reset()
            rotate(mOrbit, 0F, 1F, 0F)
            translate(tranX, 0F, 0F)
            rotate(angle, 0F, 0F, 1F)
            rotate(mRotation, 0F, 1F, 0F)
            scale(scaleX, scaleY, scaleZ)
        }
    }

    companion object {
        private val originalPoint = Point(0F, 0F, 0F)
    }
}