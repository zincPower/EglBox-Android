package com.jiangpengyong.eglbox_core.program

import android.opengl.GLES20
import android.util.Size
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.utils.IDENTITY_MATRIX_4x4
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

/**
 * @author jiang peng yong
 * @date 2024/6/15 12:43
 * @email 56002982@qq.com
 * @des 用于绘制 2D 纹理，支持四种模式，通过 [setScaleType] 方法进行设置：
 * ScaleType {
 *     CENTER_CROP,        // 较小边适配，图片按比例缩放，会填充满可绘制区域
 *     CENTER_INSIDE,      // 较大边适配，图片按比例缩放，居中绘制纹理，可能会留有黑边
 *     FIT_XY,             // 铺满整个页面，不保持比例，铺满整个绘制区域，可能会变形
 *     MATRIX,             // 按自定义矩阵，按照定义的矩阵进行绘制
 * }
 *
 * 如果设置为 [ScaleType.MATRIX] ，可以通过下面两个方法设置需要的纹理：
 * 1、[setVertexMatrix] 设置顶点矩阵
 * 2、[setTextureMatrix] 设置纹理矩阵
 *
 * 通过 [setTargetSize] 设置绘制区域的大小，会根据设置的 [ScaleType] 和 设置的纹理大小进行计算矩阵，最后进行绘制
 *
 * 通过 [isMirrorX] 和 [isMirrorY] 设置是否需要进行 x 轴和 y 轴的镜像翻转
 *
 * 通过 [setTexture] 设置需要绘制的纹理
 *
 * 通过 [reset] 可以重置状态，包括：纹理、矩阵、设置信息
 *
 * 彩蛋：
 * 如果自定义矩阵，但又想使用 [ScaleType] 模式的算法，可以调用 [VertexAlgorithmFactory.calculate] 进行计算，
 * 可以得到一个 [ModelMatrix] 类型的返回值，内部包含了缩放值，可以直接对该矩阵调用相应的方法进行缩放、偏移、旋转，也
 * 可以调用 [ModelMatrix.matrix] 获取 16 个 Float 类型的 [FloatArray] 数组。
 */
class Texture2DProgram(val target: Target) : GLProgram() {
    private var mVertexCoordinates = defaultVertexCoordinates
    private var mTextureCoordinates = defaultTextureCoordinates
    private var mVertexMatrix = IDENTITY_MATRIX_4x4
    private var mTextureMatrix = IDENTITY_MATRIX_4x4
    private var mTexture: GLTexture? = null

    private var mVertexMatrixHandle = 0
    private var mTextureMatrixHandle = 0
    private var mVertexPosHandle = 0
    private var mTexturePosHandle = 0

    private val mCurrentTexture2DInfo = Texture2DInfo()
    private val mBeforeTexture2DInfo = Texture2DInfo()
    private var mCustomVertexMatrix = IDENTITY_MATRIX_4x4
    private var mCustomTextureMatrix = IDENTITY_MATRIX_4x4

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
        mCustomVertexMatrix = matrix
        return this
    }

    fun setTextureMatrix(matrix: FloatArray): Texture2DProgram {
        mCustomTextureMatrix = matrix
        return this
    }

    fun reset(): Texture2DProgram {
//        mVertexCoordinates = defaultVertexCoordinates
//        mTextureCoordinates = defaultTextureCoordinates
        mVertexMatrix = IDENTITY_MATRIX_4x4
        mTextureMatrix = IDENTITY_MATRIX_4x4
        mTexture = null
        mCurrentTexture2DInfo.reset()
        mBeforeTexture2DInfo.reset()
        mCustomVertexMatrix = IDENTITY_MATRIX_4x4
        mCustomTextureMatrix = IDENTITY_MATRIX_4x4
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
        updateInfo(Size(mTexture?.width ?: 0, mTexture?.height ?: 0))
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

    private fun updateInfo(textureSize: Size) {
        // 如果是 Matrix 类型，则矩阵计算交由外部
        if (mCurrentTexture2DInfo.scaleType == ScaleType.MATRIX) {
            mBeforeTexture2DInfo.update(mCurrentTexture2DInfo)
            mVertexMatrix = mCustomVertexMatrix
            mTextureMatrix = mCustomTextureMatrix
            return
        }
        // 没有改变则不进行计算
        if (mCurrentTexture2DInfo == mBeforeTexture2DInfo) {
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

        mBeforeTexture2DInfo.update(mCurrentTexture2DInfo)
        mTextureMatrix = IDENTITY_MATRIX_4x4

        val matrix = VertexAlgorithmFactory.calculate(
            mCurrentTexture2DInfo.scaleType,
            targetSize,
            textureSize
        )
        matrix.scale(
            if (mCurrentTexture2DInfo.isMirrorX) -1F else 1F,
            if (mCurrentTexture2DInfo.isMirrorY) -1F else 1F,
            1F
        )
        mVertexMatrix = matrix.matrix
    }

    companion object {
        private const val TAG = "Texture2DProgram"

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

    fun reset() {
        scaleType = ScaleType.MATRIX
        targetSize = Size(0, 0)
        isMirrorX = false
        isMirrorY = false
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

fun Size.isValid(): Boolean {
    return width > 0 && height > 0
}

enum class ScaleType {
    CENTER_CROP,        // 较小边适配，图片按比例缩放
    CENTER_INSIDE,      // 较大边适配，图片按比例缩放
    FIT_XY,             // 铺满整个页面，不保持比例
    MATRIX,             // 按自定义矩阵
}

object VertexAlgorithmFactory {

    private val algorithm = ArrayList<VertexAlgorithm>()

    init {
        algorithm.add(CenterCropAlgorithm())
        algorithm.add(CenterInsideAlgorithm())
        algorithm.add(FitXYAlgorithm())
    }

    fun calculate(scaleType: ScaleType, targetSize: Size, sourceSize: Size): ModelMatrix {
        for (item in algorithm) {
            if (item.getScaleType() == scaleType) {
                return item.handle(targetSize, sourceSize)
            }
        }
        return ModelMatrix()
    }

}

interface VertexAlgorithm {
    fun getScaleType(): ScaleType

    fun handle(targetSize: Size, sourceSize: Size): ModelMatrix
}

class CenterCropAlgorithm : VertexAlgorithm {
    override fun getScaleType(): ScaleType = ScaleType.CENTER_CROP
    override fun handle(targetSize: Size, sourceSize: Size): ModelMatrix {
        val matrix = ModelMatrix()
        val targetRatio = targetSize.width.toFloat() / targetSize.height.toFloat()
        val sourceRatio = sourceSize.width.toFloat() / sourceSize.height.toFloat()
        var scaleX = 1F
        var scaleY = 1F
        if (targetRatio < sourceRatio) { // 横图
            val width = sourceSize.height * targetRatio
            scaleX = sourceSize.width.toFloat() / width
            scaleY = 1.0F
        } else {                         // 竖图
            val height = sourceSize.width / targetRatio
            scaleX = 1.0F
            scaleY = sourceSize.height.toFloat() / height
        }
        Logger.i(
            "CenterCropAlgorithm",
            "handle targetRatio=${targetRatio}, sourceRatio=${sourceRatio}, scaleX=${scaleX}, scaleY=${scaleY}"
        );
        matrix.reset()
        matrix.scale(scaleX, scaleY, 1F)
        return matrix
    }
}

class CenterInsideAlgorithm : VertexAlgorithm {
    override fun getScaleType(): ScaleType = ScaleType.CENTER_INSIDE
    override fun handle(targetSize: Size, sourceSize: Size): ModelMatrix {
        val matrix = ModelMatrix()
        val targetRatio = targetSize.width.toFloat() / targetSize.height.toFloat()
        val sourceRatio = sourceSize.width.toFloat() / sourceSize.height.toFloat()
        var scaleX = 1F
        var scaleY = 1F
        if (targetRatio < sourceRatio) { // 横图
            val height = targetSize.width.toFloat() / sourceRatio
            scaleX = 1.0F
            scaleY = height / targetSize.height.toFloat()
        } else {                         // 竖图
            val width = targetSize.height * sourceRatio
            scaleX = width / targetSize.width.toFloat()
            scaleY = 1.0F
        }
        Logger.i(
            "CenterInsideAlgorithm",
            "handle targetRatio=${targetRatio}, sourceRatio=${sourceRatio}, scaleX=${scaleX}, scaleY=${scaleY}"
        );
        matrix.reset()
        matrix.scale(scaleX, scaleY, 1F)
        return matrix
    }
}

class FitXYAlgorithm : VertexAlgorithm {
    override fun getScaleType(): ScaleType = ScaleType.FIT_XY
    override fun handle(targetSize: Size, sourceSize: Size): ModelMatrix = ModelMatrix()
}