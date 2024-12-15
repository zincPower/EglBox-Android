package com.jiangpengyong.eglbox_filter.program

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

    override fun onInit() {

    }

    override fun onRelease() {
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

    override fun realDraw(modelData: ModelData) {

    }

    companion object {
        const val TAG = "FogProgram"
    }
}