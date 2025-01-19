#version 300 es
uniform mat4 uMVPMatrix;       // 模型-视-投影矩阵
uniform mat4 uModelMatrix;     // 模型矩阵
in vec3 aPosition;             // 顶点位置
in vec3 aNormal;               // 顶点法向量

void main() {
    // 将法向量转换到世界空间
    vec3 worldNormal = normalize(mat3(uModelMatrix) * aNormal);

    // 定义偏移量大小（可根据需要调整）
    float outlineSize = 2.0;

    // 沿法向量方向偏移顶点位置
    vec4 offsetPosition = vec4(aPosition + worldNormal * outlineSize, 1.0);

    // 将偏移后的顶点位置转换到裁剪空间
    gl_Position = uMVPMatrix * offsetPosition;
}