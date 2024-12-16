package com.jiangpengyong.sample.i_scene.fog

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.program.Texture2DProgram
import com.jiangpengyong.eglbox_core.space3d.Scale
import com.jiangpengyong.eglbox_core.space3d.Space3D
import com.jiangpengyong.eglbox_core.utils.Math3D
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.model.ModelCreator
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.program.Color
import com.jiangpengyong.eglbox_filter.program.FogProgram
import com.jiangpengyong.eglbox_filter.program.LightCalculateType
import com.jiangpengyong.eglbox_filter.utils.Obj3DModelLoader
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.d_light.normal_type.NormalTypeCubeProgram
import com.jiangpengyong.sample.utils.toRadians
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

class FogFilter : GLFilter() {
    private val mSceneFilter = FogSceneFilter()

    private val mTexture2DProgram = Texture2DProgram(target = Target.TEXTURE_2D)
    private val mSniperScopeTexture = GLTexture()

    override fun onInit(context: FilterContext) {
        mContext?.let { mSceneFilter.init(it) }
        mTexture2DProgram.init()
        BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/sniper_scope_1.png").absolutePath).let { bitmap ->
            mSniperScopeTexture.init()
            mSniperScopeTexture.setData(bitmap)
            bitmap.recycle()
        }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return
        val fbo = context.getTexFBO(texture.width, texture.height, DepthType.Texture)
        fbo.use {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)

            mSceneFilter.draw(imageInOut)

            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mSceneFilter.release()
        mTexture2DProgram.release()
        mSniperScopeTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    companion object {
        const val TAG = "FogFilter"
    }
}

class FogSceneFilter : GLFilter() {
    private var mFogProgram = FogProgram(lightCalculateType = LightCalculateType.Fragment)

    private var mTeapotModelData: ModelData? = null
    private val mTeapotTexture = GLTexture()
    private val mTeapotModelMatrix = ModelMatrix()
    private var mTeapotScaleInfo = Scale(1F, 1F, 1F)

    private var mFilmModelData: ModelData? = null
    private val mFilmTexture = GLTexture()
    private val mFilmModelMatrix = ModelMatrix()
    private var mFilmScaleInfo = Scale(1F, 1F, 1F)

    private var mRingModelData = ModelCreator.createRing()

    private val mRingTexture = GLTexture()
    private val mRingModelMatrix = ModelMatrix()

    private val mTableProgram = NormalTypeCubeProgram()
    private val mTableModelMatrix = ModelMatrix()

    private val mViewMatrix = ViewMatrix()

    override fun onInit(context: FilterContext) {
        mFogProgram.init()
        mTableProgram.init()

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
        val viewPoint = space3D.viewPoint

        calculateFogRange(space3D)

        val radius = viewPoint.z
        val angleX = space3D.rotation.angleX
        val x = viewPoint.x + sin(angleX.toRadians()) * radius
        val z = viewPoint.z - cos(angleX.toRadians()) * radius

        val angleY = space3D.rotation.angleY
        val y = -sin(angleY.toRadians()) * radius

        mViewMatrix.reset()
        mViewMatrix.setLookAtM(
            eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
            centerX = x.toFloat(), centerY = y.toFloat(), centerZ = z.toFloat(),
            upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
        )

        val vpMatrix = space3D.projectionMatrix * mViewMatrix

        mRingModelMatrix.reset()
        mRingModelMatrix.translate(0F, 0.5F, -2F)
        mFogProgram.setModelData(mRingModelData)
        mFogProgram.setModelMatrix(mRingModelMatrix)
        mFogProgram.setMVPMatrix(vpMatrix * mRingModelMatrix)
        mFogProgram.setLightPoint(lightPoint)
        mFogProgram.setViewPoint(viewPoint)
        mFogProgram.setTexture(mRingTexture)
        mFogProgram.draw()

        mFilmModelData?.let { modelData ->
            mFilmModelMatrix.reset()
            mFilmModelMatrix.translate(3.5F, 0F, 2F)
            mFilmModelMatrix.scale(mFilmScaleInfo.scaleX, mFilmScaleInfo.scaleY, mFilmScaleInfo.scaleZ)
            mFilmModelMatrix.rotate(-90F, 1F, 0F, 0F)
            mFogProgram.setModelData(modelData)
            mFogProgram.setModelMatrix(mFilmModelMatrix)
            mFogProgram.setMVPMatrix(vpMatrix * mFilmModelMatrix)
            mFogProgram.setLightPoint(lightPoint)
            mFogProgram.setViewPoint(viewPoint)
            mFogProgram.setTexture(mFilmTexture)
            mFogProgram.draw()
        }

        mTeapotModelData?.let { modelData ->
            mTeapotModelMatrix.reset()
            mTeapotModelMatrix.translate(-5F, 0F, 0F)
            mTeapotModelMatrix.scale(mTeapotScaleInfo.scaleX, mTeapotScaleInfo.scaleY, mTeapotScaleInfo.scaleZ)
            mFogProgram.setModelData(modelData)
            mFogProgram.setModelMatrix(mTeapotModelMatrix)
            mFogProgram.setMVPMatrix(vpMatrix * mTeapotModelMatrix)
            mFogProgram.setLightPoint(lightPoint)
            mFogProgram.setViewPoint(viewPoint)
            mFogProgram.setTexture(mTeapotTexture)
            mFogProgram.draw()
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
        mFogProgram.release()
        mTableProgram.release()
        mTeapotTexture.release()
        mFilmTexture.release()
        mRingTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}

    private fun calculateFogRange(space3D: Space3D) {
        val viewPoint = space3D.viewPoint
        val centerPoint = space3D.centerPoint
        val distance = Math3D.length(centerPoint, viewPoint)
        mFogProgram.setFogRange(distance - 2F, distance + 2F)
        mFogProgram.setFogColor(Color(1F, 1F, 1F, 1F))
    }

    companion object {
        private const val TAG = "FogSceneFilter"
    }
}
