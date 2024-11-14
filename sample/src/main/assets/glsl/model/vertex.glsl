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
// 纹理坐标
in vec2 aTextureCoord;
// 光滑度
in float aShininess;

// 控制三种光是否使用
uniform int uIsAddAmbientLight;
uniform int uIsAddDiffuseLight;
uniform int uIsAddSpecularLight;
uniform int uIsDoubleSideRendering;

// 将未转换的顶点位置传递给片元着色器
out vec3 vPosition;
// 输出纹理坐标
out vec2 vTextureCoord;

// 正面环境光亮度
out vec4 vFrontAmbientLight;
// 正面散射光亮度
out vec4 vFrontDiffuseLight;
// 正面镜面光亮度
out vec4 vFrontSpecularLight;

// 反面环境光亮度
out vec4 vBackAmbientLight;
// 反面散射光亮度
out vec4 vBackDiffuseLight;
// 反面镜面光亮度
out vec4 vBackSpecularLight;

// 计算该顶点的散射光最终强度
vec4 calDiffuseLight(
    vec3 normal,            // 法向量
    vec3 lightLocation,     // 光照位置
    vec4 ligthIntensity     // 光强
) {
    // 顶点进行模型转换
    vec3 finalPosition = (uMMatrix * vec4(aPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = aPosition + normal;
    vec3 realNormal = (uMMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 计算顶点到光源的向量
    vec3 lightVector = normalize(lightLocation - (uMMatrix * vec4(aPosition, 1)).xyz);
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
    vec3 finalPosition = (uMMatrix * vec4(aPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = aPosition + normal;
    vec3 realNormal = (uMMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(normalize(realNormal));

    // 顶点到相机的向量
    vec3 eyeVector = normalize(normalize(uCameraPosition - finalPosition));
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
    // 将未转换的顶点位置传给片元着色器
    vPosition = aPosition;
    vTextureCoord = aTextureCoord;

    vec3 frontNormal = normalize(aNormal);

    // 环境光
//    if (uIsAddAmbientLight == 1) {
        vFrontAmbientLight = vec4(0.15, 0.15, 0.15, 1.0);
//    } else {
//        vFrontAmbientLight = vec4(0);
//    }

    // 散射光
//    if (uIsAddDiffuseLight == 1) {
        vec4 diffuseLightIntensity = vec4(0.8, 0.8, 0.8, 1.0);
        vFrontDiffuseLight = calDiffuseLight(frontNormal, uLightPosition, diffuseLightIntensity);
//    } else {
//        vFrontDiffuseLight = vec4(0);
//    }

    // 镜面光
    if (uIsAddSpecularLight == 1) {
        vec4 specularLightIntensity = vec4(0.7, 0.7, 0.7, 1.0);
        vFrontSpecularLight = calSpecularLight(frontNormal, uLightPosition, specularLightIntensity);
    } else {
        vFrontSpecularLight = vec4(0);
    }

    if (uIsDoubleSideRendering == 1) {
        vec3 backNormal = normalize(-aNormal);
        // 环境光
        if (uIsAddAmbientLight == 1) {
            vBackAmbientLight = vec4(0.15, 0.15, 0.15, 1.0);
        } else {
            vBackAmbientLight = vec4(0);
        }

        // 散射光
        if (uIsAddDiffuseLight == 1) {
            vec4 diffuseLightIntensity = vec4(0.8, 0.8, 0.8, 1.0);
            vBackDiffuseLight = calDiffuseLight(backNormal, uLightPosition, diffuseLightIntensity);
        } else {
            vBackDiffuseLight = vec4(0);
        }

        // 镜面光
        if (uIsAddSpecularLight == 1) {
            vec4 specularLightIntensity = vec4(0.7, 0.7, 0.7, 1.0);
            vBackSpecularLight = calSpecularLight(backNormal, uLightPosition, specularLightIntensity);
        } else {
            vBackSpecularLight = vec4(0);
        }
    }

    // 为了点绘制时，方便查看点绘制
    gl_PointSize = 10.0;
}