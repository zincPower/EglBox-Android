package com.jiangpengyong.eglbox

import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author: jiang peng yong
 * @date: 2021/12/10 4:37 下午
 * @email: 56002982@qq.com
 * @desc: 矩阵类
 */
class MatrixState {
    companion object {
        private const val MATRIX_LENGTH = 16
    }

    // 投影矩阵
    val projectMatrix = FloatArray(MATRIX_LENGTH)

    // 摄像机位置朝向矩阵
    val viewMatrix = FloatArray(MATRIX_LENGTH)

    // 获取具体物体的变换矩阵
    val modelMatrix = FloatArray(MATRIX_LENGTH)

    // 相机位置
    private var cameraLocation = floatArrayOf(0f, 0f, 0f)
    private val cameraLocationBuffer: FloatBuffer by lazy { allocateFloatBuffer(3) }

    private var mMVPMatrix = FloatArray(MATRIX_LENGTH)

    init {
        reset()
    }

    fun reset(): MatrixState {
        Matrix.setIdentityM(projectMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(modelMatrix, 0)
        return this
    }

    fun resetProject(): MatrixState {
        Matrix.setIdentityM(modelMatrix, 0)
        return this
    }

    fun resetView(): MatrixState {
        Matrix.setIdentityM(viewMatrix, 0)
        return this
    }

    fun resetModel(): MatrixState {
        Matrix.setIdentityM(modelMatrix, 0)
        return this
    }

    fun translate(x: Float, y: Float, z: Float): MatrixState {
        Matrix.translateM(modelMatrix, 0, x, y, z)
        return this
    }

    fun rotate(angle: Double, x: Float, y: Float, z: Float): MatrixState {
        Matrix.rotateM(modelMatrix, 0, Math.toRadians(angle).toFloat(), x, y, z)
        return this
    }

    fun scale(x: Float, y: Float, z: Float): MatrixState {
        Matrix.scaleM(modelMatrix, 0, x, y, z)
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
    fun setCamera(
        eyeX: Float, eyeY: Float, eyeZ: Float,
        centerX: Float, centerY: Float, centerZ: Float,
        upx: Float, upy: Float, upz: Float,
    ): MatrixState {
        Matrix.setLookAtM(
            viewMatrix,
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
    fun setProjectFrustum(
        left: Float, right: Float,
        bottom: Float, top: Float,
        near: Float, far: Float
    ): MatrixState {
        Matrix.frustumM(
            projectMatrix,
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
    fun setProjectOrtho(
        left: Float, right: Float,
        bottom: Float, top: Float,
        near: Float, far: Float
    ): MatrixState {
        Matrix.orthoM(
            projectMatrix,
            0,
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
            projectMatrix, 0,
            viewMatrix, 0
        )
        Matrix.multiplyMM(
            mMVPMatrix, 0,
            mMVPMatrix, 0,
            modelMatrix, 0
        )
        return mMVPMatrix
    }
}