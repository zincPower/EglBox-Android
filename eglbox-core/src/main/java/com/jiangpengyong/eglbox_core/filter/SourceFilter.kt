package com.jiangpengyong.eglbox_core.filter

import com.jiangpengyong.eglbox_core.gles.GLTexture

/**
 * @author jiang peng yong
 * @date 2024/7/18 12:48
 * @email 56002982@qq.com
 * @des 滤镜源
 */
abstract class SourceFilter : GLFilter() {
    private var mImage: ImageInOut = ImageInOut()
    private var mTexture: GLTexture? = null

    fun getImage(): ImageInOut {
        mTexture?.let { mImage.out(it) }
        return mImage
    }

    protected fun updateTexture(texture: GLTexture?) {
        this.mTexture = texture
    }
}