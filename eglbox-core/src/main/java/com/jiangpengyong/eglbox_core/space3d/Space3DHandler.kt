package com.jiangpengyong.eglbox_core.space3d

import android.os.Message

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
            Space3DMessageType.UPDATE_ROTATION-> {
                space3D.rotation = message.obj as? Rotation ?: return
            }

            Space3DMessageType.UPDATE_SCALE-> {
                space3D.scale = message.obj as? Scale ?: return
            }

            Space3DMessageType.UPDATE_VIEWPOINT -> {
                space3D.viewPoint = message.obj as? Point ?: return
            }

            Space3DMessageType.UPDATE_CENTER_POINT -> {
                space3D.centerPoint = message.obj as? Point ?: return
            }

            Space3DMessageType.UPDATE_UP_VECTOR -> {
                space3D.upVector = message.obj as? Vector ?: return
            }

            Space3DMessageType.UPDATE_PROJECTION -> {
                space3D.projection = message.obj as? Projection ?: return
            }

            Space3DMessageType.UPDATE_LIGHT_POINT -> {
                space3D.lightPoint = message.obj as? Point ?: return
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
    const val UPDATE_SCALE = -20003  // 更新缩放系数
    const val UPDATE_VIEWPOINT = -20005 // 更新观察点
    const val UPDATE_CENTER_POINT = -20006 // 更新被观察点
    const val UPDATE_UP_VECTOR = -20007 // 更新观察点向上向量
    const val UPDATE_PROJECTION = -20008 // 更新投影
    const val UPDATE_LIGHT_POINT = -20009 // 更新光照

    fun obtainUpdateRotationMessage(angleX: Float, angleY: Float, angleZ: Float): Message = Message.obtain().apply {
        what = UPDATE_ROTATION
        obj = Rotation(angleX = angleX, angleY = angleY, angleZ = angleZ)
    }

    fun obtainUpdateScaleMessage(scaleX: Float, scaleY: Float, scaleZ: Float): Message = Message.obtain().apply {
        what = UPDATE_SCALE
        obj = Scale(scaleX = scaleX, scaleY = scaleY, scaleZ = scaleZ)
    }

    fun obtainUpdateViewpointMessage(x: Float, y: Float, z: Float): Message = Message.obtain().apply {
        what = UPDATE_VIEWPOINT
        obj = Point(x = x, y = y, z = z)
    }

    fun obtainUpdateCenterPointMessage(x: Float, y: Float, z: Float): Message = Message.obtain().apply {
        what = UPDATE_CENTER_POINT
        obj = Point(x = x, y = y, z = z)
    }

    fun obtainUpdateUpVectorMessage(x: Float, y: Float, z: Float): Message = Message.obtain().apply {
        what = UPDATE_UP_VECTOR
        obj = Vector(x = x, y = y, z = z)
    }

    fun obtainUpdateProjectionMessage(type: ProjectionType, near: Float, far: Float, ratio: Float): Message = Message.obtain().apply {
        what = UPDATE_PROJECTION
        obj = Projection(type = type, near = near, far = far, ratio = ratio)
    }

    fun obtainUpdateLightPointMessage(x: Float, y: Float, z: Float): Message = Message.obtain().apply {
        what = UPDATE_LIGHT_POINT
        obj = Point(x = x, y = y, z = z)
    }
}