package com.jiangpengyong.sample.filter

import android.opengl.GLES20
import android.util.Size
import com.jiangpengyong.eglbox.GLProgram
import com.jiangpengyong.eglbox.GLTexture
import com.jiangpengyong.eglbox.Target
import com.jiangpengyong.eglbox.allocateFloatBuffer
import com.jiangpengyong.eglbox.logger.Logger
import com.jiangpengyong.eglbox.utils.IDENTITY_MATRIX_4x4

enum class ScaleType {
    CENTER_CROP,        // 较小边适配，图片按比例缩放
    CENTER_INSIDE,      // 较大边适配，图片按比例缩放
    FIT_XY,             // 铺满整个页面，不保持比例
    MATRIX,             // 按自定义矩阵
}

fun Size.isValid(): Boolean {
    return width > 0 && height > 0
}

private data class Texture2DInfo(
    var scaleType: ScaleType = ScaleType.MATRIX,
    var targetSize: Size = Size(0, 0),
    var isMirrorX: Boolean = false,
    var isMirrorY: Boolean = false,
) {
    fun update(info: Texture2DInfo) {
        scaleType = info.scaleType
        targetSize = info.targetSize
        isMirrorX = info.isMirrorX
        isMirrorY = info.isMirrorY
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Texture2DInfo) return false
        return scaleType == other.scaleType &&
                targetSize.width == other.targetSize.width &&
                targetSize.height == other.targetSize.height &&
                isMirrorX == other.isMirrorX &&
                isMirrorY == other.isMirrorY
    }

    override fun hashCode(): Int {
        var result = scaleType.hashCode()
        result = 31 * result + targetSize.hashCode()
        result = 31 * result + isMirrorX.hashCode()
        result = 31 * result + isMirrorY.hashCode()
        return result
    }
}

class Texture2DProgram(val target: Target) : GLProgram() {
    private val TAG = "Texture2DProgram"

    private var mVertexCoordinates = defaultVertexCoordinates
    private var mTextureCoordinates = defaultTextureCoordinates
    private var mVertexMatrix = IDENTITY_MATRIX_4x4
    private var mTextureMatrix = IDENTITY_MATRIX_4x4

    private var mVertexMatrixHandle = 0
    private var mTextureMatrixHandle = 0
    private var mVertexPosHandle = 0
    private var mTexturePosHandle = 0
    private var mTexture: GLTexture? = null

    private val mCurrentTexture2DInfo = Texture2DInfo()
    private val mBeforeTexture2DInfo = Texture2DInfo()

    fun setScaleType(scaleType: ScaleType): Texture2DProgram {
        mCurrentTexture2DInfo.scaleType = scaleType
        return this
    }

    fun setTargetSize(size: Size): Texture2DProgram {
        mCurrentTexture2DInfo.targetSize = size
        return this
    }

    fun isMirrorX(value: Boolean): Texture2DProgram {
        mCurrentTexture2DInfo.isMirrorX = value
        return this
    }

    fun isMirrorY(value: Boolean): Texture2DProgram {
        mCurrentTexture2DInfo.isMirrorY = value
        return this
    }

    fun setTexture(texture: GLTexture): Texture2DProgram {
        mTexture = texture
        return this
    }

    fun setVertexMatrix(matrix: FloatArray): Texture2DProgram {
        if (mCurrentTexture2DInfo.scaleType != ScaleType.MATRIX) {
            Logger.e(TAG, "Since the scale type of Texture2DProgram isn't Matrix, you can't use setVertexMatrix function.")
            return this
        }
        mVertexMatrix = matrix
        return this
    }

    fun setTextureMatrix(matrix: FloatArray): Texture2DProgram {
        if (mCurrentTexture2DInfo.scaleType != ScaleType.MATRIX) {
            Logger.e(TAG, "Since the scale type of Texture2DProgram isn't Matrix, you can't use setTextureMatrix function.")
            return this
        }
        mTextureMatrix = matrix
        return this
    }

    fun reset(): Texture2DProgram {
        mVertexCoordinates = defaultVertexCoordinates
        mTextureCoordinates = defaultTextureCoordinates
        mVertexMatrix = IDENTITY_MATRIX_4x4
        mTextureMatrix = IDENTITY_MATRIX_4x4
        mTexture = null
        return this
    }

    override fun onInit() {
        mVertexMatrixHandle = getUniformLocation("uVertexMatrix")
        mTextureMatrixHandle = getUniformLocation("uTextureMatrix")
        mVertexPosHandle = getAttribLocation("aVertexPosition")
        mTexturePosHandle = getAttribLocation("aTexturePosition")
    }

    override fun onDraw() {
        if (mTexture == null) return
        mTexture?.bind()
        GLES20.glUniformMatrix4fv(mVertexMatrixHandle, 1, false, mVertexMatrix, 0)
        GLES20.glUniformMatrix4fv(mTextureMatrixHandle, 1, false, mTextureMatrix, 0)
        GLES20.glEnableVertexAttribArray(mVertexPosHandle)
        GLES20.glVertexAttribPointer(
            mVertexPosHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            2 * 4,
            mVertexCoordinates
        )
        GLES20.glEnableVertexAttribArray(mTexturePosHandle)
        GLES20.glVertexAttribPointer(
            mTexturePosHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            2 * 4,
            mTextureCoordinates
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mVertexPosHandle)
        GLES20.glDisableVertexAttribArray(mTexturePosHandle)
        mTexture?.unbind()
    }

    override fun onRelease() {
        mVertexMatrixHandle = 0
        mTextureMatrixHandle = 0
        mVertexPosHandle = 0
        mTexturePosHandle = 0
        mTexture = null
    }

    override fun getVertexShaderSource(): String = """
    uniform mat4 uVertexMatrix;
    uniform mat4 uTextureMatrix;
    attribute vec4 aVertexPosition;
    attribute vec4 aTexturePosition;
    varying vec2 vTexturePosition;
    void main() {
        gl_Position = uVertexMatrix * aVertexPosition;
        vTexturePosition = (uTextureMatrix * aTexturePosition).xy;
    }
    """

    override fun getFragmentShaderSource(): String = if (target == Target.TEXTURE_2D) {
        """
        precision mediump float;
        varying vec2 vTexturePosition;
        uniform sampler2D sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexturePosition);
        }
        """
    } else {
        """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexturePosition;
        uniform samplerExternalOES sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexturePosition);
        }
        """
    }

    private fun calculate(textureSize: Size) {
        // 没有改变则不进行计算
        if (mCurrentTexture2DInfo == mBeforeTexture2DInfo) {
            return
        }
        // 如果是 Matrix 类型，则矩阵计算交由外部
        if (mCurrentTexture2DInfo.scaleType == ScaleType.MATRIX) {
            mBeforeTexture2DInfo.update(mCurrentTexture2DInfo)
            return
        }
        val targetSize = mCurrentTexture2DInfo.targetSize
        if (!targetSize.isValid()) {
            Logger.e(TAG, "Target size is invalid. size=${targetSize}")
            return
        }
        if (!textureSize.isValid()) {
            Logger.e(TAG, "Texture size is invalid. size=${textureSize}")
            return
        }

    }

    companion object {
        val defaultVertexCoordinates = allocateFloatBuffer(
            floatArrayOf(
                -1.0f, -1.0f,   // 左下
                -1.0f, 1.0f,    // 左上
                1.0f, -1.0f,    // 右下
                1.0f, 1.0f      // 右上
            )
        )

        val defaultTextureCoordinates = allocateFloatBuffer(
            floatArrayOf(
                0.0f, 0.0f,     // 左下
                0.0f, 1.0f,     // 左上
                1.0f, 0.0f,     // 右下
                1.0f, 1.0f      // 右上
            )
        )
    }
}