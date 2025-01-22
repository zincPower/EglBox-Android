package com.jiangpengyong.sample.j_pre_fragment_tests.alpha_test

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
import com.jiangpengyong.eglbox_filter.program.LightProgram
import com.jiangpengyong.eglbox_filter.utils.Obj3DModelLoader
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.g_model.Model3DProgram
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:46
 * @email 56002982@qq.com
 * @des 场景滤镜
 */
class SceneFilter : GLFilter() {
    private val model3DProgram = Model3DProgram()

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

    private val mTableModelData = ModelCreator.createCube()
    private val mTableModelMatrix = ModelMatrix()
    private val mTableTexture = GLTexture()

    private var mVPMatrix = GLMatrix()
    private var mViewPoint = Point(0F, 0F, 10F)

    fun setVPMatrix(matrix: GLMatrix) {
        mVPMatrix = matrix
    }

    fun setViewPoint(point: Point) {
        mViewPoint = point
    }

    override fun onInit(context: FilterContext) {
        model3DProgram.init()
        mLightProgram.init()

        mRingModelData = ModelCreator.createRing(majorSegment = 360)

        BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test-gradient-square.jpg").absolutePath).let { bitmap ->
            mRingTexture.init()
            mRingTexture.setData(bitmap)
            bitmap.recycle()
        }

        BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/wood.png").absolutePath).let { bitmap ->
            mTableTexture.init()
            mTableTexture.setData(bitmap)
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
        mLightProgram.draw()

        mFilmModelData?.let { modelData ->
            mFilmModelMatrix.reset()
            mFilmModelMatrix.translate(3.5F, 0F, 2F)
            mFilmModelMatrix.scale(mFilmScaleInfo.scaleX, mFilmScaleInfo.scaleY, mFilmScaleInfo.scaleZ)
            mFilmModelMatrix.rotate(-90F, 1F, 0F, 0F)
            model3DProgram.setModelMatrix(mFilmModelMatrix)
            model3DProgram.setMVPMatrix(vpMatrix * mFilmModelMatrix)
            model3DProgram.setLightPoint(lightPoint)
            model3DProgram.setViewPoint(viewPoint)
            model3DProgram.setTexture(mFilmTexture)
            model3DProgram.setData(
                vertexBuffer = modelData.vertexBuffer,
                textureBuffer = modelData.textureBuffer,
                normalBuffer = modelData.normalBuffer ?: return@let,
                vertexCount = modelData.count,
            )
            model3DProgram.draw()
        }


        mTeapotModelData?.let { modelData ->
            mTeapotModelMatrix.reset()
            mTeapotModelMatrix.translate(-5F, 0F, 0F)
            mTeapotModelMatrix.scale(mTeapotScaleInfo.scaleX, mTeapotScaleInfo.scaleY, mTeapotScaleInfo.scaleZ)
            model3DProgram.setModelMatrix(mTeapotModelMatrix)
            model3DProgram.setMVPMatrix(vpMatrix * mTeapotModelMatrix)
            model3DProgram.setLightPoint(lightPoint)
            model3DProgram.setViewPoint(viewPoint)
            model3DProgram.setTexture(mTeapotTexture)
            model3DProgram.setData(
                vertexBuffer = modelData.vertexBuffer,
                textureBuffer = modelData.textureBuffer,
                normalBuffer = modelData.normalBuffer ?: return@let,
                vertexCount = modelData.count,
            )
            model3DProgram.draw()
        }

        mTableModelMatrix.reset()
        mTableModelMatrix.scale(13F, 0.25F, 7F)
        mTableModelMatrix.translate(0F, -0.125F, 0F)
        mLightProgram.setModelData(mTableModelData)
        mLightProgram.setTexture(mTableTexture)
        mLightProgram.setModelMatrix(mTableModelMatrix)
        mLightProgram.setMVPMatrix(vpMatrix * mTableModelMatrix)
        mLightProgram.setLightPoint(lightPoint)
        mLightProgram.setViewPoint(viewPoint)
        mLightProgram.draw()
    }

    override fun onRelease(context: FilterContext) {
        model3DProgram.release()
        mLightProgram.release()
        mTeapotTexture.release()
        mFilmTexture.release()
        mRingTexture.release()
        mTableTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        private const val TAG = "BlendSceneFilter"
    }
}