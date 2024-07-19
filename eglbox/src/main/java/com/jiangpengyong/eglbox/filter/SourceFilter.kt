package com.jiangpengyong.eglbox.filter

import com.jiangpengyong.eglbox.gles.GLTexture

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
        mTexture?.let { mImage.reset(it) }
        return mImage
    }

    protected fun updateTexture(texture: GLTexture) {
        this.mTexture = texture
    }
}