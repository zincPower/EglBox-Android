package com.jiangpengyong.sample.filter

import android.opengl.GLES20
import com.jiangpengyong.eglbox.GLProgram
import com.jiangpengyong.eglbox.GLTexture
import com.jiangpengyong.eglbox.Target
import com.jiangpengyong.eglbox.allocateFloatBuffer
import com.jiangpengyong.eglbox.utils.IDENTITY_MATRIX_4x4

class Texture2DProgram(val target: Target) : GLProgram() {
    private var mVertexCoordinates = defaultVertexCoordinates
    private var mTextureCoordinates = defaultTextureCoordinates
    private var mVertexMatrix = IDENTITY_MATRIX_4x4
    private var mTextureMatrix = IDENTITY_MATRIX_4x4

    private var mVertexMatrixHandle = 0
    private var mTextureMatrixHandle = 0
    private var mVertexPosHandle = 0
    private var mTexturePosHandle = 0
    private var mTexture: GLTexture? = null

    fun setTexture(texture: GLTexture) {
        mTexture = texture
    }

    fun setVertexMatrix(matrix: FloatArray) {
        mVertexMatrix = matrix
    }

    fun setTextureMatrix(matrix: FloatArray) {
        mTextureMatrix = matrix
    }

    fun reset() {
        mVertexCoordinates = defaultVertexCoordinates
        mTextureCoordinates = defaultTextureCoordinates
        mVertexMatrix = IDENTITY_MATRIX_4x4
        mTextureMatrix = IDENTITY_MATRIX_4x4
        mTexture = null
    }

    override fun onInit() {
        mVertexMatrixHandle = getUniformLocation("uVertexMatrix")
        mTextureMatrixHandle = getUniformLocation("uTextureMatrix")
        mVertexPosHandle = getAttribLocation("aVertexPosition")
        mTexturePosHandle = getAttribLocation("aTexturePosition")
    }

    override fun onDraw() {
        if (mTexture == null) return
        mTexture?.bind()
        GLES20.glUniformMatrix4fv(mVertexMatrixHandle, 1, false, mVertexMatrix, 0)
        GLES20.glUniformMatrix4fv(mTextureMatrixHandle, 1, false, mTextureMatrix, 0)
        GLES20.glEnableVertexAttribArray(mVertexPosHandle)
        GLES20.glVertexAttribPointer(
            mVertexPosHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            2 * 4,
            mVertexCoordinates
        )
        GLES20.glEnableVertexAttribArray(mTexturePosHandle)
        GLES20.glVertexAttribPointer(
            mTexturePosHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            2 * 4,
            mTextureCoordinates
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mVertexPosHandle)
        GLES20.glDisableVertexAttribArray(mTexturePosHandle)
        mTexture?.unbind()
    }

    override fun onRelease() {
        mVertexMatrixHandle = 0
        mTextureMatrixHandle = 0
        mVertexPosHandle = 0
        mTexturePosHandle = 0
        mTexture = null
    }

    override fun getVertexShaderSource(): String = """
    uniform mat4 uVertexMatrix;
    uniform mat4 uTextureMatrix;
    attribute vec4 aVertexPosition;
    attribute vec4 aTexturePosition;
    varying vec2 vTexturePosition;
    void main() {
        gl_Position = uVertexMatrix * aVertexPosition;
        vTexturePosition = (uTextureMatrix * aTexturePosition).xy;
    }
    """

    override fun getFragmentShaderSource(): String = if (target == Target.TEXTURE_2D) {
        """
        precision mediump float;
        varying vec2 vTexturePosition;
        uniform sampler2D sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexturePosition);
        }
        """
    } else {
        """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexturePosition;
        uniform samplerExternalOES sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexturePosition);
        }
        """
    }

    companion object {
        val defaultVertexCoordinates = allocateFloatBuffer(
            floatArrayOf(
                -1.0f, -1.0f,   // 左下
                -1.0f, 1.0f,    // 左上
                1.0f, -1.0f,    // 右下
                1.0f, 1.0f      // 右上
            )
        )

        val defaultTextureCoordinates = allocateFloatBuffer(
            floatArrayOf(
                0.0f, 0.0f,     // 左下
                0.0f, 1.0f,     // 左上
                1.0f, 0.0f,     // 右下
                1.0f, 1.0f      // 右上
            )
        )
    }
}