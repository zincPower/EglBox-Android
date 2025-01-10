package com.jiangpengyong.sample.i_scene.grayscale_terrain

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.util.Range
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.WrapMode
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.program.DrawMode
import com.jiangpengyong.eglbox_filter.program.LightSourceType

/**
 * @author jiang peng yong
 * @date 2025/1/8 22:38
 * @email 56002982@qq.com
 * @des 灰度图滤镜
 */
class GrayscaleTerrainFilter : GLFilter() {
    private var mGrayscaleTerrainData: GrayscaleTerrainData? = null
    private var mLandTexture = GLTexture(
        wrapS = WrapMode.REPEAT,
        wrapT = WrapMode.REPEAT,
    )
    private var mMountainTexture = GLTexture(
        wrapS = WrapMode.REPEAT,
        wrapT = WrapMode.REPEAT,
    )
    private var mSnowTexture = GLTexture(
        wrapS = WrapMode.REPEAT,
        wrapT = WrapMode.REPEAT,
    )

    private var mProgram = GrayscaleTerrainProgram()

    private val mViewMatrix = ViewMatrix()

    override fun onInit(context: FilterContext) {
        mLandTexture.init()
        mMountainTexture.init()
        mSnowTexture.init()

        mProgram.init()

        val space3D = context.space3D
        mViewMatrix.reset()
        mViewMatrix.setLookAtM(
            eyeX = space3D.viewPoint.x, eyeY = space3D.viewPoint.y, eyeZ = space3D.viewPoint.z,
            centerX = 0F, centerY = 0F, centerZ = 0F,
            upx = space3D.upVector.x, upy = space3D.upVector.y, upz = space3D.upVector.z,
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val grayscaleTerrainData = mGrayscaleTerrainData ?: return
        if (!mLandTexture.isInit()) return
        if (!mMountainTexture.isInit()) return
        val texture = imageInOut.texture ?: return

        val space3D = context.space3D
        val vpMatrix = space3D.projectionMatrix * mViewMatrix

        val fbo = context.getTexFBO(texture.width, texture.height)
        fbo.use {
            mProgram.setModelData(grayscaleTerrainData.modelData)
                .setMountainBoundaryRange(grayscaleTerrainData.mountainBoundaryRange)
                .setSnowBoundaryRange(grayscaleTerrainData.snowBoundaryRange)
                .setLandTexture(mLandTexture)
                .setMountainTexture(mMountainTexture)
                .setSnowTexture(mSnowTexture)
//                .setSnowTexture()
                .setMVPMatrix(vpMatrix)
//                .setModelMatrix()
                .setLightPoint(space3D.lightPoint)
                .setViewPoint(space3D.viewPoint)
                .setShininess(50F)
//                .setIsAddAmbientLight(true)
//                .setIsAddDiffuseLight(true)
                .setIsAddSpecularLight(false)
//                .setAmbientLightCoefficient()
//                .setDiffuseLightCoefficient()
//                .setSpecularLightCoefficient()
                .setLightSourceType(LightSourceType.DirectionalLight)
                .setDrawMode(DrawMode.Triangles)
                .draw()
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mGrayscaleTerrainData = null
        mProgram.release()
        mLandTexture.release()
        mMountainTexture.release()
        mSnowTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}

    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            Type.UPDATE_DATA.value -> {
                mGrayscaleTerrainData = message.obj as GrayscaleTerrainData
            }

            Type.UPDATE_LAND_TEXTURE.value -> {
                (message.obj as Bitmap).let {
                    mLandTexture.setData(it)
                    it.recycle()
                }
            }

            Type.UPDATE_MOUNTAIN_TEXTURE.value -> {
                (message.obj as Bitmap).let {
                    mMountainTexture.setData(it)
                    it.recycle()
                }
            }

            Type.UPDATE_SNOW_TEXTURE.value -> {
                (message.obj as Bitmap).let {
                    mSnowTexture.setData(it)
                    it.recycle()
                }
            }
        }
    }

    enum class Type(val value: Int) {
        UPDATE_DATA(10000),
        UPDATE_LAND_TEXTURE(10001),
        UPDATE_MOUNTAIN_TEXTURE(10002),
        UPDATE_SNOW_TEXTURE(10003),
    }

    data class GrayscaleTerrainData(
        val modelData: ModelData,
        val mountainBoundaryRange: Range<Float>,
        val snowBoundaryRange: Range<Float>,
    )

    companion object {
        const val TAG = "GrayscaleTerrainFilter"
    }
}