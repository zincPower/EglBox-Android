#version 300 es

// 总的变换矩阵
uniform mat4 uMVPMatrix;
// 物体变换矩阵，只包括物体的旋转、平移、缩放
uniform mat4 uMMatrix;
// 光源位置
uniform vec3 uLightPosition;
// 相机位置
uniform vec3 uCameraPosition;
// 顶点位置
in vec3 aPosition;
// 法向量
in vec3 aNormal;
// 光滑度
in float aShininess;

// 将未转换的顶点位置传递给片元着色器
out vec3 vPosition;
// 该顶点镜面光最终亮度
out vec4 vSpecularLight;

// 计算该顶点的镜面光最终强度
vec4 calSpecularLight(
    vec3 normal, // 法向量（相对于原点）
    vec3 lightLocation,
    vec4 ligthIntensity
) {
    // 将向量移动到顶点位置，表示该顶点的法向量
    vec3 realNormal = normalize(aPosition + normalize(normal));

    // 转换后的顶点
    vec3 finalPosition = (uMMatrix * vec4(aPosition, 1.0)).xyz;

    // 顶点到相机的向量
    vec3 eyeVector = normalize(uCameraPosition - finalPosition);
    // 顶点到光源的向量
    vec3 lightVector = normalize(lightLocation - finalPosition);
    // 半向量
    vec3 halfVector = normalize(eyeVector + lightVector);

    // 利用点积，计算 cos 的值
    float dotResult = dot(realNormal, halfVector);
    float powerResult = max(0.0, pow(dotResult, aShininess));
    return ligthIntensity * powerResult;
}

void main() {
    // 使用变换矩阵计算绘制顶点的最终位置
    gl_Position = uMVPMatrix * vec4(aPosition, 1);
    // 将未转换的顶点位置传给片元着色器
    vPosition = aPosition;

    vec4 scatteredLightIntensity = vec4(0.7, 0.7, 0.7, 1.0);
    vSpecularLight = calSpecularLight(aNormal, uLightPosition, scatteredLightIntensity);

    // 为了点绘制时，方便查看点绘制
    gl_PointSize = 10.0;
}