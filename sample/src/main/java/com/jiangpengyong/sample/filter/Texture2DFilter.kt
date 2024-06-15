package com.jiangpengyong.sample.filter

import android.os.Bundle
import com.jiangpengyong.eglbox.gles.Target
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter
import com.jiangpengyong.eglbox.filter.ImageInOut
import com.jiangpengyong.eglbox.utils.ModelMatrix

class Texture2DFilter : GLFilter() {
    private val mTexture2DProgram = Texture2DProgram(Target.TEXTURE_2D)
    private val mMatrix = ModelMatrix()

    override fun onInit() {
        mTexture2DProgram.init()
        mMatrix.reset()
        mMatrix.scale(-1F, 1F, 1F)
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        imageInOut.texture?.let { mTexture2DProgram.setTexture(it) }
        mTexture2DProgram.isMirrorY(true)
//        mTexture2DProgram.setScaleType(ScaleType.CENTER_INSIDE)
//        mTexture2DProgram.setScaleType(ScaleType.FIT_XY)
        mTexture2DProgram
            .setVertexMatrix(mMatrix.matrix)
            .setScaleType(ScaleType.MATRIX)
        mTexture2DProgram.setScaleType(ScaleType.CENTER_CROP)
        mContext?.let { mTexture2DProgram.setTargetSize(it.displaySize) }
        mTexture2DProgram.draw()
    }

    override fun onRelease() {
        mTexture2DProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}

    override fun onRestoreData(restoreData: Bundle) {}

    override fun onSaveData(saveData: Bundle) {}
}