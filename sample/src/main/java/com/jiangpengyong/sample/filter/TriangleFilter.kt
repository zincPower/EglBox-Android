package com.jiangpengyong.sample.filter

import android.os.Bundle
import com.jiangpengyong.eglbox.filter.FilterContext
import com.jiangpengyong.eglbox.filter.GLFilter

/**
 * @author jiang peng yong
 * @date 2024/2/11 12:36
 * @email 56002982@qq.com
 * @des 三角形的效果
 */
class TriangleFilter : GLFilter() {

    private val mTriangleProgram = TriangleProgram()

    override fun onInit() {
        mTriangleProgram.init()
    }

//    override fun updateSize(context: FilterContext) {
//        if (!context.isValidSize()) return
//        val ratio = context.width.toFloat() / context.height
//        if (ratio > 1.0) {      // 横图
//            mMVPMatrix.setFrustumM(
//                -ratio, ratio,
//                -1F, 1F,
//                5F, 10F
//            )
//        } else {                // 方图、竖图
//            mMVPMatrix.setFrustumM(
//                -1F, 1F,
//                -ratio, ratio,
//                5F, 10F
//            )
//        }
//    }

    override fun onDraw(context: FilterContext) {
        mTriangleProgram.draw()
    }

    override fun onRelease() {
        mTriangleProgram.release()
    }

    override fun onUpdateData(inputData: Bundle) {}
    override fun onRestoreData(restoreData: Bundle) {}
    override fun onSaveData(saveData: Bundle) {}
}