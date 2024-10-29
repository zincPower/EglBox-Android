package com.jiangpengyong.eglbox_core.processor.preview

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.filter.SourceFilter
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.MessageType
import com.jiangpengyong.eglbox_core.processor.bean.Angle
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.isValid
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

/**
 * @author: jiang peng yong
 * @date: 2024/8/6 13:01
 * @email: 56002982@qq.com
 * @desc: 上屏接收数据节点
 */
class PreviewSourceFilter : SourceFilter() {
    private val mVertexMatrix = ModelMatrix().apply {
        scale(1F, -1F, 1F)
    }
    private var mPreviewSize = Size(0, 0)
    private val mTexture = GLTexture()

    private var mIsNeedBlank = false

    override fun onInit() {
        mTexture.init()
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        val texture = imageInOut.texture ?: return

        context.previewSize = mPreviewSize

        val texture2DProgram = context.texture2DProgram
        texture2DProgram.reset()
        texture2DProgram.setTexture(texture)
        texture2DProgram.setVertexMatrix(mVertexMatrix.matrix)
        texture2DProgram.setScaleType(ScaleType.MATRIX)

        val fbo = context.getTexFBO(texture.width, texture.height)
        fbo.use {
            texture2DProgram.draw()
        }

        texture2DProgram.reset()

        // 不释放源纹理图片，mTexture 才不会被释放
        imageInOut.out(fbo, false)
    }

    override fun onRelease() {
        mTexture.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
            MessageType.PREVIEW_SET_IMAGE -> {
                mIsNeedBlank = false
                val bitmap = message.obj as? Bitmap
                if (bitmap == null) {
                    Logger.e(TAG, "PREVIEW_SET_IMAGE receive invalid bitmap.")
                    return
                }
                mTexture.setData(bitmap)
                if (message.arg1 == 1) {
                    bitmap.recycle()
                }
                updateTexture(mTexture)
            }

            MessageType.PREVIEW_SET_BLANK -> {
                val width = message.arg1
                val height = message.arg2
                if (width != 0 && height != 0) {
                    mIsNeedBlank = false
                    setBlank(width, height)
                } else if (mPreviewSize.isValid()) {
                    mIsNeedBlank = true
                    setBlank(mPreviewSize.width, mPreviewSize.height)
                } else {
                    mIsNeedBlank = true
                }
            }

            MessageType.SURFACE_CREATED -> {
                mPreviewSize = Size(message.arg1, message.arg2)
                if (mIsNeedBlank) {
                    setBlank(mPreviewSize.width, mPreviewSize.height)
                }
            }

            // TODO 抽到更高
            MessageType.TOUCH_EVENT -> {
                val angle = message.obj as? Angle
                angle ?: return
                mContext?.space3D?.angle = angle
            }

            // TODO 抽到更高
            MessageType.TOUCH_RESET -> {
                mContext?.space3D?.angle = Angle(0F, 0F, 0F)
            }
        }
    }

    private fun setBlank(width: Int, height: Int) {
        mTexture.setData(width, height)
        val fbo = mContext?.getFBO()
        fbo?.bindTexture(mTexture, GLES20.GL_COLOR_ATTACHMENT0) {
            GLES20.glClearColor(0F, 0F, 0F, 1F)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        }
        fbo?.unbindTexture(GLES20.GL_COLOR_ATTACHMENT0)
        fbo?.let { mContext?.recycle(it) }
        this.updateTexture(mTexture)
    }

    companion object {
        const val TAG = "PreviewSourceFilter"
    }
}