package com.jiangpengyong.sample.i_scene.outline

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_filter.model.FrontFace

/**
 * @author jiang peng yong
 * @date 2025/1/15 07:42
 * @email 56002982@qq.com
 * @des 描边滤镜 Group
 */
class OutlineGroupFilter : GLFilter() {
    private val mSceneFilter = SceneFilter()
    private val mOutlineFilter = OutlineFilter()

    override fun onInit(context: FilterContext) {
        mSceneFilter.init(context)
        mOutlineFilter.init(context)
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        val fbo = context.getTexFBO(texture.width, texture.height, depthType = DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            FrontFace.CCW.use()
            mSceneFilter.draw(imageInOut)
            FrontFace.CW.use()
            mOutlineFilter.draw(imageInOut)
            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mSceneFilter.release()
        mOutlineFilter.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object{
        const val TAG = "OutlineGroupFilter"
    }
}