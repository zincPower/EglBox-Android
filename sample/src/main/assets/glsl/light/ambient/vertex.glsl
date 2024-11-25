#version 300 es

// 变换矩阵
uniform mat4 uMVPMatrix;
// 顶点位置
in vec3 aPosition;
// 将未转换的顶点位置传递给片元着色器
out vec3 vPosition;
// 环境光强度
out vec4 vAmbientLight;

void main() {
    // 使用变换矩阵计算绘制顶点的最终位置
    gl_Position = uMVPMatrix * vec4(aPosition, 1);
    // 将未转换的顶点位置传给片元着色器
    vPosition = aPosition;
    // 环境光强度
    vAmbientLight = vec4(0.15, 0.15, 0.15, 1.0);
    // 为了点绘制时，方便查看点绘制
    gl_PointSize = 10.0;
}