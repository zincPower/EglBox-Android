package com.jiangpengyong.eglbox

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import com.jiangpengyong.eglbox.logger.Logger

/**
 * @author: jiang peng yong
 * @date: 2023/8/31 19:51
 * @email: 56002982@qq.com
 * @desc: 纹理
 */
class Texture(
    private val targetType: TargetType = TargetType.TEXTURE_2D,
    private val minFilter: MinFilter = MinFilter.NEAREST,
    private val magFilter: MagFilter = MagFilter.LINEAR,
    private val wrapS: WrapMode = WrapMode.EDGE,
    private val wrapT: WrapMode = WrapMode.EDGE,
) {
    companion object {
        private const val NOT_INIT = -1
    }

    var id = NOT_INIT
        private set
    var width: Int = NOT_INIT
        private set
    var height: Int = NOT_INIT
        private set

    /**
     * 初始化纹理
     */
    fun createTexture() {
        init()
        GLES20.glBindTexture(targetType.value, 0)

        Logger.i("Create texture simple. [id: $id]")
    }

    /**
     * 初始化纹理
     * @param width 纹理宽
     * @param height 纹理高
     * @param internalFormat 内部格式
     * @param format 数据格式
     */
    fun createTexture(width: Int, height: Int, internalFormat: Int = GLES20.GL_RGBA, format: Int = GLES20.GL_RGBA) {
        if (targetType == TargetType.EXTERNAL_OES) {
            Logger.e("Target type can't EXTERNAL OES.")
            return
        }
        init()
        this.width = width
        this.height = height
        // 设置颜色附件纹理格式
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,                   // 层次
            internalFormat,             // 内部格式
            width,                      // 宽度
            height,                     // 高度
            0,                  // 边界宽度
            format,                     // 格式
            GLES20.GL_UNSIGNED_BYTE,    // 每个像素数据格式
            null
        )
        GLES20.glBindTexture(targetType.value, 0)

        Logger.i("Create texture by size. [id: $id, size: ${this.width} x ${this.height}]")
    }

    /**
     * 初始化纹理
     * @param bitmap 位图
     */
    fun createTexture(bitmap: Bitmap) {
        if (bitmap.isRecycled) {
            Logger.e("Bitmap is recycled.")
            return
        }
        init()
        this.width = bitmap.width
        this.height = bitmap.height
        // https://registry.khronos.org/OpenGL-Refpages/es2.0/xhtml/glTexImage2D.xml
        GLUtils.texImage2D(
            GLES20.GL_TEXTURE_2D,               // 纹理类型
            0,                           // 层次
            GLUtils.getInternalFormat(bitmap),  // 指定纹理的内部格式
            bitmap,                             // 纹理图像
            GLUtils.getType(bitmap),            // 指定 texel 数据的格式，必须匹配 internal format。
            0                           // 纹理边框尺寸
        )
        GLES20.glBindTexture(targetType.value, 0)

        Logger.i("Create texture by bitmap. [id: $id, size: ${this.width} x ${this.height}]")
    }

    private fun init() {
        if (id != NOT_INIT) {
            Logger.e("Texture id is not NOT_INIT. Please delete texture first.[$id]")
            return
        }

        val textureIdArray = intArrayOf(NOT_INIT)
        GLES20.glGenTextures(1, textureIdArray, 0)
        id = textureIdArray[0]

        // 可用参数 https://registry.khronos.org/OpenGL-Refpages/gl4/html/glTexParameter.xhtml
        GLES20.glBindTexture(targetType.value, id)
        GLES20.glTexParameteri(targetType.value, GLES20.GL_TEXTURE_MIN_FILTER, minFilter.value)
        GLES20.glTexParameteri(targetType.value, GLES20.GL_TEXTURE_MAG_FILTER, magFilter.value)
        GLES20.glTexParameteri(targetType.value, GLES20.GL_TEXTURE_WRAP_S, wrapS.value)
        GLES20.glTexParameteri(targetType.value, GLES20.GL_TEXTURE_WRAP_T, wrapT.value)
    }

    /**
     * 是否初始化
     * @return true：已经初始化
     *         false：未初始化
     */
    fun isInit(): Boolean = (id != NOT_INIT)

    /**
     * 是否可复用（这里没有进行比对 targetType 等属性）
     * @param width 新的宽度
     * @param height 新的高度
     * @return true 可复用
     *         false 不可复用
     */
    fun isReusable(width: Int, height: Int): Boolean {
        if (!isInit()) return false
        return width == this.width && height == this.height
    }

    fun bind() {
        if (id < 0) {
            Logger.e("Texture id is invalid.Please call initTexture function first. [$id]")
            return
        }
        GLES20.glBindTexture(targetType.value, id)
    }

    fun unbind() {
        GLES20.glBindTexture(targetType.value, 0)
    }

    fun release() {
        if (!isInit()) return
        GLES20.glBindTexture(targetType.value, 0)
        GLES20.glDeleteTextures(1, intArrayOf(id), 0)
        Logger.i("Delete Texture.[Texture Id: $id]")

        id = NOT_INIT
        width = NOT_INIT
        height = NOT_INIT
    }

    override fun toString(): String {
        return "id: $id, size: $width x $height"
    }
}

enum class TargetType(val value: Int) {
    TEXTURE_2D(GLES20.GL_TEXTURE_2D),
    EXTERNAL_OES(GLES11Ext.GL_TEXTURE_EXTERNAL_OES),
}

enum class WrapMode(val value: Int) {
    EDGE(GLES20.GL_CLAMP_TO_EDGE),                      // 末端拉伸
    REPEAT(GLES20.GL_REPEAT),                           // 重复
    MIRROR(GLES20.GL_MIRRORED_REPEAT),                  // 镜像
}

enum class MinFilter(val value: Int) {
    NEAREST(GLES20.GL_NEAREST),
    LINEAR(GLES20.GL_LINEAR),
    NEAREST_MIPMAP_NEAREST(GLES20.GL_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GLES20.GL_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GLES20.GL_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GLES20.GL_LINEAR_MIPMAP_LINEAR),
}

enum class MagFilter(val value: Int) {
    NEAREST(GLES20.GL_NEAREST),
    LINEAR(GLES20.GL_LINEAR),
}