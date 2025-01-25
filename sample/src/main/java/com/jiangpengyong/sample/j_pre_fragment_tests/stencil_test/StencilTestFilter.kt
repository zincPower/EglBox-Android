package com.jiangpengyong.sample.j_pre_fragment_tests.stencil_test

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.blend
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.program.Light
import com.jiangpengyong.eglbox_filter.program.LightProgram
import com.jiangpengyong.sample.App
import java.io.File

class StencilTestFilter : GLFilter() {
    private val mSceneFilter = SceneFilter()
    private val mViewMatrix = ViewMatrix()
    private val mMirrorMatrix = ModelMatrix()
        .apply { scale(1F, -1F, 1F) }

    private val mTableModelData = ModelCreator.createCube()
    private val mTableSurfaceModelData = ModelCreator.createRectangle()
    private val mTableModelMatrix = ModelMatrix()
    private val mTableTexture = GLTexture()

    private val mLightProgram = LightProgram()

    override fun onInit(context: FilterContext) {
        mSceneFilter.init(context)

        BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/wood_smooth.png").absolutePath).let { bitmap ->
            mTableTexture.init()
            mTableTexture.setData(bitmap)
            bitmap.recycle()
        }

        mLightProgram.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return

        val space3D = context.space3D
        val lightPoint = space3D.lightPoint

        val fbo = context.getTexFBO(texture.width, texture.height, depthType = DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)

            val space3D = context.space3D
            val viewPoint = space3D.viewPoint

            mTableModelMatrix.reset()
            mTableModelMatrix.scale(13F, 0.25F, 7F)
            mTableModelMatrix.translate(0F, -0.125F, 0F)
            mLightProgram.setModelData(mTableModelData)
            mLightProgram.setTexture(mTableTexture)
            mLightProgram.setModelMatrix(mTableModelMatrix)
            mLightProgram.setMVPMatrix(space3D.projectionMatrix * mViewMatrix * space3D.gestureMatrix * mTableModelMatrix)
            mLightProgram.setLightPoint(lightPoint)
            mLightProgram.setViewPoint(viewPoint)
            mLightProgram.draw()

            // 开启模板测试
            GLES20.glEnable(GLES20.GL_STENCIL_TEST)
            // 清空模板值，全为 0
            GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT)
            // 设置模板测试参数
            // 总是通过，绘制物体的地方会留下参考值 1
            GLES20.glStencilFunc(GLES20.GL_ALWAYS, 1, 1)
            // 设置模板测试后的参数
            // 因为每次都通过，绘制物体会使用参考值
            GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_REPLACE)

            GLES20.glFrontFace(FrontFace.CCW.value)
            mViewMatrix.reset()
            mViewMatrix.setLookAtM(
                eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
                centerX = 0F, centerY = 0F, centerZ = 0F,
                upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
            )
            mSceneFilter.setVPMatrix(space3D.projectionMatrix * mViewMatrix * space3D.gestureMatrix)
            mSceneFilter.setViewPoint(viewPoint)
            mSceneFilter.setAmbientLightCoefficient(Light(0.3F, 0.3F, 0.3F, 1.0F))
            mSceneFilter.setDiffuseLightCoefficient(Light(0.7F, 0.7F, 0.7F, 1.0F))
            mSceneFilter.setSpecularLightCoefficient(Light(0.6F, 0.6F, 0.6F, 1.0F))
            mSceneFilter.draw(imageInOut)

            GLES20.glStencilFunc(GLES20.GL_ALWAYS, 1, 1)
            GLES20.glStencilOp(GLES20.GL_ZERO, GLES20.GL_ZERO, GLES20.GL_REPLACE)
            mLightProgram.setModelData(mTableSurfaceModelData)
            mLightProgram.setTexture(mTableTexture)
            mLightProgram.setModelMatrix(mTableModelMatrix)
            mLightProgram.setMVPMatrix(space3D.projectionMatrix * mViewMatrix * space3D.gestureMatrix * mTableModelMatrix)
            mLightProgram.setLightPoint(lightPoint)
            mLightProgram.setViewPoint(viewPoint)
            mLightProgram.draw()

            GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 1)
            GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP)
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

            blend(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA) {
                GLES20.glFrontFace(FrontFace.CW.value)
                mSceneFilter.setVPMatrix(space3D.projectionMatrix * mViewMatrix * space3D.gestureMatrix * mMirrorMatrix)
                mSceneFilter.setViewPoint(viewPoint)
                mSceneFilter.setAmbientLightCoefficient(Light(0.3F / 2, 0.3F / 2, 0.3F / 2, 0.35F))
                mSceneFilter.setDiffuseLightCoefficient(Light(0.7F / 2, 0.7F / 2, 0.7F / 2, 0.35F))
                mSceneFilter.setSpecularLightCoefficient(Light(0.6F / 2, 0.6F / 2, 0.6F / 2, 0.35F))
                mSceneFilter.draw(imageInOut)
            }

            GLES20.glDisable(GLES20.GL_STENCIL_TEST)
            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mSceneFilter.release()
        mTableTexture.release()
        mLightProgram.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        const val TAG = "StencilTestFilter"
    }
}