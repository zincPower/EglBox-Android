package com.jiangpengyong.sample.filter

import android.os.Bundle
import com.jiangpengyong.eglbox.Target
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter

class Texture2DFilter : GLFilter() {
    private val mTexture2DProgram = Texture2DProgram(Target.TEXTURE_2D)

    override fun onInit() {
        mTexture2DProgram.init()
    }

    override fun onDraw(context: FilterContext, ) {

    }

    override fun onRelease() {
        mTexture2DProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}

    override fun onRestoreData(restoreData: Bundle) {}

    override fun onSaveData(saveData: Bundle) {}
}