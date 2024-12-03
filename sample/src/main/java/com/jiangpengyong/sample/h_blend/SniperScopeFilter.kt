package com.jiangpengyong.sample.h_blend

import android.graphics.BitmapFactory
import android.opengl.GLES20
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
import com.jiangpengyong.eglbox_core.gles.blend
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.Texture2DProgram
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.sample.App
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/11/18 08:48
 * @email 56002982@qq.com
 * @des 狙击镜滤镜，无透明
 */
class SniperScopeFilter : GLFilter() {
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

            blend(GLES20.GL_SRC_COLOR, GLES20.GL_ONE_MINUS_SRC_COLOR) {
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
            }
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
        const val TAG = "SniperScopeFilter"
    }
}

