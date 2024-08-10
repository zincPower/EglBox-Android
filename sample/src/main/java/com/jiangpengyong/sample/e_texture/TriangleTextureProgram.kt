package com.jiangpengyong.sample.e_texture

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.gles.GLTexture
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.eglbox_core.utils.ModelMatrix

/**
 * @author jiang peng yong
 * @date 2024/6/15 13:05
 * @email 56002982@qq.com
 * @des 绘制三角形纹理
 */
class TriangleTextureProgram : GLProgram() {
    private val mVertexBuffer = allocateFloatBuffer(
        floatArrayOf(
            -0.5F, 0.5F, 0F,
            0.5F, 0.5F, 0F,
            0F, -0.5F, 0F
        )
    )
    private val mTextureBuffer = allocateFloatBuffer(
        floatArrayOf(
            0F, 1F,
            1F, 1F,
            0.5F, 0F,
        )
    )

    private var mMVPMatrixHandle = 0
    private var mVertexPositionHandle = 0
    private var mTexturePositionHandle = 0
    private var mTextureHandle = 0

    private val mVertexCount = 3

    private val mMatrix = ModelMatrix()

    private var mTexture: GLTexture? = null

    fun setTexture(texture: GLTexture) {
        this.mTexture = texture
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mTextureHandle = getUniformLocation("uTexture")
        mVertexPositionHandle = getAttribLocation("aVertexPosition")
        mTexturePositionHandle = getAttribLocation("aTexturePosition")
    }

    override fun onDraw() {
        this.mTexture?.bind()
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttribPointer(mVertexPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES20.glVertexAttribPointer(mTexturePositionHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer)
        GLES20.glEnableVertexAttribArray(mVertexPositionHandle)
        GLES20.glEnableVertexAttribArray(mTexturePositionHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mVertexPositionHandle)
        GLES20.glDisableVertexAttribArray(mTexturePositionHandle)
        this.mTexture?.unbind()
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mTextureHandle = 0
        mVertexPositionHandle = 0
        mTexturePositionHandle = 0
    }

    override fun getVertexShaderSource(): String = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        in vec3 aVertexPosition;
        in vec2 aTexturePosition;
        out vec2 vTexturePosition;
        void main() {
            gl_Position = uMVPMatrix * vec4(aVertexPosition, 1.0);
            vTexturePosition = aTexturePosition;
        }
    """.trimIndent()

    override fun getFragmentShaderSource(): String = """
        #version 300 es
        precision mediump float;
        uniform sampler2D uTexture;
        in vec2 vTexturePosition;
        out vec4 fragColor;
        void main() {
            fragColor = texture(uTexture, vTexturePosition);
        }
    """.trimIndent()
}