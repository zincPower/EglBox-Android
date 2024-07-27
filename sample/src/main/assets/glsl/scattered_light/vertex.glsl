#version 300 es

// 总的变换矩阵
uniform mat4 uMVPMatrix;
// 物体变换矩阵，只包括物体的旋转、平移、缩放
uniform mat4 uMMatrix;
// 光源位置
uniform vec3 uLightPosition;
// 顶点位置
in vec3 aPosition;
// 法向量
in vec3 aNormal;
// 将未转换的顶点位置传递给片元着色器
out vec3 vPosition;
// 该顶点散射光最终亮度
out vec4 vScatteredLight;

// 计算该顶点的散射光最终强度
vec4 calScatteredLight(
    vec3 normal, // 法向量（相对于原点）
    vec3 lightLocation,
    vec4 ligthIntensity
) {
    // 将向量移动到顶点位置，表示该顶点的法向量
    vec3 positionNormal = aPosition + normalize(normal);

    vec3 realNormal = (uMMatrix * vec4(positionNormal, 1.0)).xyz - (uMMatrix * vec4(aPosition, 1.0)).xyz;
    // 归一化向量
    vec3 newNormal = normalize(positionNormal);

    vec3 lightVector = normalize(lightLocation - (uMMatrix * vec4(aPosition, 1.0)).xyz);
    // 利用点积，计算 cos 的值，并限制在 [0, 1] 之间
    float dotResult = max(0.0, dot(newNormal, lightVector));
    return ligthIntensity * dotResult;
}

void main() {
    // 使用变换矩阵计算绘制顶点的最终位置
    gl_Position = uMVPMatrix * vec4(aPosition, 1);
    // 将未转换的顶点位置传给片元着色器
    vPosition = aPosition;

    vec4 scatteredLightIntensity = vec4(0.8, 0.8, 0.8, 1.0);
    vScatteredLight = calScatteredLight(aNormal, uLightPosition, scatteredLightIntensity);

    // 为了点绘制时，方便查看点绘制
    gl_PointSize = 10.0;
}