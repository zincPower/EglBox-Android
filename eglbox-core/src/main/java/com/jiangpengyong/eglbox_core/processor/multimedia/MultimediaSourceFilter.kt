package com.jiangpengyong.eglbox_core.processor.multimedia

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.filter.SourceFilter
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.gles.Target
import com.jiangpengyong.eglbox_core.processor.CommonMessageType
import com.jiangpengyong.eglbox_core.program.Texture2DProgram
import com.jiangpengyong.eglbox_core.program.isValid
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

class MultimediaSourceFilter : SourceFilter() {
    private val mTexture = GLTexture(Target.EXTERNAL_OES)
    private val mTexture2DProgram = Texture2DProgram(Target.EXTERNAL_OES)
    private val mStreamSnapshot = StreamSnapshot()

    private var mSurfaceTexture: SurfaceTexture? = null

    private val mMatrix = ModelMatrix()
    private val mPreviewSize = Size(0, 0)

    private var mIsNeedCalculate = true
    private var mScaleX = 1F
    private var mScaleY = 1F
    private var mAngle = 0F
    private var mXMirror = false
    private var mYMirror = false

    override fun onInit() {
        mTexture.init()
        mTexture2DProgram.init()
        mTexture2DProgram.setTexture(mTexture)
        mStreamSnapshot.setFilterId(id)
        updateTexture(mTexture)
        mSurfaceTexture = SurfaceTexture(mTexture.id)
        mSurfaceTexture?.setOnFrameAvailableListener { handleFrameAvailable() }
        Message.obtain().apply {
            this.what = CommonMessageType.SURFACE_CREATED
            this.obj = mSurfaceTexture
            mContext?.sendMessage(id, this)
        }
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        if (!mPreviewSize.isValid()) return
        val fbo = context.getTexFBO(mPreviewSize.width, mPreviewSize.height)

        if (mIsNeedCalculate) {
            mMatrix.reset()
            mMatrix.scale(mScaleX, mScaleY, 1F)
            mMatrix.scale(if (mXMirror) -1F else 1F, if (mYMirror) -1F else 1F, 1F)
            mMatrix.rotate(mAngle, 0F, 0F, 1F)
            mTexture2DProgram.setVertexMatrix(mMatrix.matrix)
            mIsNeedCalculate = false
        }
        fbo.use { mTexture2DProgram.draw() }

        imageInOut.out(fbo)

        imageInOut.texture?.let { mStreamSnapshot.maybeSnapshot(context, it) }
    }

    override fun onRelease() {
        Message.obtain().apply {
            this.what = CommonMessageType.SURFACE_DESTROY
            this.obj = mSurfaceTexture
            mContext?.sendMessage(id, this)
        }
        updateTexture(null)
        mTexture.release()
        mTexture2DProgram.release()
        mSurfaceTexture?.release()
        mSurfaceTexture = null
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
//            CommonMessageType.MULTIMEDIA_PREVIEW_SIZE -> {
//
//            }
        }
    }

    private fun handleFrameAvailable() {
        // TODO 切换线程
        val surfaceTexture = mSurfaceTexture ?: return
        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(mMatrix.matrix)
        //
    }
}