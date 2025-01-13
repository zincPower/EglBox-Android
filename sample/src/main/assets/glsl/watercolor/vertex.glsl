#version 300 es

// 总的变换矩阵
uniform mat4 uMVPMatrix;
// 物体变换矩阵，只包括物体的旋转、平移、缩放
uniform mat4 uModelMatrix;
// 光源位置
uniform vec3 uLightPoint;
// 视点位置
uniform vec3 uViewPoint;
// 顶点位置
in vec3 aPosition;
// 法向量
in vec3 aNormal;

// 光滑度
uniform float uShininess;

// 控制三种光是否现实
uniform int uIsAddAmbientLight;
uniform int uIsAddDiffuseLight;
uniform int uIsAddSpecularLight;

// 三种光照系数
uniform vec4 ambientLightCoefficient;
uniform vec4 diffuseLightCoefficient;
uniform vec4 specularLightCoefficient;

// 光源类型 1：定点光 2：定向光
uniform int uLightSourceType;

// 纹理坐标
out vec2 vTextureCoord;

// 计算顶点的散射光强度
vec4 calDiffuseLight(
    vec3 normal,
    vec3 lightLocation,
    vec4 ligthCoefficient
) {
    // 顶点进行模型转换
    vec3 finalPosition = (uModelMatrix * vec4(aPosition, 1)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = aPosition + normal;
    vec3 realNormal = (uModelMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 计算顶点到光源的向量
    vec3 lightVector;
    if (uLightSourceType == 1) {
        lightVector = normalize(lightLocation - (uModelMatrix * vec4(aPosition, 1)).xyz);
    } else {
        lightVector = normalize(lightLocation);
    }
    // 利用点积，计算 cos 的值，并限制在 [0, 1] 之间
    float dotResult = max(0.0, dot(realNormal, lightVector));
    return ligthCoefficient * dotResult;
}

// 计算顶点的镜面光强度
vec4 calSpecularLight(
    vec3 normal,
    vec3 lightLocation,
    vec4 ligthCoefficient
) {
    // 顶点进行模型转换
    vec3 finalPosition = (uModelMatrix * vec4(aPosition, 1)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = aPosition + normal;
    vec3 realNormal = (uModelMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 视点到相机的向量
    vec3 eyeVector = normalize(uViewPoint - finalPosition);
    // 顶点到光源的向量
    vec3 lightVector;
    if (uLightSourceType == 1) {
        lightVector = normalize(normalize(lightLocation - finalPosition));
    } else {
        lightVector = normalize(lightLocation);
    }
    // 半向量
    vec3 halfVector = normalize(eyeVector + lightVector);

    // 利用点积，计算 cos 的值
    float dotResult = dot(realNormal, halfVector);
    float powerResult = max(0.0, pow(dotResult, uShininess));
    return ligthCoefficient * powerResult;
}

void main() {
    gl_Position = uMVPMatrix * vec4(aPosition, 1);

    // 环境光强度
    vec4 ambientLight;
    // 散射光强度
    vec4 diffuseLight;
    // 镜面光亮度
    vec4 specularLight;
    // 环境光
    if (uIsAddAmbientLight == 1) {
        ambientLight = ambientLightCoefficient;
    } else {
        ambientLight = vec4(0);
    }

    // 散射光
    if (uIsAddDiffuseLight == 1) {
        diffuseLight = calDiffuseLight(normalize(aNormal), uLightPoint, diffuseLightCoefficient);
    } else {
        diffuseLight = vec4(0);
    }

    // 镜面光
    if (uIsAddSpecularLight == 1) {
        specularLight = calSpecularLight(normalize(aNormal), uLightPoint, specularLightCoefficient);
    } else {
        specularLight = vec4(0);
    }

    float s = diffuseLight.x + specularLight.x;
    vTextureCoord = vec2(s, 0.5);
}