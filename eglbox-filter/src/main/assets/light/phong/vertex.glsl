#version 300 es

// 总的变换矩阵
uniform mat4 uMVPMatrix;
// 顶点位置
in vec3 aPosition;
// 纹理位置
in vec2 aTextureCoord;
// 法向量
in vec3 aNormal;

// 法向量插值
out vec3 vNormal;
// 纹理坐标
out vec2 vTextureCoord;
out vec3 vPosition;

void main() {
    // 使用变换矩阵计算绘制顶点的最终位置
    gl_Position = uMVPMatrix * vec4(aPosition, 1);
    vPosition = aPosition;
    vTextureCoord = aTextureCoord;
    vNormal = aNormal;

    // 为了点绘制时，方便查看点绘制
    gl_PointSize = 10.0;
}