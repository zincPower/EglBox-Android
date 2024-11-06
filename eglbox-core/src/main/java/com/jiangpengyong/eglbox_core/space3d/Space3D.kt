package com.jiangpengyong.eglbox_core.space3d

import android.util.Size
import com.jiangpengyong.eglbox_core.program.isValid
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectionMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix

/**
 * @author jiang peng yong
 * @date 2024/11/4 20:40
 * @email 56002982@qq.com
 * @des 3D 参数
 */
class Space3D {
    // ===================================================================================================
    // =============================================== 模型 ===============================================
    // ===================================================================================================
    var rotation = Rotation(angleX = 0F, angleY = 0F, angleZ = 0F)
        set(value) {
            field = value
            updateGestureMatrix()
        }
    var scale = Scale(scaleX = 1F, scaleY = 1F)
    val gestureMatrix = ModelMatrix()

    // ===================================================================================================
    // =============================================== 视图 ===============================================
    // ===================================================================================================
    // 观察点
    var viewPoint = Point(0F, 0F, 10F)
        set(value) {
            field = value
            updateViewMatrix()
        }

    // 观察点向上向量
    var upVector = Point(0F, 1F, 0F)
        set(value) {
            field = value
            updateViewMatrix()
        }

    // 被观察点
    var centerPoint = Point(0F, 0F, 0F)
        set(value) {
            field = value
            updateViewMatrix()
        }
    val viewMatrix = ViewMatrix()

    // ===================================================================================================
    // =============================================== 投影 ===============================================
    // ===================================================================================================
    var projection = Projection(type = ProjectionType.Perspective, near = 2F, far = 1000F, ratio = 1F)
        set(value) {
            field = value
            updateProjectMatrix(previewSize)
        }
    var previewSize = Size(0, 0)
        set(value) {
            field = value
            updateProjectMatrix(value)
        }
    val projectionMatrix = ProjectionMatrix()

    /**
     * 更新手势矩阵
     */
    private fun updateGestureMatrix() {
        gestureMatrix.reset()
        gestureMatrix.rotate(rotation.angleX, 0F, 1F, 0F)
        gestureMatrix.rotate(rotation.angleY, 1F, 0F, 0F)
        gestureMatrix.rotate(rotation.angleZ, 0F, 0F, 1F)
    }

    /**
     * 更新视图矩阵
     */
    private fun updateViewMatrix() {
        viewMatrix.reset()
        viewMatrix.setLookAtM(
            eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
            centerX = centerPoint.x, centerY = centerPoint.y, centerZ = centerPoint.z,
            upx = upVector.x, upy = upVector.y, upz = centerPoint.z,
        )
    }

    /**
     * 更新投影矩阵
     */
    private fun updateProjectMatrix(previewSize: Size) {
        if (previewSize.isValid()) return
        projectionMatrix.reset()
        val left: Float
        val right: Float
        val bottom: Float
        val top: Float
        val near: Float
        val far: Float
        if (previewSize.width > previewSize.height) {
            val previewRatio = previewSize.width.toFloat() / previewSize.height.toFloat() * projection.ratio
            left = -previewRatio
            right = previewRatio
            bottom = -projection.ratio
            top = projection.ratio
            near = projection.near
            far = projection.far
        } else {
            val previewRatio = previewSize.height.toFloat() / previewSize.width.toFloat() * projection.ratio
            left = -projection.ratio
            right = projection.ratio
            bottom = -previewRatio
            top = previewRatio
            near = projection.near
            far = projection.far
        }
        when (projection.type) {
            ProjectionType.Orthographic -> projectionMatrix.setOrthoM(left = left, right = right, bottom = bottom, top = top, near = near, far = far)
            ProjectionType.Perspective -> projectionMatrix.setFrustumM(left = left, right = right, bottom = bottom, top = top, near = near, far = far)
        }
    }
}

/**
 * @author jiang peng yong
 * @date 2024/10/20 18:00
 * @email 56002982@qq.com
 * @des 旋转角度
 */
data class Rotation(
    val angleX: Float,
    val angleY: Float,
    val angleZ: Float,
)

/**
 * @author jiang peng yong
 * @date 2024/10/21 08:32
 * @email 56002982@qq.com
 * @des 缩放
 */
data class Scale(
    val scaleX: Float,
    val scaleY: Float,
)

/**
 * @author jiang peng yong
 * @date 2024/11/4 21:23
 * @email 56002982@qq.com
 * @des 投影信息
 */
data class Projection(
    val type: ProjectionType,
    val near: Float,
    val far: Float,
    val ratio: Float,
)

/**
 * @author jiang peng yong
 * @date 2024/11/4 21:22
 * @email 56002982@qq.com
 * @des 投影方式
 */
enum class ProjectionType {
    Orthographic,   // 正交投影
    Perspective,    // 透视投影
}

/**
 * @author jiang peng yong
 * @date 2024/11/4 21:30
 * @email 56002982@qq.com
 * @des 点
 */
data class Point(
    val x: Float,
    val y: Float,
    val z: Float,
)