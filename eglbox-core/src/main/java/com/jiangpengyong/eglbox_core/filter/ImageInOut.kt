package com.jiangpengyong.eglbox_core.filter

import com.jiangpengyong.eglbox_core.gles.GLFrameBuffer
import com.jiangpengyong.eglbox_core.gles.GLTexture

/**
 * @author jiang peng yong
 * @date 2024/7/18 12:50
 * @email 56002982@qq.com
 * @des 图像信息
 */
class ImageInOut {
    private var mContext: FilterContext? = null
    var texture: GLTexture? = null
        private set

    fun setContext(context: FilterContext) {
        mContext = context
    }

    fun isValid(): Boolean {
        val curTex = texture
        return curTex != null && curTex.isInit() && curTex.width > 0 && curTex.height > 0
    }

    fun out(texture: GLTexture, isAutoRelease: Boolean = true) {
        if (this.texture != null && this.texture?.id != texture.id && isAutoRelease) {
            this.texture?.let {
                if (mContext == null) {
                    it.release()
                } else {
                    mContext?.recycle(it)
                }
            }
        }
        this.texture = texture
    }

    fun out(fbo: GLFrameBuffer, isAutoRelease: Boolean = true) {
        val innerTexture = fbo.unbindTexture()
        innerTexture?.let { out(it, isAutoRelease) }
        if (mContext == null) {
            fbo.release()
        } else {
            mContext?.recycle(fbo)
        }
    }

    fun clear() {
        texture = null
    }
}