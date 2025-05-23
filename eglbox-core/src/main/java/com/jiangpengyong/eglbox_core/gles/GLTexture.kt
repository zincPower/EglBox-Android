package com.jiangpengyong.eglbox_core.gles

import android.graphics.Bitmap
import android.opengl.ETC1Util
import android.opengl.ETC1Util.ETC1Texture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import com.jiangpengyong.eglbox_core.logger.Logger

/**
 * @author: jiang peng yong
 * @date: 2023/8/31 19:51
 * @email: 56002982@qq.com
 * @desc: 纹理
 */
class GLTexture(
    val target: Target = Target.TEXTURE_2D,
    val internalFormat: Int = GLES20.GL_RGBA,
    val format: Int = GLES20.GL_RGBA,
    val type: Int = GLES20.GL_UNSIGNED_BYTE,
    val minFilter: MinFilter = MinFilter.NEAREST,
    val magFilter: MagFilter = MagFilter.LINEAR,
    val wrapS: WrapMode = WrapMode.EDGE,
    val wrapT: WrapMode = WrapMode.EDGE,
) {
    var id = 0
        private set
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    private var mTextureUnit = GLES20.GL_TEXTURE0

    /**
     * 初始化纹理
     */
    fun init(block: (() -> Unit)? = null) {
        if (isInit()) {
            Logger.i(TAG, "Texture has been initialized. id=$id")
            return
        }
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        id = ids[0]
        GLES20.glBindTexture(target.value, id)
        // 可用参数 https://registry.khronos.org/OpenGL-Refpages/gl4/html/glTexParameter.xhtml
        GLES20.glTexParameteri(target.value, GLES20.GL_TEXTURE_MIN_FILTER, minFilter.value)
        GLES20.glTexParameteri(target.value, GLES20.GL_TEXTURE_MAG_FILTER, magFilter.value)
        GLES20.glTexParameteri(target.value, GLES20.GL_TEXTURE_WRAP_S, wrapS.value)
        GLES20.glTexParameteri(target.value, GLES20.GL_TEXTURE_WRAP_T, wrapT.value)

        block?.invoke()
        unbind()
        Logger.i(TAG, "Init GLTexture success. id=$id")
    }

    fun updateSizeForOES(width: Int, height: Int) {
        if (target != Target.EXTERNAL_OES) {
            Logger.e(TAG, "Can't call updateSizeForOES function when target isn't EXTERNAL_OES type.")
            return
        }
        this.width = width
        this.height = height
    }

    /**
     * 初始化纹理
     * @param width 纹理宽
     * @param height 纹理高
     */
    fun setData(width: Int, height: Int, pixels: java.nio.Buffer? = null) {
        if (!isInit()) {
            Logger.i(TAG, "Texture isn't initialized. size=$width x $height【setData(width, height, pixels)】")
            return
        }
        if (target == Target.EXTERNAL_OES) {
            Logger.e(TAG, "Target type can't EXTERNAL OES.")
            return
        }
        this.width = width
        this.height = height
        GLES20.glBindTexture(target.value, id)
        // 设置颜色附件纹理格式
        GLES20.glTexImage2D(
            target.value,
            0,       // 层次
            internalFormat, // 内部格式
            width,          // 宽度
            height,         // 高度
            0,      // 边界宽度
            format,         // 格式
            type,           // 每个像素数据格式
            pixels,
        )
        if (pixels != null && (minFilter == MinFilter.LINEAR_MIPMAP_NEAREST
                    || minFilter == MinFilter.LINEAR_MIPMAP_LINEAR
                    || minFilter == MinFilter.NEAREST_MIPMAP_LINEAR
                    || minFilter == MinFilter.NEAREST_MIPMAP_NEAREST)
        ) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        }
        unbind()
        Logger.i(TAG, "Set data to texture success. id=$id, size=$width x $height, pixels=$pixels")
    }

    /**
     * 初始化纹理
     * @param bitmap 位图
     */
    fun setData(bitmap: Bitmap) {
        if (!isInit()) {
            Logger.e(TAG, "Texture isn't initialized. size=${bitmap.width} x ${bitmap.height}【setData(bitmap)】")
            return
        }
        if (target == Target.EXTERNAL_OES) {
            Logger.e(TAG, "Target type can't EXTERNAL OES.")
            return
        }
        if (bitmap.isRecycled) {
            Logger.e(TAG, "Bitmap is recycled.")
            return
        }
        this.width = bitmap.width
        this.height = bitmap.height
        GLES20.glBindTexture(target.value, id)
        // https://registry.khronos.org/OpenGL-Refpages/es2.0/xhtml/glTexImage2D.xml
        GLUtils.texImage2D(
            target.value,                       // 纹理类型
            0,                             // 层次
            GLUtils.getInternalFormat(bitmap),  // 指定纹理的内部格式
            bitmap,                             // 纹理图像
            GLUtils.getType(bitmap),            // 指定 texel 数据的格式，必须匹配 internal format
            0,                           // 纹理边框尺寸
        )
        if (minFilter == MinFilter.LINEAR_MIPMAP_NEAREST
            || minFilter == MinFilter.LINEAR_MIPMAP_LINEAR
            || minFilter == MinFilter.NEAREST_MIPMAP_LINEAR
            || minFilter == MinFilter.NEAREST_MIPMAP_NEAREST
        ) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        }
        unbind()
        Logger.i(TAG, "Create texture by bitmap. id=$id, size=${this.width} x ${this.height}")
    }

    /**
     * 设置 ETC1 格式纹理
     */
    fun setData(ect1Texture: ETC1Texture) {
        if (!isInit()) {
            Logger.e(TAG, "Texture isn't initialized. size=${ect1Texture.width} x ${ect1Texture.height}【setData(ect1Texture)】")
            return
        }
        if (target == Target.EXTERNAL_OES) {
            Logger.e(TAG, "Target type can't EXTERNAL OES.")
            return
        }
        this.width = ect1Texture.width
        this.height = ect1Texture.height
        GLES20.glBindTexture(target.value, id)
        ETC1Util.loadTexture(
            GLES20.GL_TEXTURE_2D,
            0,
            0,
            GLES20.GL_RGB,
            GLES20.GL_UNSIGNED_BYTE,
            ect1Texture
        )
        if (minFilter == MinFilter.LINEAR_MIPMAP_NEAREST
            || minFilter == MinFilter.LINEAR_MIPMAP_LINEAR
            || minFilter == MinFilter.NEAREST_MIPMAP_LINEAR
            || minFilter == MinFilter.NEAREST_MIPMAP_NEAREST
        ) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        }
        unbind()
        Logger.i(TAG, "Create texture by ect1Texture. id=$id, size=${this.width} x ${this.height}")
    }

    fun isInit(): Boolean = (id != 0)

    fun bind(textureUnit: Int = GLES20.GL_TEXTURE0, block: (() -> Unit)) {
        bind(textureUnit)
        block()
        unbind()
    }

    fun bind(textureUnit: Int = GLES20.GL_TEXTURE0) {
        if (id <= 0) {
            Logger.i(TAG, "GLTexture isn't initialized.【bind】")
            return
        }
        mTextureUnit = textureUnit
        GLES20.glActiveTexture(textureUnit)
        GLES20.glBindTexture(target.value, id)
    }

    fun unbind() {
        if (!isInit()) {
            Logger.e(TAG, "GLTexture isn't initialized.【unbind】")
            return
        }
        val currentTexture = EglBox.getCurrentTexture()
        if (currentTexture == id) {
            GLES20.glActiveTexture(mTextureUnit)
            GLES20.glBindTexture(target.value, 0)
        }
    }

    fun release() {
        if (!isInit()) return
        Logger.i(TAG, "Release GLTexture. id=$id, size=$width x $height")
        unbind()
        GLES20.glDeleteTextures(1, intArrayOf(id), 0)

        id = 0
        width = 0
        height = 0
    }

    override fun toString(): String {
        return "[ GLTexture id=$id, size=$width x $height ]"
    }

    companion object {
        private const val TAG: String = "GLTexture"

        /**
         * 创建颜色纹理
         */
        fun createColorTexture() = GLTexture(
            target = Target.TEXTURE_2D,
            internalFormat = GLES20.GL_RGBA,
            format = GLES20.GL_RGBA,
            type = GLES20.GL_UNSIGNED_BYTE,
            minFilter = MinFilter.NEAREST,
            magFilter = MagFilter.LINEAR,
            wrapS = WrapMode.EDGE,
            wrapT = WrapMode.EDGE,
        )

        /**
         * 创建深度纹理
         * GL_DEPTH_COMPONENT16 需要 16 位，即 2 个字节，需要的类型至少能装下 16 位，选择 GL_UNSIGNED_SHORT
         */
        fun createDepthTexture() = GLTexture(
            target = Target.TEXTURE_2D,
            internalFormat = GLES20.GL_DEPTH_COMPONENT16,
            format = GLES20.GL_DEPTH_COMPONENT,
            type = GLES20.GL_UNSIGNED_SHORT,
            minFilter = MinFilter.NEAREST,
            magFilter = MagFilter.LINEAR,
            wrapS = WrapMode.EDGE,
            wrapT = WrapMode.EDGE,
        )

//        /**
//         * 创建模板纹理
//         *
//         */
//        fun createStencilTexture() = GLTexture(
//            target = Target.TEXTURE_2D,
//            internalFormat = GLES20.GL_STENCIL_INDEX8,
//            format = GLES20.GL_STENCIL_INDEX,
//            type = GLES20.GL_UNSIGNED_SHORT,
//            minFilter = MinFilter.NEAREST,
//            magFilter = MagFilter.LINEAR,
//            wrapS = WrapMode.EDGE,
//            wrapT = WrapMode.EDGE,
//        )
    }
}

enum class Target(val value: Int) {
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