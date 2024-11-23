package com.jiangpengyong.sample.h_blend

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.program.ScaleAlgorithm
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.Texture2DProgram
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_core.space3d.Scale
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.d_light.NormalTypeCubeProgram
import com.jiangpengyong.sample.e_texture.planet.RingProgram
import com.jiangpengyong.sample.g_model.Model3DInfo
import com.jiangpengyong.sample.g_model.Model3DProgram
import com.jiangpengyong.sample.g_model.Obj3DModelLoader
import com.jiangpengyong.sample.utils.toRadians
import java.io.File
import javax.microedition.khronos.opengles.GL
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:48
 * @email 56002982@qq.com
 * @des 混合滤镜
 */
class BlendFilter : GLFilter() {
    private val mSceneFilter = BlendSceneFilter()

    private val mTexture2DProgram = Texture2DProgram(target = Target.TEXTURE_2D)
    private val mSniperScopeTexture = GLTexture()
    private val mSniperScopeMatrix = ModelMatrix()

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

            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_ONE_MINUS_SRC_COLOR)

            val (scaleX, scaleY) = VertexAlgorithmFactory.calculate(
                ScaleType.CENTER_INSIDE,
                Size(mSniperScopeTexture.width, mSniperScopeTexture.height),
                Size(imageInOut.texture?.width ?: 0, imageInOut.texture?.height ?: 0),
            )
            Log.i(TAG, "scaleX=${scaleX} scaleY=${scaleY}")
            mSniperScopeMatrix.reset()
            mSniperScopeMatrix.scale(0.3F, 0.3F, 1F)
            mSniperScopeMatrix.scale(scaleX, scaleY, 1F)
            mTexture2DProgram.setVertexMatrix(mSniperScopeMatrix.matrix)
            mTexture2DProgram.setTexture(mSniperScopeTexture)
            mTexture2DProgram.draw()

            GLES20.glDisable(GLES20.GL_BLEND)
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

        val radius = Math.sqrt((viewPoint.z - centerPoint.z) * (viewPoint.z - centerPoint.z) + (viewPoint.x - centerPoint.x) * (viewPoint.x - centerPoint.x).toDouble())
        val angle = space3D.rotation.angleX
        val x = viewPoint.x + sin(angle.toRadians()) * radius
        val z = viewPoint.z - cos(angle.toRadians()) * radius

        Log.i(TAG, "rotation ${space3D.rotation} viewPoint=${viewPoint} centerPoint=${x},${centerPoint.y},${z} upVector=${space3D.upVector}")
        mViewMatrix.reset()
        mViewMatrix.setLookAtM(
            eyeX = viewPoint.x, eyeY = viewPoint.y, eyeZ = viewPoint.z,
            centerX = x.toFloat(), centerY = centerPoint.y, centerZ = z.toFloat(),
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