package com.jiangpengyong.sample.j_pre_fragment_tests.clipping_plane

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Scale
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.utils.Obj3DModelLoader
import com.jiangpengyong.sample.App
import java.io.File

/**
 * @author jiang peng yong
 * @date 2025/1/25 13:28
 * @email 56002982@qq.com
 * @des 任意平面裁剪
 */
class ClippingPlaneFilter : GLFilter() {
    private val mLightProgram = ClippingPlaneProgram()

    private var mFilmModelData: ModelData? = null
    private val mFilmTexture = GLTexture()
    private val mFilmModelMatrix = ModelMatrix()
    private var mFilmScaleInfo = Scale(1F, 1F, 1F)

    override fun onInit(context: FilterContext) {
        File(App.context.filesDir, "model/film/film.obj")
            .let { file ->
                mFilmModelData = Obj3DModelLoader.load(file = file, textureFlip = true)
                val modelData = mFilmModelData ?: return@let
                val x = modelData.space.right - modelData.space.left
                val y = modelData.space.top - modelData.space.bottom
                val z = modelData.space.far - modelData.space.near
                val max = Math.max(Math.max(x, y), z) / 2
                mFilmScaleInfo = Scale(1 / max, 1 / max, 1 / max)
            }
        BitmapFactory.decodeFile(File(App.context.filesDir, "model/film/film.jpg").absolutePath).let { bitmap ->
            mFilmTexture.init()
            mFilmTexture.setData(bitmap)
            bitmap.recycle()
        }

        mLightProgram.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        val fbo = context.getTexFBO(texture.width, texture.height, depthType = DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)

            val space3D = context.space3D
            val viewPoint = space3D.viewPoint

            mFilmModelData?.let { modelData ->
                mFilmModelMatrix.reset()
                mFilmModelMatrix.scale(mFilmScaleInfo.scaleX * 2, mFilmScaleInfo.scaleY * 2, mFilmScaleInfo.scaleZ * 2)
                mFilmModelMatrix.rotate(-90F, 1F, 0F, 0F)
                mFilmModelMatrix.translate(0F, 0F, (modelData.space.far - modelData.space.near) / 2)
                mLightProgram.setModelMatrix(mFilmModelMatrix)
                mLightProgram.setMVPMatrix(space3D.projectionMatrix * space3D.viewMatrix * space3D.gestureMatrix * mFilmModelMatrix)
                mLightProgram.setLightPoint(space3D.lightPoint)
                mLightProgram.setViewPoint(viewPoint)
                mLightProgram.setTexture(mFilmTexture)
                mLightProgram.setModelData(modelData)
                mLightProgram.draw()
            }

            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mLightProgram.init()
        mFilmTexture.init()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        const val TAG = "ClippingPlaneFilter"
    }
}

data class ClippingPlane(
    val a: Float,
    val b: Float,
    val c: Float,
    val d: Float,
)