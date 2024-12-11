package com.jiangpengyong.eglbox_filter.filter

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_filter.program.BallProgram

/**
 * @author jiang peng yong
 * @date 2024/11/26 08:57
 * @email 56002982@qq.com
 * @des 球体滤镜
 */
class BallFilter : GLFilter() {
    private val mProgram = BallProgram()
    private val mModelMatrix = ModelMatrix()

    override fun onInit(context: FilterContext) {
        mProgram.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        imageInOut.texture?.let { texture ->
            val fbo = context.getTexFBO(texture.width, texture.height)
            fbo.use {
                context.texture2DProgram.reset()
                context.texture2DProgram.setTexture(texture)
                context.texture2DProgram.draw()

                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                val space3D = context.space3D
                mModelMatrix.reset()
                val (scaleX, scaleY) = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, space3D.previewSize, Size(texture.width, texture.height))
                mModelMatrix.scale(scaleX, scaleY, 1F)
                val modelMatrix = mModelMatrix * space3D.gestureMatrix
                mProgram.setLightPoint(space3D.lightPoint)
                mProgram.setViewPoint(space3D.viewPoint)
                mProgram.setMVPMatrix(space3D.projectionMatrix * space3D.viewMatrix * modelMatrix)
                mProgram.setModelMatrix(modelMatrix)
                mProgram.draw()
                GLES20.glDisable(GLES20.GL_DEPTH_TEST)
                GLES20.glDisable(GLES20.GL_CULL_FACE)
            }
            fbo.unbindTexture()?.let { imageInOut.out(it) }
        }
    }

    override fun onRelease(context: FilterContext) {
        mProgram.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        const val TAG = "BallFilter"
    }
}