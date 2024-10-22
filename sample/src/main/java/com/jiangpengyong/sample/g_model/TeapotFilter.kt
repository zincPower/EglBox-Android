//package com.jiangpengyong.sample.g_model
//
//import android.graphics.Bitmap
//import android.opengl.GLES20
//import android.os.Bundle
//import android.os.Message
//import android.util.Size
//import com.jiangpengyong.eglbox_core.filter.FilterContext
//import com.jiangpengyong.eglbox_core.filter.GLFilter
//import com.jiangpengyong.eglbox_core.filter.ImageInOut
//import com.jiangpengyong.eglbox_core.gles.GLTexture
//import com.jiangpengyong.eglbox_core.utils.ModelMatrix
//import com.jiangpengyong.eglbox_core.utils.ProjectMatrix
//import com.jiangpengyong.eglbox_core.utils.ViewMatrix
//import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
//import com.jiangpengyong.sample.g_model.film.DrawMode
//import com.jiangpengyong.sample.g_model.film.Model3DInfo
//import com.jiangpengyong.sample.g_model.film.Model3DProgram
//import com.jiangpengyong.sample.g_model.film.Obj3DModelUtils
//
///**
// * @author jiang peng yong
// * @date 2024/10/10 08:56
// * @email 56002982@qq.com
// * @des 茶壶滤镜
// */
//class TeapotFilter : GLFilter() {
//    private val mProgram = Model3DProgram()
//
//    private var mModel3DInfo: Model3DInfo? = null
//    private var mTexture: GLTexture? = null
//
//    private val mProjectMatrix = ProjectMatrix()
//    private val mViewMatrix = ViewMatrix()
//    private val mModelMatrix = ModelMatrix()
//
//    private var mXAngle = 0F
//    private var mYAngle = 0F
//
//    private var mPreviewSize = Size(0, 0)
//    private var mLightPosition = floatArrayOf(0F, 0F, 100F)
//    private var mCameraPosition = floatArrayOf(0F, 0F, 100F)
//
//    override fun onInit() {
//        mProgram.init()
//        updateViewMatrix()
//    }
//
//    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
//        updateProjectionMatrix(context)
//        drawModel()
//    }
//
//    override fun onRelease() {
//        mProgram.release()
//    }
//
//    override fun onUpdateData(updateData: Bundle) {
//        synchronized(this) {
//            mXAngle += updateData.getFloat("xAngle", 0F)
//            mYAngle += updateData.getFloat("yAngle", 0F)
//            mModelMatrix.reset()
//            mModelMatrix.rotate(mXAngle, 0F, 1F, 0F)
//            mModelMatrix.rotate(mYAngle, 1F, 0F, 0F)
//        }
//    }
//
//    override fun onRestoreData(inputData: Bundle) {}
//    override fun onStoreData(outputData: Bundle) {}
//    override fun onReceiveMessage(message: Message) {
//        synchronized(this) {
//            when (message.what) {
//                MessageWhat.RESET.value -> {
//                    mXAngle = 0F
//                    mYAngle = 0F
//                    mModelMatrix.reset()
//                }
//
//                MessageWhat.OBJ_DATA.value -> {
//                    message.obj?.toString()?.let {
//                        mModel3DInfo = Obj3DModelUtils.load(it)
//                    }
//                }
//
//                MessageWhat.OBJ_TEXTURE.value -> {
//                    (message.obj as? Bitmap)?.apply {
//                        mTexture?.release()
//                        mTexture = GLTexture()
//                        mTexture?.init()
//                        mTexture?.setData(this)
//                    }
//                }
//
//                else -> {}
//            }
//        }
//    }
//
//    private fun drawModel() {
//        val model3DInfo = mModel3DInfo ?: return
//        val texture = mTexture ?: return
//        mProgram.setTexture(texture)
//        mProgram.setCameraPosition(mCameraPosition)
//        mProgram.setLightPosition(mLightPosition)
//        mProgram.setData(
//            vertexBuffer = allocateFloatBuffer(model3DInfo.vertexData),
//            textureBuffer = model3DInfo.textureData?.let { allocateFloatBuffer(it) },
//            normalBuffer = model3DInfo.normalData?.let { allocateFloatBuffer(it) },
//            vertexCount = model3DInfo.count
//        )
//        GLES20.glFrontFace(model3DInfo.frontFace.value)
//        mProgram.setDrawMode(DrawMode.Triangles)
//        mProgram.setMVPMatrix(mProjectMatrix * mViewMatrix * mModelMatrix)
//        mProgram.setMMatrix(mModelMatrix)
//        mProgram.draw()
//    }
//
//    private fun updateProjectionMatrix(context: FilterContext) {
//        val previewSize = context.previewSize
//        if (mPreviewSize.width != previewSize.width || mPreviewSize.height != previewSize.height) {
//            if (previewSize.width > previewSize.height) {
//                val ratio = previewSize.width.toFloat() / previewSize.height.toFloat()
//                mProjectMatrix.setFrustumM(
//                    -ratio, ratio,
//                    -1F, 1F,
//                    10F, 200F
//                )
//            } else {
//                val ratio = previewSize.height.toFloat() / previewSize.width.toFloat()
//                mProjectMatrix.setFrustumM(
//                    -1F, 1F,
//                    -ratio, ratio,
//                    10F, 200F
//                )
//            }
//            mPreviewSize = previewSize
//        }
//    }
//
//    private fun updateViewMatrix() {
//        mViewMatrix.setLookAtM(
//            mCameraPosition[0], mCameraPosition[1], mCameraPosition[2],
//            0F, 0F, 0F,
//            0F, 1F, 0F
//        )
//    }
//
//    companion object {
//        const val TAG = "TeapotFilter"
//    }
//}
//
//enum class MessageWhat(val value: Int) {
//    RESET(10000),
//    OBJ_DATA(10001),
//    OBJ_TEXTURE(10002),
//}