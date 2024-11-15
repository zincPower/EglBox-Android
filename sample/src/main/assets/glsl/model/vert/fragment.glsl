#version 300 es
precision mediump float;

// 顶点位置
in vec3 vPosition;
// 纹理坐标
in vec2 vTextureCoord;

// 正面环境光强度
in vec4 vFrontAmbientLight;
// 正面散射光强度
in vec4 vFrontDiffuseLight;
// 正面镜面光强度
in vec4 vFrontSpecularLight;

// 反面环境光亮度
in vec4 vBackAmbientLight;
// 反面散射光亮度
in vec4 vBackDiffuseLight;
// 反面镜面光亮度
in vec4 vBackSpecularLight;

uniform int uIsUseTexture;
uniform sampler2D sTexture;

out vec4 fragColor;

void main() {
    vec4 orgColor = (uIsUseTexture == 1) ? texture(sTexture, vTextureCoord) : vec4(1.0);
    if (gl_FrontFacing) {
        fragColor = orgColor * vFrontAmbientLight + orgColor * vFrontDiffuseLight + orgColor * vFrontSpecularLight;
    } else {
        fragColor = orgColor * vBackAmbientLight + orgColor * vBackDiffuseLight + orgColor * vBackSpecularLight;
    }
}