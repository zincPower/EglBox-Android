package com.jiangpengyong.eglbox.filter

import com.jiangpengyong.eglbox.GLFrameBuffer
import com.jiangpengyong.eglbox.GLTexture

class ImageInOut {

    private var mContext: FilterContext? = null
    var texture: GLTexture? = null
        private set

    fun setContext(context: FilterContext) {
        mContext = context
    }

    fun reset(texture: GLTexture) {
        val curTex = this.texture
        if (curTex != null && curTex.id != texture.id) {
            if (mContext == null) {
                curTex.release()
            } else {
                mContext?.recycle(curTex)
            }
        }
        this.texture = texture
    }

    fun isValid(): Boolean {
        val curTex = texture
        return curTex != null && curTex.isInit() && curTex.width > 0 && curTex.height > 0
    }

    fun out(texture: GLTexture, isAutoRelease: Boolean = true) {
        if (this.texture != null && this.texture?.id != texture.id && isAutoRelease) {
            if (mContext == null) {
                texture.release()
            } else {
                mContext?.recycle(texture)
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