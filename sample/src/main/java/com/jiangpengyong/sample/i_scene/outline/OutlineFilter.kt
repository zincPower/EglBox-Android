package com.jiangpengyong.sample.i_scene.outline

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import android.util.Log
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.space3d.Scale
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.utils.Obj3DModelLoader
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.utils.toRadians
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:46
 * @email 56002982@qq.com
 * @des 描边滤镜
 */
class OutlineFilter : GLFilter() {
    private val mOutlineProgram = OutlineProgram()

    private var mTeapotModelData: ModelData? = null
    private val mTeapotTexture = GLTexture()
    private val mTeapotModelMatrix = ModelMatrix()
    private var mTeapotScaleInfo = Scale(1F, 1F, 1F)

    private var mFilmModelData: ModelData? = null
    private val mFilmTexture = GLTexture()
    private val mFilmModelMatrix = ModelMatrix()
    private var mFilmScaleInfo = Scale(1F, 1F, 1F)

    private lateinit var mRingModelData: ModelData
    private val mRingTexture = GLTexture()
    private val mRingModelMatrix = ModelMatrix()

    private val mTableModelData = ModelCreator.createCube()
    private val mTableModelMatrix = ModelMatrix()
    private val mTableTexture = GLTexture()

    private val mViewMatrix = ViewMatrix()

    override fun onInit(context: FilterContext) {
        mOutlineProgram.init()

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
        val viewPoint = space3D.viewPoint

        val radius = viewPoint.z
        val angleX = space3D.rotation.angleX
        val x = viewPoint.x + sin(angleX.toRadians()) * radius
        val z = viewPoint.z - cos(angleX.toRadians()) * radius

        val angleY = space3D.rotation.angleY
        val y = -sin(angleY.toRadians()) * radius

        Log.i(TAG, "rotation ${space3D.rotation} centerPoint=${x},${y},${z} upVector=${space3D.upVector}")
        mViewMatrix.reset()
        mViewMatrix.setLookAtM(
            eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
            centerX = x.toFloat(), centerY = y.toFloat(), centerZ = z.toFloat(),
            upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
        )

        val vpMatrix = space3D.projectionMatrix * mViewMatrix

//        mRingModelData.let { modelData ->
//            mRingModelMatrix.reset()
//            mRingModelMatrix.translate(0F, 0.5F, -2F)
//            mOutlineProgram.setMVPMatrix(vpMatrix * mRingModelMatrix)
//            mOutlineProgram.setData(
//                vertexBuffer = modelData.vertexBuffer,
//                normalBuffer = modelData.normalBuffer ?: return@let,
//                vertexCount = modelData.count,
//            )
//            mOutlineProgram.draw()
//        }

        mFilmModelData?.let { modelData ->
            mFilmModelMatrix.reset()
            mFilmModelMatrix.translate(3.5F, 0F, 2F)
            mFilmModelMatrix.scale(mFilmScaleInfo.scaleX, mFilmScaleInfo.scaleY, mFilmScaleInfo.scaleZ)
            mFilmModelMatrix.rotate(-90F, 1F, 0F, 0F)
            mOutlineProgram.setMVPMatrix(vpMatrix * mFilmModelMatrix)
            mOutlineProgram.setData(
                vertexBuffer = modelData.vertexBuffer,
                normalBuffer = modelData.normalBuffer ?: return@let,
                vertexCount = modelData.count,
            )
            mOutlineProgram.draw()
        }

        mTeapotModelData?.let { modelData ->
            mTeapotModelMatrix.reset()
            mTeapotModelMatrix.translate(-5F, 0F, 0F)
            mTeapotModelMatrix.scale(mTeapotScaleInfo.scaleX, mTeapotScaleInfo.scaleY, mTeapotScaleInfo.scaleZ)
            mOutlineProgram.setMVPMatrix(vpMatrix * mTeapotModelMatrix)
            mOutlineProgram.setData(
                vertexBuffer = modelData.vertexBuffer,
                normalBuffer = modelData.normalBuffer ?: return@let,
                vertexCount = modelData.count,
            )
            mOutlineProgram.draw()
        }

//        mTableModelData.let { modelData ->
//            mTableModelMatrix.reset()
//            mTableModelMatrix.scale(13F, 0.25F, 7F)
//            mTableModelMatrix.translate(0F, -0.125F, 0F)
//            mOutlineProgram.setMVPMatrix(vpMatrix * mTableModelMatrix)
//            mOutlineProgram.setData(
//                vertexBuffer = modelData.vertexBuffer,
//                normalBuffer = modelData.normalBuffer ?: return@let,
//                vertexCount = modelData.count,
//            )
//            mOutlineProgram.draw()
//        }
    }

    override fun onRelease(context: FilterContext) {
        mOutlineProgram.release()
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
        private const val TAG = "OutlineFilter"
    }
}