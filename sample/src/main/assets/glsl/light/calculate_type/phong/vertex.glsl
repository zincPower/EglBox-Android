#version 300 es

// 总的变换矩阵
uniform mat4 uMVPMatrix;
// 顶点位置
in vec3 aPosition;
// 法向量
in vec3 aNormal;

// 将未转换的顶点位置传递给片元着色器
out vec3 vPosition;
out vec3 vNormal;

void main() {
    // 使用变换矩阵计算绘制顶点的最终位置
    gl_Position = uMVPMatrix * vec4(aPosition, 1);
    // 将未转换的顶点位置传给片元着色器
    vPosition = aPosition;
    vNormal = aNormal;

    // 为了点绘制时，方便查看点绘制
    gl_PointSize = 10.0;
}