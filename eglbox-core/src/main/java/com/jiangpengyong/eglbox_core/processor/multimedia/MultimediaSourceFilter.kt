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
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.MessageType
import com.jiangpengyong.eglbox_core.program.ScaleType
import com.jiangpengyong.eglbox_core.program.Texture2DProgram
import com.jiangpengyong.eglbox_core.program.VertexAlgorithmFactory
import com.jiangpengyong.eglbox_core.program.isValid
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

/**
 * @author jiang peng yong
 * @date 2024/11/1 13:10
 * @email 56002982@qq.com
 * @des 多媒体源
 */
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

    private var mFrameSizeInfo: FrameSizeInfo? = null

    override fun onInit() {
        mTexture.init()
        mTexture2DProgram.init()
        mTexture2DProgram.setTexture(mTexture)
        mStreamSnapshot.setFilterId(id)
        updateTexture(mTexture)

        mSurfaceTexture = SurfaceTexture(mTexture.id)
        mSurfaceTexture?.setOnFrameAvailableListener { handleFrameAvailable() }
        Message.obtain().apply {
            this.what = MessageType.SURFACE_CREATED
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
        val surfaceTexture = mSurfaceTexture
        mSurfaceTexture = null
        if (surfaceTexture != null) {
            Message.obtain().apply {
                this.what = MessageType.SURFACE_DESTROY
                this.obj = surfaceTexture
                mContext?.sendMessage(id, this)
            }
        }

        updateTexture(null)
        mTexture.release()
        mTexture2DProgram.release()
        surfaceTexture?.release()
    }

    override fun onUpdateData(updateData: Bundle) {}
    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {
        when (message.what) {
//            PreviewMessageType.SET_FRAME_SIZE -> handleFrameSize(message.obj as? FrameSizeInfo)
//            CommonMessageType.SURFACE_CREATED -> handlePreviewSize(message.arg1, message.arg2)
//            CommonMessageType.SURFACE_CHANGED -> handlePreviewSize(message.arg1, message.arg2)
//            CommonMessageType.SURFACE_DESTROY -> handlePreviewSize(0, 0)
//            PreviewMessageType.SET_SOURCE_ROTATION -> handleRotation(message.arg1)
//            PreviewMessageType.SET_SOURCE_MIRROR -> handleMirror(message.arg1, message.arg2)
        }
    }

    private fun handleFrameAvailable() {
        // TODO 切换线程
        val surfaceTexture = mSurfaceTexture ?: return
        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(mMatrix.matrix)
        //
    }

    private fun handleFrameSize(frameSizeInfo: FrameSizeInfo?) {
        if (frameSizeInfo == null || frameSizeInfo.isValid()) {
            Logger.e(TAG, "Frame size info is invalid. FrameSizeInfo=${frameSizeInfo}")
            return
        }
        Logger.i(TAG, "handleFrameSize sourceSize=${frameSizeInfo.sourceSize}, targetSize=${frameSizeInfo.targetSize}")
        mTexture.updateSizeForOES(frameSizeInfo.sourceSize.width, frameSizeInfo.sourceSize.height)
        mFrameSizeInfo = frameSizeInfo
        val (scaleX, scaleY) = VertexAlgorithmFactory.calculate(ScaleType.CENTER_INSIDE, frameSizeInfo.sourceSize, frameSizeInfo.targetSize)
    }

    companion object {
        const val TAG = "MultimediaSourceFilter"
    }
}