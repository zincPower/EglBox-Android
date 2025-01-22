package com.jiangpengyong.sample.j_pre_fragment_tests.scissor_test

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.ViewMatrix

/**
 * @author jiang peng yong
 * @date 2025/1/20 23:53
 * @email 56002982@qq.com
 * @des 裁剪测试滤镜
 */
class ScissorTestFilter : GLFilter() {
    private val mSceneFilter = SceneFilter()
    private val mViewMatrix = ViewMatrix()

    override fun onInit(context: FilterContext) {
        mSceneFilter.init(context)
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        val fbo = context.getTexFBO(texture.width, texture.height, depthType = DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)

            val space3D = context.space3D
            val viewPoint = space3D.viewPoint

            mViewMatrix.reset()
            mViewMatrix.setLookAtM(
                eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
                centerX = 0F, centerY = 0F, centerZ = 0F,
                upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
            )
            mSceneFilter.setVPMatrix(space3D.projectionMatrix * mViewMatrix * space3D.gestureMatrix)
            mSceneFilter.setViewPoint(viewPoint)
            mSceneFilter.draw(imageInOut)

            // =============================================== 裁剪测试 开始 ==================================================
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
            GLES20.glViewport(texture.width / 3, 0, texture.width / 3, texture.height / 3)
            GLES20.glScissor(texture.width / 3, 0, texture.width / 3, texture.height / 3)
            GLES20.glClearColor(0.2F, 0.3F, 0.3F, 1.0F)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            mViewMatrix.reset()
            mViewMatrix.setLookAtM(
                eyeX = 0F, eyeY = 50F, eyeZ = 0F,
                centerX = 0F, centerY = 0F, centerZ = 0F,
                upx = 0F, upy = 0F, upz = -1F,
            )
            mSceneFilter.setVPMatrix(space3D.projectionMatrix * mViewMatrix * space3D.gestureMatrix)
            mSceneFilter.setViewPoint(Point(0F, 65F, 0F))
            mSceneFilter.draw(imageInOut)

            GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
            // =============================================== 裁剪测试 结束 ==================================================

            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mSceneFilter.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        const val TAG = "ScissorTestFilter"
    }
}