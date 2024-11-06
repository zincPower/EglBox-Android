package com.jiangpengyong.eglbox_core.space3d

import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.MessageType

/**
 * @author jiang peng yong
 * @date 2024/11/5 22:41
 * @email 56002982@qq.com
 * @des 3D 信息处理器
 */
class Space3DHandler {
    var space3D: Space3D? = null

    fun handleMessage(message: Message) {
        val space3D = space3D ?: return
        when (message.what) {
            Space3DMessageType.UPDATE_ROTATION, Space3DMessageType.RESET_ROTATION -> {
                space3D.rotation = message.obj as? Rotation ?: return
            }

            Space3DMessageType.UPDATE_VIEWPOINT -> {
                space3D.viewPoint = message.obj as? Point ?: return
            }

            Space3DMessageType.UPDATE_CENTER_POINT -> {
                space3D.centerPoint = message.obj as? Point ?: return
            }

            Space3DMessageType.UPDATE_UP_VECTOR -> {
                space3D.upVector = message.obj as? Point ?: return
            }

            Space3DMessageType.UPDATE_PROJECTION -> {
                space3D.projection = message.obj as? Projection ?: return
            }

            MessageType.SURFACE_CREATED -> {
                val width = message.arg1
                val height = message.arg2
                space3D.previewSize = Size(width, height)
            }

            MessageType.SURFACE_CHANGED -> {
                val width = message.arg1
                val height = message.arg2
                space3D.previewSize = Size(width, height)
            }

            MessageType.SURFACE_DESTROY -> {
                space3D.previewSize = Size(0, 0)
            }
        }
    }

    companion object {
        private const val TAG = "Space3DHandler"
    }
}

/**
 * @author jiang peng yong
 * @date 2024/11/4 20:38
 * @email 56002982@qq.com
 * @des 3D 信息
 */
object Space3DMessageType {
    const val UPDATE_ROTATION = -20001  // 更新旋转角度
    const val RESET_ROTATION = -20002   // 重置旋转角度
    const val UPDATE_VIEWPOINT = -20003 // 更新观察点
    const val UPDATE_CENTER_POINT = -20004 // 更新被观察点
    const val UPDATE_UP_VECTOR = -20005 // 更新观察点向上向量
    const val UPDATE_PROJECTION = -20006 // 更新投影

    fun obtainUpdateRotationMessage(angleX: Float, angleY: Float, angleZ: Float) = Message.obtain().apply {
        what = UPDATE_ROTATION
        obj = Rotation(angleX = angleX, angleY = angleY, angleZ = angleZ)
    }

    fun obtainResetRotationMessage() = Message.obtain().apply {
        what = RESET_ROTATION
        obj = Rotation(angleX = 0F, angleY = 0F, angleZ = 0F)
    }

    fun obtainUpdateViewpointMessage(x: Float, y: Float, z: Float) = Message.obtain().apply {
        what = UPDATE_VIEWPOINT
        obj = Point(x = x, y = y, z = z)
    }

    fun obtainUpdateCenterPointMessage(x: Float, y: Float, z: Float) = Message.obtain().apply {
        what = UPDATE_CENTER_POINT
        obj = Point(x = x, y = y, z = z)
    }

    fun obtainUpdateUpVectorMessage(x: Float, y: Float, z: Float) = Message.obtain().apply {
        what = UPDATE_UP_VECTOR
        obj = Point(x = x, y = y, z = z)
    }

    fun obtainUpdateProjectionMessage(type: ProjectionType, near: Float, far: Float, ratio: Float) = Message.obtain().apply {
        what = UPDATE_PROJECTION
        obj = Projection(type = type, near = near, far = far, ratio = ratio)
    }
}