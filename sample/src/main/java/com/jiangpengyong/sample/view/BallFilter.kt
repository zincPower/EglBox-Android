package com.jiangpengyong.sample.view

import android.opengl.GLES20
import android.os.Bundle
import android.os.Message
import android.util.Size
import com.jiangpengyong.eglbox_core.filter.FilterContext
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.ImageInOut
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt
import com.jiangpengyong.eglbox_core.utils.ModelMatrix
import com.jiangpengyong.eglbox_core.utils.ProjectionMatrix
import com.jiangpengyong.eglbox_core.utils.ViewMatrix
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.utils.toRadians
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class BallFilter : GLFilter() {
    private val mProgram = TrigonometricBallProgram()

    private val mProjectMatrix = ProjectionMatrix()
    private val mViewMatrix = ViewMatrix()
    private val mModelMatrix = ModelMatrix()

    private var xAngle = 0F
    private var yAngle = 0F

    private var mPreviewSize = Size(0, 0)

    override fun onInit(context: FilterContext) {
        mProgram.init()
        mViewMatrix.setLookAtM(
            0F, 0F, 3F,
            0F, 0F, 0F,
            0F, 1F, 0F
        )
    }

    override fun onDraw(context: FilterContext, imageInOut: ImageInOut) {
        imageInOut.texture?.let { texture ->
            updateProjectionMatrix(Size(texture.width, texture.height))
            val fbo = context.getTexFBO(texture.width, texture.height)
            fbo.use {
                GLES20.glClearColor(0F, 0F, 0F, 1F)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                context.texture2DProgram.reset()
                context.texture2DProgram.setTexture(texture)
                context.texture2DProgram.draw()
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                GLES20.glFrontFace(GLES20.GL_CW)
                mProgram.setMatrix(mProjectMatrix * mViewMatrix * mModelMatrix * context.space3D.gestureMatrix)
                mProgram.draw()
                GLES20.glDisable(GLES20.GL_DEPTH_TEST)
                GLES20.glDisable(GLES20.GL_CULL_FACE)
            }
            fbo.unbindTexture()?.let { imageInOut.out(it) }
        }
    }

    override fun onRelease(context: FilterContext) {
        mProgram.release()
    }

    private fun updateProjectionMatrix(size: Size) {
        if (mPreviewSize.width != size.width || mPreviewSize.height != size.height) {
            mProjectMatrix.reset()
            if (size.width > size.height) {
                val ratio = size.width.toFloat() / size.height.toFloat()
                mProjectMatrix.setFrustumM(
                    -ratio, ratio,
                    -1F, 1F,
                    2F, 10F
                )
            } else {
                val ratio = size.height.toFloat() / size.width.toFloat()
                mProjectMatrix.setFrustumM(
                    -1F, 1F,
                    -ratio, ratio,
                    2F, 10F
                )
            }
            mPreviewSize = size
        }
    }

    override fun onUpdateData(updateData: Bundle) {
        xAngle += updateData.getFloat("xAngle", 0F)
        yAngle += updateData.getFloat("yAngle", 0F)
        mModelMatrix.reset()
        mModelMatrix.rotate(xAngle, 0F, 1F, 0F)
        mModelMatrix.rotate(yAngle, 1F, 0F, 0F)

        val mode = updateData.getInt("drawingMode", 0)
        if (mode != 0) {
            when (mode) {
                TrigonometricBallProgram.DrawMode.Point.value -> mProgram.setDrawMode(TrigonometricBallProgram.DrawMode.Point)
                TrigonometricBallProgram.DrawMode.Line.value -> mProgram.setDrawMode(TrigonometricBallProgram.DrawMode.Line)
                TrigonometricBallProgram.DrawMode.Face.value -> mProgram.setDrawMode(TrigonometricBallProgram.DrawMode.Face)
            }
        }

        val spanAngle = updateData.getInt("spanAngle", 0)
        if (spanAngle != 0) {
            mProgram.setAngleSpan(spanAngle)
        }
    }

    override fun onRestoreData(inputData: Bundle) {}
    override fun onStoreData(outputData: Bundle) {}
    override fun onReceiveMessage(message: Message) {}
}

/**
 * @author jiang peng yong
 * @date 2024/6/19 10:08
 * @email 56002982@qq.com
 * @des 绘制球
 */
class TrigonometricBallProgram : GLProgram() {
    enum class DrawMode(val value: Int) {
        Point(1),
        Line(2),
        Face(3)
    }

    private var mAngleSpan = 10
    private lateinit var mVertexBuffer: FloatBuffer

    private var mRadius = 1F

    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0

    private var mVertexCount = 0
    private var mMatrix: GLMatrix = GLMatrix()
    private var mDrawMode: DrawMode = DrawMode.Face

    init {
        calculateVertex()
    }

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    fun setDrawMode(mode: DrawMode) {
        mDrawMode = mode
    }

    fun setAngleSpan(angleSpan: Int) {
        mAngleSpan = angleSpan
        calculateVertex()
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        when (mDrawMode) {
            DrawMode.Point -> GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mVertexCount)
            DrawMode.Line -> GLES20.glDrawArrays(GLES20.GL_LINES, 0, mVertexCount)
            DrawMode.Face -> GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        }
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
    }

    override fun getVertexShaderSource(): String = GLShaderExt.loadFromAssetsFile(App.context.resources, "glsl/ball/vertex.glsl")

    override fun getFragmentShaderSource(): String = GLShaderExt.loadFromAssetsFile(App.context.resources, "glsl/ball/fragment.glsl")

    private fun calculateVertex() {
        val vertexList = ArrayList<Float>()

        var verticalAngle = -90.0

        // 计算中间每一层的点
        while (verticalAngle < 90F) {   // 垂直角度从 -90 到 90
            // 这一层的半径
            val curLayerAngle = verticalAngle.toRadians()
            val layerRadius = mRadius * cos(curLayerAngle)

            // 下一层的半径
            val nextLayerAngle = (verticalAngle + mAngleSpan).toRadians()
            val nextLayerRadius = mRadius * cos(nextLayerAngle)

            val curLayerY = mRadius * sin(curLayerAngle)
            val nextLayerY = mRadius * sin(nextLayerAngle)

            var horizontalAngle = 0.0
            while (horizontalAngle < 360) {     // 水平角度从 0 到 360
                val curHorAngle = horizontalAngle.toRadians()
                val nextHorAngle = (horizontalAngle + mAngleSpan).toRadians()

                /**
                 *     P2(x0, y0, z0)   P3(x0, y0, z0)
                 *      ------------------
                 *      ｜              ╱｜
                 *      ｜            ╱  ｜
                 *      ｜          ╱    ｜
                 *      ｜        ╱      ｜
                 *      ｜      ╱        ｜
                 *      ｜    ╱          ｜
                 *      ｜  ╱            ｜
                 *      ｜╱              ｜
                 *      ------------------
                 *     P1(x0, y0, z0)   P0(x0, y0, z0)
                 */
                val x0 = layerRadius * cos(curHorAngle)
                val y0 = curLayerY
                val z0 = layerRadius * sin(curHorAngle)

                val x1 = layerRadius * cos(nextHorAngle)
                val y1 = curLayerY
                val z1 = layerRadius * sin(nextHorAngle)

                val x2 = nextLayerRadius * cos(nextHorAngle)
                val y2 = nextLayerY
                val z2 = nextLayerRadius * sin(nextHorAngle)

                val x3 = nextLayerRadius * cos(curHorAngle)
                val y3 = nextLayerY
                val z3 = nextLayerRadius * sin(curHorAngle)

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                vertexList.add(x0.toFloat())
                vertexList.add(y0.toFloat())
                vertexList.add(z0.toFloat())

                vertexList.add(x1.toFloat())
                vertexList.add(y1.toFloat())
                vertexList.add(z1.toFloat())

                vertexList.add(x2.toFloat())
                vertexList.add(y2.toFloat())
                vertexList.add(z2.toFloat())

                vertexList.add(x3.toFloat())
                vertexList.add(y3.toFloat())
                vertexList.add(z3.toFloat())

                horizontalAngle += mAngleSpan
            }
            verticalAngle += mAngleSpan
        }
        mVertexCount = vertexList.size / 3
        mVertexBuffer = allocateFloatBuffer(vertexList.toFloatArray())
    }
}