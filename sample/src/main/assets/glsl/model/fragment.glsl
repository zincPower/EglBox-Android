#version 300 es
precision mediump float;

// 顶点位置
in vec3 vPosition;
// 环境光强度
in vec4 vAmbientLight;
// 散射光强度
in vec4 vDiffuseLight;
// 镜面光强度
in vec4 vSpecularLight;
// 纹理坐标
in vec2 vTextureCoord;

uniform int uIsUseTexture;
uniform sampler2D sTexture;

out vec4 fragColor;

void main() {
    vec4 orgColor;
    if (uIsUseTexture == 1) {
        orgColor = texture(sTexture, vTextureCoord);
    } else {
        orgColor = vec4(1.0);
    }
    fragColor = orgColor * vAmbientLight + orgColor * vDiffuseLight + orgColor * vSpecularLight;
}