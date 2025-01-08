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

class GrayscaleTerrainFilter : GLFilter() {
    private var mGrayscaleTerrainData: GrayscaleTerrainData? = null
    private var mGrassTexture = GLTexture(
        wrapS = WrapMode.REPEAT,
        wrapT = WrapMode.REPEAT,
    )
    private var mRockTexture = GLTexture(
        wrapS = WrapMode.REPEAT,
        wrapT = WrapMode.REPEAT,
    )

    private var mProgram = GrayscaleTerrainProgram()

    private val mViewMatrix = ViewMatrix()

    override fun onInit(context: FilterContext) {
        mGrassTexture.init()
        mRockTexture.init()

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
        if (!mGrassTexture.isInit()) return
        if (!mRockTexture.isInit()) return
        val texture = imageInOut.texture ?: return

        val space3D = context.space3D
        val vpMatrix = space3D.projectionMatrix * mViewMatrix

        val fbo = context.getTexFBO(texture.width, texture.height)
        fbo.use {
            mProgram.setModelData(grayscaleTerrainData.modelData)
            mProgram.setBoundaryRange(grayscaleTerrainData.boundaryRange)
            mProgram.setLandTexture(mGrassTexture)
            mProgram.setMountainTexture(mRockTexture)
//                .setSnowTexture()
            mProgram.setMVPMatrix(vpMatrix)
//                .setModelMatrix()
            mProgram.setLightPoint(space3D.lightPoint)
            mProgram.setViewPoint(space3D.viewPoint)
            mProgram.setShininess(50F)
//                .setIsAddAmbientLight(true)
//                .setIsAddDiffuseLight(true)
//                .setIsAddSpecularLight(true)
//                .setAmbientLightCoefficient()
//                .setDiffuseLightCoefficient()
//                .setSpecularLightCoefficient()
            mProgram.setLightSourceType(LightSourceType.DirectionalLight)
            mProgram.setDrawMode(DrawMode.Triangles)

            mProgram.draw()
        }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mProgram.release()

        mGrayscaleTerrainData = null
        mGrassTexture.release()
        mRockTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}

    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            Type.UPDATE_DATA.value -> {
                mGrayscaleTerrainData = message.obj as GrayscaleTerrainData
            }

            Type.UPDATE_GRASS_TEXTURE.value -> {
                (message.obj as Bitmap).let {
                    mGrassTexture.setData(it)
                    it.recycle()
                }
            }

            Type.UPDATE_ROCK_TEXTURE.value -> {
                (message.obj as Bitmap).let {
                    mRockTexture.setData(it)
                    it.recycle()
                }
            }
        }
    }

    enum class Type(val value: Int) {
        UPDATE_DATA(10000),
        UPDATE_GRASS_TEXTURE(10001),
        UPDATE_ROCK_TEXTURE(10002),
    }

    data class GrayscaleTerrainData(
        val modelData: ModelData,
        val boundaryRange: Range<Float>,
    )

    companion object {
        const val TAG = "GrayscaleTerrainFilter"
    }
}