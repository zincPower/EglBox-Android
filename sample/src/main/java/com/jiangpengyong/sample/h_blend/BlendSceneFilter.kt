package com.jiangpengyong.sample.h_blend

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
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.d_light.normal_type.NormalTypeCubeProgram
import com.jiangpengyong.eglbox_filter.program.RingProgram
import com.jiangpengyong.sample.g_model.Model3DInfo
import com.jiangpengyong.sample.g_model.Model3DProgram
import com.jiangpengyong.sample.g_model.Obj3DModelLoader
import com.jiangpengyong.sample.utils.toRadians
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:46
 * @email 56002982@qq.com
 * @des 混合场景滤镜
 */
class BlendSceneFilter : GLFilter() {
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

    private val mTableProgram = NormalTypeCubeProgram()
    private val mTableModelMatrix = ModelMatrix()

    private val mViewMatrix = ViewMatrix()

    override fun onInit(context: FilterContext) {
        model3DProgram.init()
        mRingProgram.init()
        mTableProgram.init()

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
                mTeapotModel3DInfo = Obj3DModelLoader.load(file = file, textureFlip = true)
                val model3DInfo = mTeapotModel3DInfo ?: return@let
                val x = model3DInfo.space.right - model3DInfo.space.left
                val y = model3DInfo.space.top - model3DInfo.space.bottom
                val z = model3DInfo.space.far - model3DInfo.space.near
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
        val centerPoint = space3D.centerPoint
        val viewPoint = space3D.viewPoint

        val radius = viewPoint.z
        val angleX = space3D.rotation.angleX
        val x = viewPoint.x + sin(angleX.toRadians()) * radius
        val z = viewPoint.z - cos(angleX.toRadians()) * radius

        val angleY = space3D.rotation.angleY
        val y = -sin(angleY.toRadians()) * radius

        Log.i(TAG, "rotation ${space3D.rotation} viewPoint=${viewPoint} centerPoint=${x},${y},${z} upVector=${space3D.upVector}")
        mViewMatrix.reset()
        mViewMatrix.setLookAtM(
            eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
            centerX = x.toFloat(), centerY = y.toFloat(), centerZ = z.toFloat(),
            upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
        )

        val vpMatrix = space3D.projectionMatrix * mViewMatrix

        mRingModelMatrix.reset()
        mRingModelMatrix.translate(0F, 0.5F, -2F)
        mRingProgram.setModelMatrix(mRingModelMatrix)
        mRingProgram.setMVPMatrix(vpMatrix * mRingModelMatrix)
        mRingProgram.setLightPoint(lightPoint)
        mRingProgram.setViewPoint(viewPoint)
        mRingProgram.setTexture(mRingTexture)
        mRingProgram.draw()

        mFilmModel3DInfo?.let { model3DInfo ->
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
            model3DProgram.setModelMatrix(mTeapotModelMatrix)
            model3DProgram.setMVPMatrix(vpMatrix * mTeapotModelMatrix)
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

        mTableModelMatrix.reset()
        mTableModelMatrix.scale(13F, 0.25F, 7F)
        mTableModelMatrix.translate(0F, -0.125F, 0F)
        mTableProgram.setModelMatrix(mTableModelMatrix)
        mTableProgram.setMVPMatrix(vpMatrix * mTableModelMatrix)
        mTableProgram.setLightPoint(lightPoint)
        mTableProgram.setViewPoint(viewPoint)
        mTableProgram.draw()
    }

    override fun onRelease(context: FilterContext) {
        model3DProgram.release()
        mRingProgram.release()
        mTableProgram.release()
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