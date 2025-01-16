#version 300 es

// 总的变换矩阵
uniform mat4 uMVPMatrix;
// 顶点位置
in vec3 aPosition;
// 法向量
in vec3 aNormal;

void main() {
    vec3 position = aPosition;
    // 所有的点延法线方向外扩
    position.xyz += aNormal * 1.0;
    gl_Position = uMVPMatrix * vec4(position.xyz, 1);
}