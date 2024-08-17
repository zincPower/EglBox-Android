package com.jiangpengyong.sample.e_texture.planet

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.GLMatrix
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.App
import java.nio.FloatBuffer
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author jiang peng yong
 * @date 2024/8/12 22:04
 * @email 56002982@qq.com
 * @des 天体
 */
class RingProgram(
    private val bigRadius: Float = 1F,      // 圆环的半径
    private val smallRadius: Float = 1.5F,  // 圆环条的半径
    private val bigSpan: Int = 36,          // 圆环的份数
    private val smallSpan: Int = 36,        // 圆环条的份数
) : GLProgram() {


    private var mMVPMatrixHandle = 0
    private var mMMatrixHandle = 0
    private var mLightPositionHandle = 0
    private var mCameraPositionHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mShininessHandle = 0
    private var mLightSourceTypeHandle = 0
    private var mTextureHandle = 0
    private var mTextureCoordHandle = 0

    private var mVertexCount = 0
    private var mMVPMatrix: GLMatrix = GLMatrix()
    private var mMMatrix: GLMatrix = GLMatrix()

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mNormalBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    private var mLightPosition = FloatArray(3)
    private var mCameraPosition = FloatArray(3)
    private var mShininess = 50F

    private var mTexture: GLTexture? = null

    init {
        calculateVertex()
    }

    fun setMVPMatrix(matrix: GLMatrix) {
        mMVPMatrix = matrix
    }

    fun setMMatrix(matrix: GLMatrix) {
        mMMatrix = matrix
    }

    fun setLightPosition(lightPosition: FloatArray) {
        mLightPosition = lightPosition
    }

    fun setCameraPosition(cameraPosition: FloatArray) {
        mCameraPosition = cameraPosition
    }

    fun setShininess(shininess: Float) {
        mShininess = shininess
    }

//    fun setAngleSpan(angleSpan: Int) {
//        mAngleSpan = angleSpan
//        calculateVertex()
//    }

    fun setTexture(texture: GLTexture) {
        mTexture = texture
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mMMatrixHandle = getUniformLocation("uMMatrix")
        mLightPositionHandle = getUniformLocation("uLightPosition")
        mCameraPositionHandle = getUniformLocation("uCameraPosition")
        mPositionHandle = getAttribLocation("aPosition")
        mTextureCoordHandle = getAttribLocation("aTextureCoord")
        mNormalHandle = getAttribLocation("aNormal")
        mShininessHandle = getAttribLocation("aShininess")
        mLightSourceTypeHandle = getUniformLocation("uLightSourceType")
        mTextureHandle = getUniformLocation("sTexture")
    }

    override fun onDraw() {
        mTexture?.bind()
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix.matrix, 0)
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mMMatrix.matrix, 0)
        GLES20.glUniform3f(mLightPositionHandle, mLightPosition[0], mLightPosition[1], mLightPosition[2])
        GLES20.glUniform3f(mCameraPositionHandle, mCameraPosition[0], mCameraPosition[1], mCameraPosition[2])
        GLES20.glVertexAttrib1f(mShininessHandle, mShininess)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mNormalBuffer)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glEnableVertexAttribArray(mNormalHandle)
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mNormalHandle)
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle)
        mTexture?.unbind()
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mMMatrixHandle = 0
        mLightPositionHandle = 0
        mCameraPositionHandle = 0
        mPositionHandle = 0
        mNormalHandle = 0
        mShininessHandle = 0
        mLightSourceTypeHandle = 0
        mTextureHandle = 0
        mTextureCoordHandle = 0
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/heavenly_body/vertex.glsl")

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(App.context.resources, "glsl/texture/heavenly_body/fragment.glsl")

    private fun calculateVertex() {
        mVertexCount = 3 * bigSpan * smallSpan * 2

        // 小圆周每份的角度跨度
        val smallSpan = 360F / smallSpan

        // 大圆周每份的角度跨度
        val bigSpan = 360F / bigSpan

        // 用于旋转的小圆半径
        val r = (bigRadius - smallRadius) / 2F

        // 旋转轨迹形成的大圆周半径
        val R = smallRadius + r

        // 原始顶点列表（未卷绕）
        val alVertex = ArrayList<Float>()
        // 用于组织三角形面的顶点编号列表
        val alFaceIndex = ArrayList<Int>()

        var curSmallAngle = 0.0
        while (ceil(curSmallAngle) < 360 + smallSpan) {
            // 当前小圆弧度
            val a = Math.toRadians(curSmallAngle)
            var curBigAngle = 0.0

            while (ceil(curBigAngle) < 360 + bigSpan) {
                // 当前大圆弧度
                val u = Math.toRadians(curBigAngle)

                // 当前顶点 x,y,z
                val x = ((R + r * sin(a)) * sin(u)).toFloat()
                val y = (r * cos(a)).toFloat()
                val z = ((R + r * sin(a)) * cos(u)).toFloat()

                // 将计算出来的X、Y、Z坐标放入原始顶点列表
                alVertex.add(x)
                alVertex.add(y)
                alVertex.add(z)

                curBigAngle += bigSpan
            }
            curSmallAngle += smallSpan
        }

        // 按照卷绕成三角形的需要
        for (i in 0 until this.smallSpan) {
            // 生成顶点编号列表
            for (j in 0 until this.bigSpan) {
                // 当前四边形第一顶点编号
                val index: Int = i * (this.bigSpan + 1) + j

                // 第一个三角形三个顶点的编号入列表
                alFaceIndex.add(index + 1)
                alFaceIndex.add(index + this.bigSpan + 1)
                alFaceIndex.add(index + this.bigSpan + 2)

                // 第二个三角形三个顶点的编号入列表
                alFaceIndex.add(index + 1)
                alFaceIndex.add(index)
                alFaceIndex.add(index + this.bigSpan + 1)
            }
        }
        // 存放按照卷绕顺序顶点坐标值的数组
        val vertices = FloatArray(mVertexCount * 3)

        // 生成卷绕后的顶点坐标数组值
        cullVertex(alVertex, alFaceIndex, vertices)

        // 原纹理坐标列表（未卷绕）
        val alST = ArrayList<Float>()

        var angdegCol = 0f
        while (ceil(angdegCol.toDouble()) < 360 + smallSpan) {
            //对小圆按照等角度间距循环
            val t = angdegCol / 360 //当前角度对应的t坐标
            var angdegRow = 0f
            while (Math.ceil(angdegRow.toDouble()) < 360 + bigSpan) {
                //对大圆按照等角度间距循环
                val s = angdegRow / 360 //当前角度对应的s坐标
                alST.add(s)
                alST.add(t) //存入原始纹理坐标列表
                angdegRow += bigSpan
            }
            angdegCol += smallSpan
        }

        val textures: FloatArray = cullTexCoor(alST, alFaceIndex) //生成卷绕后纹理坐标数组值

        mVertexBuffer = allocateFloatBuffer(vertices)
        mTextureBuffer = allocateFloatBuffer(textures)
        mNormalBuffer = allocateFloatBuffer(vertices)
    }

    private fun Double.toRadians(): Double {
        return Math.toRadians(this)
    }

    private fun cullVertex(
        alv: ArrayList<Float>,
        alFaceIndex: ArrayList<Int>,
        vertices: FloatArray
    ) {
        var vCount = 0
        // 对顶点编号列表进行循环
        for (i in alFaceIndex) {
            vertices[vCount++] = alv[3 * i]
            vertices[vCount++] = alv[3 * i + 1]
            vertices[vCount++] = alv[3 * i + 2]
        }
    }

    /**
     * 根据顶点编号生成卷绕后顶点纹理坐标数组的方法
     *
     * @param alST 原始纹理坐标列表
     * @param  alTexIndex 用于组织三角形面的顶点编号列表
     */
    private fun cullTexCoor(
        alST: ArrayList<Float>,
        alTexIndex: ArrayList<Int>
    ): FloatArray {
        // 结果纹理坐标数组
        val textures = FloatArray(alTexIndex.size * 2)

        var stCount = 0
        for (i in alTexIndex) {
            textures[stCount++] = alST[2 * i]
            textures[stCount++] = alST[2 * i + 1]
        }

        return textures
    }
}
