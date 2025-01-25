package com.jiangpengyong.sample.j_pre_fragment_tests.stencil_test

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.space3d.Scale
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.program.Light
import com.jiangpengyong.eglbox_filter.program.LightProgram
import com.jiangpengyong.eglbox_filter.utils.Obj3DModelLoader
import com.jiangpengyong.sample.App
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:46
 * @email 56002982@qq.com
 * @des 场景滤镜
 */
class SceneFilter : GLFilter() {
    private var mTeapotModelData: ModelData? = null
    private val mTeapotTexture = GLTexture()
    private val mTeapotModelMatrix = ModelMatrix()
    private var mTeapotScaleInfo = Scale(1F, 1F, 1F)

    private var mFilmModelData: ModelData? = null
    private val mFilmTexture = GLTexture()
    private val mFilmModelMatrix = ModelMatrix()
    private var mFilmScaleInfo = Scale(1F, 1F, 1F)

    private val mLightProgram = LightProgram()
    private lateinit var mRingModelData: ModelData
    private val mRingTexture = GLTexture()
    private val mRingModelMatrix = ModelMatrix()

    private var mVPMatrix = GLMatrix()
    private var mViewPoint = Point(0F, 0F, 10F)

    private var mAmbientLightCoefficient = Light(0.3F, 0.3F, 0.3F, 1.0F)
    private var mDiffuseLightCoefficient = Light(0.7F, 0.7F, 0.7F, 1.0F)
    private var mSpecularLightCoefficient = Light(0.6F, 0.6F, 0.6F, 1.0F)

    fun setVPMatrix(matrix: GLMatrix) {
        mVPMatrix = matrix
    }

    fun setViewPoint(point: Point) {
        mViewPoint = point
    }

    fun setAmbientLightCoefficient(coefficient: Light) {
        mAmbientLightCoefficient = coefficient
    }

    fun setDiffuseLightCoefficient(coefficient: Light) {
        mDiffuseLightCoefficient = coefficient
    }

    fun setSpecularLightCoefficient(coefficient: Light) {
        mSpecularLightCoefficient = coefficient
    }

    override fun onInit(context: FilterContext) {
        mLightProgram.init()

        mRingModelData = ModelCreator.createRing(majorSegment = 360)

        BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test-gradient-square.jpg").absolutePath).let { bitmap ->
            mRingTexture.init()
            mRingTexture.setData(bitmap)
            bitmap.recycle()
        }

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

        File(App.context.filesDir, "model/teapot/all/teapot.obj")
            .let { file ->
                mTeapotModelData = Obj3DModelLoader.load(file = file, textureFlip = true)
                val modelData = mTeapotModelData ?: return@let
                val x = modelData.space.right - modelData.space.left
                val y = modelData.space.top - modelData.space.bottom
                val z = modelData.space.far - modelData.space.near
                val max = Math.max(Math.max(x, y), z) / 5
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
        val lightPoint = space3D.lightPoint
        val viewPoint = mViewPoint
        val vpMatrix = mVPMatrix

        mRingModelMatrix.reset()
        mRingModelMatrix.translate(0F, 0.5F, -2F)
        mLightProgram.setModelData(mRingModelData)
        mLightProgram.setModelMatrix(mRingModelMatrix)
        mLightProgram.setMVPMatrix(vpMatrix * mRingModelMatrix)
        mLightProgram.setLightPoint(lightPoint)
        mLightProgram.setViewPoint(viewPoint)
        mLightProgram.setTexture(mRingTexture)
        mLightProgram.setAmbientLightCoefficient(mAmbientLightCoefficient)
        mLightProgram.setDiffuseLightCoefficient(mDiffuseLightCoefficient)
        mLightProgram.setSpecularLightCoefficient(mSpecularLightCoefficient)
        mLightProgram.draw()

        mFilmModelData?.let { modelData ->
            mFilmModelMatrix.reset()
            mFilmModelMatrix.translate(3.5F, 0F, 2F)
            mFilmModelMatrix.scale(mFilmScaleInfo.scaleX, mFilmScaleInfo.scaleY, mFilmScaleInfo.scaleZ)
            mFilmModelMatrix.rotate(-90F, 1F, 0F, 0F)
            mLightProgram.setModelData(modelData)
            mLightProgram.setModelMatrix(mFilmModelMatrix)
            mLightProgram.setMVPMatrix(vpMatrix * mFilmModelMatrix)
            mLightProgram.setLightPoint(lightPoint)
            mLightProgram.setViewPoint(viewPoint)
            mLightProgram.setTexture(mFilmTexture)
            mLightProgram.setAmbientLightCoefficient(mAmbientLightCoefficient)
            mLightProgram.setDiffuseLightCoefficient(mDiffuseLightCoefficient)
            mLightProgram.setSpecularLightCoefficient(mSpecularLightCoefficient)
            mLightProgram.draw()
        }


        mTeapotModelData?.let { modelData ->
            mTeapotModelMatrix.reset()
            mTeapotModelMatrix.translate(-5F, 0F, 0F)
            mTeapotModelMatrix.scale(mTeapotScaleInfo.scaleX, mTeapotScaleInfo.scaleY, mTeapotScaleInfo.scaleZ)
            mLightProgram.setModelData(modelData)
            mLightProgram.setModelMatrix(mTeapotModelMatrix)
            mLightProgram.setMVPMatrix(vpMatrix * mTeapotModelMatrix)
            mLightProgram.setLightPoint(lightPoint)
            mLightProgram.setViewPoint(viewPoint)
            mLightProgram.setTexture(mTeapotTexture)
            mLightProgram.setAmbientLightCoefficient(mAmbientLightCoefficient)
            mLightProgram.setDiffuseLightCoefficient(mDiffuseLightCoefficient)
            mLightProgram.setSpecularLightCoefficient(mSpecularLightCoefficient)
            mLightProgram.draw()
        }
    }

    override fun onRelease(context: FilterContext) {
        mLightProgram.release()
        mTeapotTexture.release()
        mFilmTexture.release()
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