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
// 纹理位置
in vec2 aTextureCoord;
// 法向量
in vec3 aNormal;
// 光滑度
in float aShininess;

out vec2 vTextureCoord;
// 环境光强度
out vec4 vAmbientLight;
// 该顶点散射光最终亮度
out vec4 vDiffuseLight;
// 该顶点镜面光最终亮度
out vec4 vSpecularLight;

// 计算该顶点的散射光最终强度
vec4 calScatteredLight(
    vec3 normal,
    vec3 lightLocation,
    vec4 ligthIntensity
) {
    // 顶点进行模型转换
    vec3 finalPosition = (uMMatrix * vec4(aPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = aPosition + normal;
    vec3 realNormal = (uMMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 计算顶点到光源的向量
    vec3 lightVector = normalize(lightLocation - (uMMatrix * vec4(aPosition, 1.0)).xyz);
    // 利用点积，计算 cos 的值，并限制在 [0, 1] 之间
    float dotResult = max(0.0, dot(realNormal, lightVector));
    return ligthIntensity * dotResult;
}

// 计算该顶点的镜面光最终强度
vec4 calSpecularLight(
    vec3 normal, // 法向量（相对于原点）
    vec3 lightLocation,
    vec4 ligthIntensity
) {
    // 顶点进行最终模型转换
    vec3 finalPosition = (uMMatrix * vec4(aPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    vec3 normalTarget = aPosition + normal;
    vec3 realNormal = (uMMatrix * vec4(normalTarget, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 顶点到相机的向量
    vec3 eyeVector = normalize(uCameraPosition - finalPosition);
    // 顶点到光源的向量
    vec3 lightVector = normalize(normalize(lightLocation - finalPosition));
    // 半向量
    vec3 halfVector = normalize(eyeVector + lightVector);

    // 利用点积，计算 cos 的值
    float dotResult = dot(realNormal, halfVector);
    if (dotResult <= 0.0) {
        return vec4(0);
    }
    float powerResult = max(0.0, pow(dotResult, aShininess));
    return ligthIntensity * powerResult;
}

void main() {
    // 使用变换矩阵计算绘制顶点的最终位置
    gl_Position = uMVPMatrix * vec4(aPosition, 1);
    vTextureCoord = aTextureCoord;

    // 环境光
    vAmbientLight = vec4(0.3, 0.3, 0.3, 1.0);

    // 散射光
    vec4 scatteredLightIntensity = vec4(0.7, 0.7, 0.7, 1.0);
    vDiffuseLight = calScatteredLight(aNormal, uLightPosition, scatteredLightIntensity);

    // 镜面光
    vec4 specularLightIntensity = vec4(0.6, 0.6, 0.6, 1.0);
    vSpecularLight = calSpecularLight(aNormal, uLightPosition, specularLightIntensity);
}