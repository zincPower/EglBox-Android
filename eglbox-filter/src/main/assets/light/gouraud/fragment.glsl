#version 300 es
precision mediump float;

// 纹理
uniform sampler2D uTexture;
// 颜色
uniform vec4 uColor;
// 是否使用纹理
uniform int uIsUseTexture;

in vec2 vTextureCoord;
in vec4 vAmbientLight;
in vec4 vDiffuseLight;
in vec4 vSpecularLight;

out vec4 fragColor;

void main() {
    vec4 orgColor;
    if (uIsUseTexture == 1) {
        orgColor = texture(uTexture, vTextureCoord);
    } else {
        orgColor = uColor;
    }
    fragColor = orgColor * vAmbientLight + orgColor * vDiffuseLight + orgColor * vSpecularLight;
}