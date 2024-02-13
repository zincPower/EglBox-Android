package com.jiangpengyong.eglbox.filter

import com.jiangpengyong.eglbox.GLTexture

/**
 * @author jiang peng yong
 * @date 2024/2/11 22:34
 * @email 56002982@qq.com
 * @des 滤镜上下文
 */
class FilterContext {

    // 可渲染尺寸
    var width = 0
    var height = 0

    // 纹理列表（如果上一个滤镜节点有多个纹理，则会有多个纹理输入，否则只有一个纹理）
    val textures = ArrayList<GLTexture>()

    // 当只有一个纹理时，简便方法，获取列表的第一个纹理
    val texture: GLTexture?
        get() = textures.firstOrNull()

    // 是否是合法的尺寸
    fun isValidSize() = width > 0 && height > 0
}