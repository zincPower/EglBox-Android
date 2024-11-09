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

out vec4 fragColor;

void main() {
    vec4 orgColor = vec4(1.0);
    fragColor = orgColor * vAmbientLight + orgColor * vDiffuseLight + orgColor * vSpecularLight;
}