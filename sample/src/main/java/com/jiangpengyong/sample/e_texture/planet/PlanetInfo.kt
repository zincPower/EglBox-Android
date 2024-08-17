package com.jiangpengyong.sample.e_texture.planet

import android.graphics.BitmapFactory
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.sample.App
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/8/17 11:12
 * @email 56002982@qq.com
 * @des 八大行星
 */
enum class PlanetType(val textureFile: String) {
    Mercury("images/heavenly_body/2k_mercury.jpg"),    // 水星
    Venus("images/heavenly_body/2k_venus_surface.jpg"),      // 金星
    Earth("images/heavenly_body/2k_earth_daymap.jpg"),      // 地球
    Mars("images/heavenly_body/2k_mars.jpg"),       // 火星
    Jupiter("images/heavenly_body/2k_jupiter.jpg"),    // 木星
    Saturn("images/heavenly_body/2k_saturn.jpg"),     // 土星
    Uranus("images/heavenly_body/2k_uranus.jpg"),     // 天王星
    Neptune("images/heavenly_body/2k_neptune.jpg"),    // 海王星
}

/**
 * @author jiang peng yong
 * @date 2024/8/17 11:12
 * @email 56002982@qq.com
 * @des 行星数据
 */
data class PlanetInfo(
    val type: PlanetType,
    val tranX: Float,
    val angle: Float,
    val scale: Float,
    var orbitSpeed: Float
) {
    val matrix = ModelMatrix()
    val texture = GLTexture()
    private var mOrbit = 0F

    fun init() {
        texture.init()
        BitmapFactory.decodeFile(File(App.context.filesDir, type.textureFile).absolutePath).apply {
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
        updateMatrix()
    }

    private fun updateMatrix() {
        matrix.apply {
            reset()
            rotate(mOrbit, 0F, 1F, 0F)
            translate(tranX, 0F, 0F)
            rotate(angle, 0F, 0F, 1F)
            scale(scale, scale, scale)
        }
    }
}