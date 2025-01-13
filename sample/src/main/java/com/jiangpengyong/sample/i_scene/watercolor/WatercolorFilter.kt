package com.jiangpengyong.sample.i_scene.watercolor

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.DepthType
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.MagFilter
import com.jiangpengyong.eglbox_core.gles.MinFilter
import com.jiangpengyong.eglbox_core.space3d.Scale
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_filter.model.ModelData

/**
 * @author jiang peng yong
 * @date 2025/1/13 08:27
 * @email 56002982@qq.com
 * @des 水彩
 */
class WatercolorFilter : GLFilter() {
    private val mProgram = WatercolorProgram()
    private val mColorChartTexture = GLTexture(
        minFilter = MinFilter.NEAREST,
        magFilter = MagFilter.NEAREST,
    )
    private var mModelData: ModelData? = null

    private val mModelMatrix = ModelMatrix()

    private var mModelScaleInfo = Scale(1F, 1F, 1F)

    override fun onInit(context: FilterContext) {
        mColorChartTexture.init()
        mProgram.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val modelData = mModelData ?: return
        val texture = imageInOut.texture ?: return

        val space3D = context.space3D
        val lightPoint = space3D.lightPoint
        val viewPoint = space3D.viewPoint

        val vpMatrix = space3D.projectionMatrix * space3D.viewMatrix
        mModelMatrix.reset()
        mModelMatrix.scale(mModelScaleInfo.scaleX, mModelScaleInfo.scaleY, mModelScaleInfo.scaleZ)
        mModelMatrix.rotate(-90F, 1F, 0F, 0F)
        mModelMatrix.translate(0F, 0F, -(modelData.space.near - modelData.space.far) / 2F)

        val modelMatrix = space3D.gestureMatrix * mModelMatrix

        mProgram.setModelData(modelData)
        mProgram.setColorChartTexture(mColorChartTexture)
        mProgram.setModelMatrix(modelMatrix)
        mProgram.setMVPMatrix(vpMatrix * modelMatrix)
        mProgram.setLightPoint(lightPoint)
        mProgram.setViewPoint(viewPoint)

        val fbo = context.getTexFBO(texture.width, texture.height, depthType = DepthType.Texture)
        fbo.use { mProgram.draw() }
        imageInOut.out(fbo)
    }

    override fun onRelease(context: FilterContext) {
        mColorChartTexture.release()
        mProgram.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}

    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            MessageType.SET_COLOR_CHART.value -> {
                (message.obj as? Bitmap)?.let { bitmap ->
                    mColorChartTexture.setData(bitmap)
                    bitmap.recycle()
                }
            }

            MessageType.SET_MODEL_DATA.value -> {
                (message.obj as? ModelData)?.let { modelData ->
                    mModelData = modelData
                    val x = modelData.space.right - modelData.space.left
                    val y = modelData.space.top - modelData.space.bottom
                    val z = modelData.space.far - modelData.space.near
                    val max = Math.max(Math.max(x, y), z) / 2
                    val scale = 5 / max
                    mModelScaleInfo = Scale(scale, scale, scale)
                }
            }
        }
    }

    enum class MessageType(val value: Int) {
        SET_COLOR_CHART(10000),
        SET_MODEL_DATA(10001),
    }

    companion object {
        const val TAG = "WatercolorFilter"
    }
}