package com.jiangpengyong.sample.d_light

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.gles.GLProgram
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.utils.GLMatrix

class LightPointProgram : GLProgram() {
    private var mMVPMatrixHandle = 0
    private var mPositionHandle = 0

    private var mMatrix: GLMatrix = GLMatrix()
    private var mLightPoint = Point(0F, 0F, 0F)

    fun setMatrix(matrix: GLMatrix) {
        mMatrix = matrix
    }

    fun setLightPoint(lightPoint: Point) {
        mLightPoint = lightPoint
    }

    override fun onInit() {
        mMVPMatrixHandle = getUniformLocation("uMVPMatrix")
        mPositionHandle = getAttribLocation("aPosition")
    }

    override fun onDraw() {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix.matrix, 0)
        GLES20.glVertexAttrib3f(mPositionHandle, mLightPoint.x, mLightPoint.y, mLightPoint.z)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }

    override fun onRelease() {
        mMVPMatrixHandle = 0
        mPositionHandle = 0
    }

    override fun getVertexShaderSource(): String = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        in vec3 aPosition;
        void main() {
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            gl_PointSize = 35.0;
        }
    """.trimIndent()

    override fun getFragmentShaderSource(): String = """
        #version 300 es
        precision mediump float;
        out vec4 fragColor;
        void main() {
            fragColor = vec4(1.0, 0.0, 0.0, 0.0);
        }
    """.trimIndent()
}