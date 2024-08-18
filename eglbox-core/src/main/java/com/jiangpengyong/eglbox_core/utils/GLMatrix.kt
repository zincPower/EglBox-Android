package com.jiangpengyong.eglbox_core.utils

import android.opengl.Matrix
import android.util.Log
import java.nio.FloatBuffer

private const val MATRIX_LENGTH = 16

val IDENTITY_MATRIX_4x4 = floatArrayOf(
    1.0F, 0.0F, 0.0F, 0.0F,
    0.0F, 1.0F, 0.0F, 0.0F,
    0.0F, 0.0F, 1.0F, 0.0F,
    0.0F, 0.0F, 0.0F, 1.0F,
).copyOf()

/**
 * @author jiang peng yong
 * @date 2024/2/11 18:42
 * @email 56002982@qq.com
 * @des 包含 Model View Project 的 Matrix
 */
class MVPMatrix {
    private val mProjectMatrix = ProjectMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var mMVPMatrix = FloatArray(MATRIX_LENGTH)

    /**
     * 重置所有矩阵
     */
    fun reset(): MVPMatrix {
        mProjectMatrix.reset()
        mViewMatrix.reset()
        mModelMatrix.reset()
        return this
    }

    /**
     * 重置投影矩阵
     */
    fun resetProject(): MVPMatrix {
        mProjectMatrix.reset()
        return this
    }

    /**
     * 重置摄像机位置朝向矩阵
     */
    fun resetView(): MVPMatrix {
        mViewMatrix.reset()
        return this
    }

    /**
     * 重置物体变换矩阵
     */
    fun resetModel(): MVPMatrix {
        mModelMatrix.reset()
        return this
    }

    /**
     * 移动位置
     * @param x x 轴移动距离
     * @param y y 轴移动距离
     * @param z z 轴移动距离
     */
    fun translate(x: Float, y: Float, z: Float): MVPMatrix {
        mModelMatrix.translate(x, y, z)
        return this
    }

    /**
     * 旋转，围绕 (x, y, z) 轴
     * @param angle 旋转角度
     * @param x
     * @param y
     * @param z
     */
    fun rotate(angle: Float, x: Float, y: Float, z: Float): MVPMatrix {
        mModelMatrix.rotate(angle, x, y, z)
        return this
    }

    /**
     * 以原点为缩放
     * @param x x 轴缩放值
     * @param y y 轴缩放值
     * @param z z 轴缩放值
     */
    fun scale(x: Float, y: Float, z: Float): MVPMatrix {
        mModelMatrix.scale(x, y, z)
        return this
    }

    /**
     * 设置摄像机
     *
     * @param eyeX  摄像机位置x
     * @param eyeY  摄像机位置y
     * @param eyeZ  摄像机位置z
     * @param centerX  摄像机目标点x
     * @param centerY  摄像机目标点y
     * @param centerZ  摄像机目标点z
     * @param upx 摄像机UP向量X分量
     * @param upy 摄像机UP向量Y分量
     * @param upz 摄像机UP向量Z分量
     */
    fun setLookAtM(
        eyeX: Float, eyeY: Float, eyeZ: Float,
        centerX: Float, centerY: Float, centerZ: Float,
        upx: Float, upy: Float, upz: Float,
    ): MVPMatrix {
        mViewMatrix.setLookAtM(
            eyeX, eyeY, eyeZ,
            centerX, centerY, centerZ,
            upx, upy, upz
        )
        return this
    }

    fun getCameraLocation() = mViewMatrix.cameraLocation

    fun getCameraLocationBuffer() = mViewMatrix.cameraLocationBuffer

    /**
     * 设置透视投影参数
     *
     * @param left near 面的left
     * @param right near 面的right
     * @param bottom near 面的bottom
     * @param top near 面的top
     * @param near 相机到 near 面距离
     * @param far 相机到 far 面距离
     */
    fun setFrustumM(
        left: Float, right: Float,
        bottom: Float, top: Float,
        near: Float, far: Float
    ): MVPMatrix {
        mProjectMatrix.setFrustumM(
            left, right,
            bottom, top,
            near, far
        )
        return this
    }

    /**
     * 设置正交投影参数
     *
     * @param right near面的left
     * @param left near面的right
     * @param bottom near面的bottom
     * @param top near面的top
     * @param near 相机到near面距离
     * @param far 相机到far面距离
     */
    fun setOrthoM(
        left: Float, right: Float,
        bottom: Float, top: Float,
        near: Float, far: Float
    ): MVPMatrix {
        mProjectMatrix.setOrthoM(
            left, right,
            bottom, top,
            near, far
        )
        return this
    }

    /**
     * 获取具体物体的总变换矩阵
     *
     * projectMatrix * viewMatrix * modelMatrix
     */
    fun getMVPMatrix(): FloatArray {
        Matrix.multiplyMM(
            mMVPMatrix, 0,
            mViewMatrix.matrix, 0,
            mModelMatrix.matrix, 0
        )
        Matrix.multiplyMM(
            mMVPMatrix, 0,
            mProjectMatrix.matrix, 0,
            mMVPMatrix, 0
        )
        return mMVPMatrix
    }
}

/**
 * @author jiang peng yong
 * @date 2024/2/11 18:47
 * @email 56002982@qq.com
 * @des 4x4 矩阵
 */
class ModelMatrix : GLMatrix() {
    /**
     * 移动位置
     * @param x x 轴移动距离
     * @param y y 轴移动距离
     * @param z z 轴移动距离
     */
    fun translate(x: Float, y: Float, z: Float): ModelMatrix {
        Matrix.translateM(matrix, 0, x, y, z)
        return this
    }

    /**
     * 旋转，围绕 (x, y, z) 轴
     * @param angle 旋转角度
     * @param x
     * @param y
     * @param z
     */
    fun rotate(angle: Float, x: Float, y: Float, z: Float): ModelMatrix {
        Matrix.rotateM(matrix, 0, angle, x, y, z)
        return this
    }

    /**
     * 以原点为缩放
     * @param x x 轴缩放值
     * @param y y 轴缩放值
     * @param z z 轴缩放值
     */
    fun scale(x: Float, y: Float, z: Float): ModelMatrix {
        Matrix.scaleM(matrix, 0, x, y, z)
        return this
    }
}

/**
 * @author jiang peng yong
 * @date 2024/2/11 21:24
 * @email 56002982@qq.com
 * @des 透视矩阵
 */
class ProjectMatrix : GLMatrix() {
    /**
     * 设置透视投影参数
     *
     * @param left near面的left
     * @param right near面的right
     * @param bottom near面的bottom
     * @param top near面的top
     * @param near 相机到near面距离
     * @param far 相机到far面距离
     */
    fun setFrustumM(
        left: Float, right: Float,
        bottom: Float, top: Float,
        near: Float, far: Float
    ): ProjectMatrix {
        Matrix.frustumM(
            matrix,
            0,
            left, right,
            bottom, top,
            near, far
        )
        return this
    }

    /**
     * 设置正交投影参数
     *
     * @param right near面的left
     * @param left near面的right
     * @param bottom near面的bottom
     * @param top near面的top
     * @param near 相机到near面距离
     * @param far 相机到far面距离
     */
    fun setOrthoM(
        left: Float, right: Float,
        bottom: Float, top: Float,
        near: Float, far: Float
    ): ProjectMatrix {
        Matrix.orthoM(
            matrix,
            0,
            left, right,
            bottom, top,
            near, far
        )
        return this
    }
}

/**
 * @author jiang peng yong
 * @date 2024/2/11 21:24
 * @email 56002982@qq.com
 * @des 视图矩阵
 */
class ViewMatrix : GLMatrix() {
    // 相机位置
    val cameraLocation = floatArrayOf(0f, 0f, 0f)
    val cameraLocationBuffer: FloatBuffer by lazy { allocateFloatBuffer(3) }

    /**
     * 设置视图参数
     *
     * @param eyeX  摄像机位置 x
     * @param eyeY  摄像机位置 y
     * @param eyeZ  摄像机位置 z
     * @param centerX  观察点 x
     * @param centerY  观察点 y
     * @param centerZ  观察点 z
     * @param upx 摄像机 up 向量X分量
     * @param upy 摄像机 up 向量Y分量
     * @param upz 摄像机 up 向量Z分量
     */
    fun setLookAtM(
        eyeX: Float, eyeY: Float, eyeZ: Float,
        centerX: Float, centerY: Float, centerZ: Float,
        upx: Float, upy: Float, upz: Float,
    ): ViewMatrix {
        Matrix.setLookAtM(
            matrix,
            0,
            eyeX, eyeY, eyeZ,
            centerX, centerY, centerZ,
            upx, upy, upz
        )

        cameraLocation[0] = eyeX
        cameraLocation[1] = eyeY
        cameraLocation[2] = eyeZ

        cameraLocationBuffer.clear()
        cameraLocationBuffer.put(cameraLocation)
        cameraLocationBuffer.position(0)
        return this
    }
}

open class GLMatrix {
    val matrix = FloatArray(MATRIX_LENGTH)

    init {
        reset()
    }

    /**
     * 重置矩阵
     */
    fun reset() {
        Matrix.setIdentityM(matrix, 0)
    }

    /**
     * 矩阵相乘
     */
    operator fun times(other: GLMatrix): GLMatrix {
        val result = GLMatrix()
        Matrix.multiplyMM(
            result.matrix, 0,
            matrix, 0,
            other.matrix, 0
        )
        return result
    }

    operator fun times(other: FloatArray): FloatArray {
        if (other.size != 3) {
            Log.e(TAG, "The parameter requires an array of length three.")
            return floatArrayOf(0F, 0F, 0F)
        }

        val vector = floatArrayOf(other[0], other[1], other[2], 1F)
        val resultVector = FloatArray(4) // 存储结果的数组
        Matrix.multiplyMV(resultVector, 0, matrix, 0, vector, 0)
        return resultVector
    }

    companion object {
        const val TAG = "GLMatrix"
    }
}