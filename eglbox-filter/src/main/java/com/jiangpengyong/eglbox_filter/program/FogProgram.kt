package com.jiangpengyong.eglbox_filter.program

import android.opengl.GLES20
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.utils.GLShaderExt.loadFromAssetsFile
import com.jiangpengyong.eglbox_filter.EglBoxRuntime
import com.jiangpengyong.eglbox_filter.model.ModelData

/**
 * @author jiang peng yong
 * @date 2024/12/6 21:17
 * @email 56002982@qq.com
 * @des 雾 Program
 */
open class FogProgram(
    lightCalculateType: LightCalculateType = LightCalculateType.Vertex,
) : LightProgram(lightCalculateType) {
    private var mStartFogHandle: Int = 0
    private var mEndFogHandle: Int = 0
    private var mFogColorHandle: Int = 0

    private var mStartFog: Float = 10F
    private var mEndFog: Float = 20F
    private var mFogColor: Color = Color(0F, 0F, 0F, 1F)

    fun setFogRange(startFot: Float, endFot: Float) {
        if (startFot >= endFot) {
            Logger.e(TAG, "Fog range is invalid. End is less than or equal to start.")
            return
        }
        mStartFog = startFot
        mEndFog = endFot
    }

    fun setFogColor(fogColor: Color) {
        mFogColor = fogColor
    }

    override fun onInit() {
        super.onInit()
        mStartFogHandle = getUniformLocation("uStartFog")
        mEndFogHandle = getUniformLocation("uEndFog")
        mFogColorHandle = getUniformLocation("uFogColor")
    }

    override fun onRelease() {
        super.onRelease()
        mStartFogHandle = 0
        mEndFogHandle = 0
        mFogColorHandle = 0
    }

    override fun realDraw(modelData: ModelData) {
        GLES20.glUniform1f(mStartFogHandle, mStartFog)
        GLES20.glUniform1f(mEndFogHandle, mEndFog)
        GLES20.glUniform4f(mFogColorHandle, mFogColor.red, mFogColor.green, mFogColor.blue, mFogColor.alpha)
        super.realDraw(modelData)
    }

    override fun getVertexShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        when (lightCalculateType) {
            LightCalculateType.Vertex -> "fog/gouraud/vertex.glsl"
            LightCalculateType.Fragment -> "fog/phong/vertex.glsl"
        }
    )

    override fun getFragmentShaderSource(): String = loadFromAssetsFile(
        EglBoxRuntime.context.resources,
        when (lightCalculateType) {
            LightCalculateType.Vertex -> "fog/gouraud/fragment.glsl"
            LightCalculateType.Fragment -> "fog/phong/fragment.glsl"
        }
    )

    companion object {
        const val TAG = "FogProgram"
    }
}