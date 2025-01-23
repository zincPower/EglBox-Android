package com.jiangpengyong.sample.j_pre_fragment_tests.alpha_test

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.sample.App
import java.io.File

/**
 * @author jiang peng yong
 * @date 2025/1/20 23:53
 * @email 56002982@qq.com
 * @des alpha 测试滤镜
 */
class AlphaTestFilter : GLFilter() {
    private val mSceneFilter = SceneFilter()
    private val mViewMatrix = ViewMatrix()

    private val mAlphaTestProgram = AlphaTestProgram()
    private val mWindowStickerTexture = GLTexture()

    override fun onInit(context: FilterContext) {
        mSceneFilter.init(context)
        mAlphaTestProgram.init()
        BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/window_sticker.png").absolutePath).let { bitmap ->
            mWindowStickerTexture.init()
            mWindowStickerTexture.setData(bitmap)
            bitmap.recycle()
        }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        val fbo = context.getTexFBO(texture.width, texture.height, depthType = DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)

            val space3D = context.space3D
            val viewPoint = space3D.viewPoint

            GLES20.glFrontFace(FrontFace.CCW.value)
            mViewMatrix.reset()
            mViewMatrix.setLookAtM(
                eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
                centerX = 0F, centerY = 0F, centerZ = 0F,
                upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
            )
            mSceneFilter.setVPMatrix(space3D.projectionMatrix * mViewMatrix * space3D.gestureMatrix)
            mSceneFilter.setViewPoint(viewPoint)
            mSceneFilter.draw(imageInOut)

            // =============================================== alpha 测试 开始 ==================================================
            GLES20.glFrontFace(FrontFace.CW.value)
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
            mAlphaTestProgram.setTargetSize(Size(texture.width, texture.height))
            mAlphaTestProgram.setTexture(mWindowStickerTexture)
            mAlphaTestProgram.setScaleType(ScaleType.CENTER_INSIDE)
            mAlphaTestProgram.draw()
            // =============================================== alpha 测试 结束 ==================================================

            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mSceneFilter.release()
        mAlphaTestProgram.release()
        mWindowStickerTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        const val TAG = "AlphaTestFilter"
    }
}