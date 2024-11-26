#version 300 es
precision mediump float;

// 物体变换矩阵，只包括物体的旋转、平移、缩放
uniform mat4 uMMatrix;
// 光源位置
uniform vec3 uLightPoint;
// 相机位置
uniform vec3 uViewPoint;
// 光滑度
//uniform float aShininess;

// 顶点位置
in vec3 vPosition;
// 纹理坐标
in vec2 vTextureCoord;
// 法向量
in vec3 vNormal;

// 控制三种光是否使用
uniform int uIsAddAmbientLight;
uniform int uIsAddDiffuseLight;
uniform int uIsAddSpecularLight;
// 是否使用纹理
uniform int uIsUseTexture;
// 纹理
uniform sampler2D sTexture;
// 是否进行双面渲染
uniform int uIsDoubleSideRendering;

out vec4 fragColor;

// 计算该顶点的散射光最终强度
vec4 calDiffuseLight(
    vec3 normal, // 法向量
    vec3 lightLocation, // 光照位置
    vec4 ligthIntensity     // 光强
) {
    // 顶点进行模型转换
    vec3 finalPosition = (uMMatrix * vec4(vPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = vPosition + normal;
    vec3 realNormal = (uMMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 计算顶点到光源的向量
    vec3 lightVector = normalize(lightLocation - (uMMatrix * vec4(vPosition, 1)).xyz);
    // 利用点积，计算 cos 的值，并限制在 [0, 1] 之间
    float dotResult = max(0.0, dot(realNormal, lightVector));
    return ligthIntensity * dotResult;
}

// 计算该顶点的镜面光最终强度
vec4 calSpecularLight(
    vec3 normal,
    vec3 lightLocation,
    vec4 ligthIntensity
) {
    // 顶点进行模型转换
    vec3 finalPosition = (uMMatrix * vec4(vPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = vPosition + normal;
    vec3 realNormal = (uMMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(normalize(realNormal));

    // 顶点到相机的向量
    vec3 eyeVector = normalize(normalize(uViewPoint - finalPosition));
    // 顶点到光源的向量
    vec3 lightVector = normalize(normalize(lightLocation - finalPosition));
    // 半向量
    vec3 halfVector = normalize(eyeVector + lightVector);

    // 利用点积，计算 cos 的值
    float dotResult = dot(realNormal, halfVector);
    if (dotResult <= 0.0) {
        return vec4(0);
    }
    float powerResult = max(0.0, pow(dotResult, 50.0));
    return ligthIntensity * powerResult;
}

void calculateLighting(vec3 normal, out vec4 ambientLight, out vec4 diffuseLight, out vec4 specularLight) {
    // 环境光
    ambientLight = (uIsAddAmbientLight == 1) ? vec4(0.15, 0.15, 0.15, 1.0) : vec4(0);
    // 散射光
    diffuseLight = (uIsAddDiffuseLight == 1) ? calDiffuseLight(normal, uLightPoint, vec4(0.8, 0.8, 0.8, 1.0)) : vec4(0);
    // 镜面光
    specularLight = (uIsAddSpecularLight == 1) ? calSpecularLight(normal, uLightPoint, vec4(0.7, 0.7, 0.7, 1.0)) : vec4(0);
}

void main() {
    vec4 orgColor = (uIsUseTexture == 1) ? texture(sTexture, vTextureCoord) : vec4(1.0);

    vec4 frontAmbientLight;
    vec4 frontDiffuseLight;
    vec4 frontSpecularLight;

    vec4 backAmbientLight;
    vec4 backDiffuseLight;
    vec4 backSpecularLight;

    calculateLighting(vNormal, frontAmbientLight, frontDiffuseLight, frontSpecularLight);

    if (uIsDoubleSideRendering == 1) {
        calculateLighting(-vNormal, backAmbientLight, backDiffuseLight, backSpecularLight);
    } else {
        backAmbientLight = vec4(0);
        backDiffuseLight = vec4(0);
        backSpecularLight = vec4(0);
    }

    if (gl_FrontFacing) {
        fragColor = orgColor * frontAmbientLight + orgColor * frontDiffuseLight + orgColor * frontSpecularLight;
    } else {
        fragColor = orgColor * backAmbientLight + orgColor * backDiffuseLight + orgColor * backSpecularLight;
    }
}