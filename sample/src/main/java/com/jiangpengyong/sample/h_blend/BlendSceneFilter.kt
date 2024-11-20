package com.jiangpengyong.sample.h_blend

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.space3d.Scale
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.e_texture.planet.RingProgram
import com.jiangpengyong.sample.g_model.Model3DInfo
import com.jiangpengyong.sample.g_model.Model3DProgram
import com.jiangpengyong.sample.g_model.Obj3DModelLoader
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:48
 * @email 56002982@qq.com
 * @des 混合滤镜
 */
class BlendFilter : GLFilter() {
    private val mSceneFilter = BlendSceneFilter()

    override fun onInit(context: FilterContext) {
        mContext?.let { mSceneFilter.init(it) }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        val fbo = context.getTexFBO(texture.width, texture.height, DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            mSceneFilter.draw(imageInOut)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            GLES20.glDisable(GLES20.GL_CULL_FACE)
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
        const val TAG = "BlendFilter"
    }
}

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:46
 * @email 56002982@qq.com
 * @des 混合场景滤镜
 */
private class BlendSceneFilter : GLFilter() {
    private val model3DProgram = Model3DProgram()

    private var mTeapotModel3DInfo: Model3DInfo? = null
    private val mTeapotTexture = GLTexture()
    private val mTeapotModelMatrix = ModelMatrix()
    private var mTeapotScaleInfo = Scale(1F, 1F, 1F)

    private var mFilmModel3DInfo: Model3DInfo? = null
    private val mFilmTexture = GLTexture()
    private val mFilmModelMatrix = ModelMatrix()
    private var mFilmScaleInfo = Scale(1F, 1F, 1F)

    private val mRingProgram = RingProgram()
    private val mRingTexture = GLTexture()
    private val mRingModelMatrix = ModelMatrix()

    override fun onInit(context: FilterContext) {
        model3DProgram.init()
        mRingProgram.init()

        BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test-gradient-square.jpg").absolutePath).let { bitmap ->
            mRingTexture.init()
            mRingTexture.setData(bitmap)
            bitmap.recycle()
        }

        File(App.context.filesDir, "model/film/film.obj")
            .let { file ->
                mFilmModel3DInfo = Obj3DModelLoader.load(file = file, textureFlip = true)
                val model3DInfo = mFilmModel3DInfo ?: return@let
                val x = model3DInfo.space.right - model3DInfo.space.left
                val y = model3DInfo.space.top - model3DInfo.space.bottom
                val z = model3DInfo.space.far - model3DInfo.space.near
                val max = Math.max(Math.max(x, y), z) / 4
                mFilmScaleInfo = Scale(1 / max, 1 / max, 1 / max)
            }
        BitmapFactory.decodeFile(File(App.context.filesDir, "model/film/film.jpg").absolutePath).let { bitmap ->
            mFilmTexture.init()
            mFilmTexture.setData(bitmap)
            bitmap.recycle()
        }

        File(App.context.filesDir, "model/teapot/all/teapot.obj")
            .let { file ->
                mTeapotModel3DInfo = Obj3DModelLoader.load(file = file, textureFlip = true)
                val model3DInfo = mTeapotModel3DInfo ?: return@let
                val x = model3DInfo.space.right - model3DInfo.space.left
                val y = model3DInfo.space.top - model3DInfo.space.bottom
                val z = model3DInfo.space.far - model3DInfo.space.near
                val max = Math.max(Math.max(x, y), z) / 4
                mTeapotScaleInfo = Scale(1 / max, 1 / max, 1 / max)
            }
        BitmapFactory.decodeFile(File(App.context.filesDir, "model/teapot/all/teapot.png").absolutePath).let { bitmap ->
            mTeapotTexture.init()
            mTeapotTexture.setData(bitmap)
            bitmap.recycle()
        }

    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val space3D = context.space3D
        val gestureMatrix = space3D.gestureMatrix
        val vpMatrix = space3D.projectionMatrix * space3D.viewMatrix
        val lightPoint = space3D.lightPoint
        val viewPoint = space3D.viewPoint

        mRingModelMatrix.reset()
        mRingModelMatrix.translate(0F,0.5F,-2F)
        val modelMatrix = gestureMatrix*mRingModelMatrix
        mRingProgram.setModelMatrix(modelMatrix)
        mRingProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mRingProgram.setLightPoint(lightPoint)
        mRingProgram.setViewPoint(viewPoint)
        mRingProgram.setTexture(mRingTexture)
        mRingProgram.draw()

        mFilmModel3DInfo?.let { model3DInfo ->
            mFilmModelMatrix.reset()
            mFilmModelMatrix.translate(3.5F, 0F, 2F)
            mFilmModelMatrix.scale(mFilmScaleInfo.scaleX, mFilmScaleInfo.scaleY, mFilmScaleInfo.scaleZ)
            mFilmModelMatrix.rotate(-90F, 1F, 0F, 0F)
            val modelMatrix = gestureMatrix * mFilmModelMatrix
            model3DProgram.setModelMatrix(modelMatrix)
            model3DProgram.setMVPMatrix(vpMatrix * modelMatrix)
            model3DProgram.setLightPoint(lightPoint)
            model3DProgram.setViewPoint(viewPoint)
            model3DProgram.setTexture(mFilmTexture)
            model3DProgram.setData(
                vertexBuffer = model3DInfo.vertexBuffer,
                textureBuffer = model3DInfo.textureBuffer,
                normalBuffer = model3DInfo.normalBuffer ?: return@let,
                vertexCount = model3DInfo.count,
            )
            model3DProgram.draw()
        }


        mTeapotModel3DInfo?.let { model3DInfo ->
            mTeapotModelMatrix.reset()
            mTeapotModelMatrix.translate(-5F, 0F, 0F)
            mTeapotModelMatrix.scale(mTeapotScaleInfo.scaleX, mTeapotScaleInfo.scaleY, mTeapotScaleInfo.scaleZ)
            val modelMatrix = gestureMatrix * mTeapotModelMatrix
            model3DProgram.setModelMatrix(modelMatrix)
            model3DProgram.setMVPMatrix(vpMatrix * modelMatrix)
            model3DProgram.setLightPoint(lightPoint)
            model3DProgram.setViewPoint(viewPoint)
            model3DProgram.setTexture(mTeapotTexture)
            model3DProgram.setData(
                vertexBuffer = model3DInfo.vertexBuffer,
                textureBuffer = model3DInfo.textureBuffer,
                normalBuffer = model3DInfo.normalBuffer ?: return@let,
                vertexCount = model3DInfo.count,
            )
            model3DProgram.draw()
        }

    }

    override fun onRelease(context: FilterContext) {
        model3DProgram.release()
        mRingProgram.release()
        mRingTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        private const val TAG = "BlendSceneFilter"
    }
}