#version 300 es
precision mediump float;

// 物体变换矩阵，只包括物体的旋转、平移、缩放
uniform mat4 uModelMatrix;
// 光源位置
uniform vec3 uLightPoint;
// 相机位置
uniform vec3 uViewPoint;

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

// 纹理
uniform sampler2D uTexture;
// 颜色
uniform vec4 uColor;
// 纹理类型，1：纯色；2：格子颜色；3、纹理
uniform int uTextureType;

// 雾开始和结束
uniform float uStartFog;
uniform float uEndFog;

// 雾颜色
uniform vec4 uFogColor;

in vec3 vPosition;
in vec2 vTextureCoord;
in vec3 vNormal;

out vec4 fragColor;

const float verticalSegments = 18.0;   // 纵向切分数
const float horizontalSegments = 36.0; // 横向切分数

// 计算该顶点的散射光最终强度
vec4 calDiffuseLight(
    vec3 normal,
    vec3 lightLocation,
    vec4 ligthIntensity
) {
    // 顶点进行模型转换
    vec3 finalPosition = (uModelMatrix * vec4(vPosition, 1)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = vPosition + normal;
    vec3 realNormal = (uModelMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 计算顶点到光源的向量
    vec3 lightVector;
    if (uLightSourceType == 1) {
        lightVector = normalize(lightLocation - (uModelMatrix * vec4(vPosition, 1)).xyz);
    } else {
        lightVector = normalize(lightLocation);
    }
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
    vec3 finalPosition = (uModelMatrix * vec4(vPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = vPosition + normal;
    vec3 realNormal = (uModelMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 顶点到相机的向量
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
    return ligthIntensity * powerResult;
}

vec2 calculateUV(vec3 point) {
    // 横向，即在 xz 面，
    // atan(normal.z, normal.x) 计算出 normal 在 xz 面上与 (1, 0, 0) 的夹角，atan 结果为弧度
    // 2 * PI（即 360 度），atan/ 2 * PI 范围落在 [0, 1] 区间
    float u = 0.5 + atan(point.z, point.x) / (2.0 * 3.14159);
    // 纵向
    // asin(normal.y) 计算出 normal 与 xz 平面的夹角，asin 结果为弧度
    // asin(normal.y) / 3.14159 范围落在 [-0.5, 0.5] 区间
    float v = 0.5 + asin(point.y) / 3.14159;
    return vec2(u, v);
}

vec4 calColor() {
    // 归一化顶点坐标
    vec3 point = normalize(vPosition.xyz);
    // 将给定的法向量转换为球体纹理坐标
    vec2 uv = calculateUV(point);

    int verticalSegmentIndex = int(uv.y * verticalSegments);
    int horizontalSegmentIndex = int(uv.x * horizontalSegments);

    // 计算最终颜色
    if ((horizontalSegmentIndex + verticalSegmentIndex) % 2 == 0) {
        return uColor;
    } else {
        return vec4(1.0);
    }
}

float computeFogFactor() {
    float fogDistance = length(uViewPoint - (uModelMatrix * vec4(vPosition, 1)).xyz);
    float factor = 1.0 - smoothstep(uStartFog, uEndFog, fogDistance);
    return factor;
}

void main() {
    // 环境光
    vec4 ambientLight = vec4(0);
    if (uIsAddAmbientLight == 1) {
        ambientLight = ambientLightCoefficient;
    }

    // 散射光
    vec4 diffuseLight = vec4(0);
    if (uIsAddDiffuseLight == 1) {
        diffuseLight = calDiffuseLight(normalize(vNormal), uLightPoint, diffuseLightCoefficient);
    }

    // 镜面光
    vec4 specularLight = vec4(0);
    if (uIsAddSpecularLight == 1) {
        specularLight = calSpecularLight(normalize(vNormal), uLightPoint, specularLightCoefficient);
    }

    vec4 orgColor;
    if (uTextureType == 1) {
        orgColor = uColor;
    } else if (uTextureType == 2) {
        orgColor = calColor();
    } else {
        orgColor = texture(uTexture, vTextureCoord);
    }

    float fogFactor = computeFogFactor();
    if (fogFactor > 0.0) {
        vec4 lightColor = orgColor * ambientLight + orgColor * diffuseLight + orgColor * specularLight;
        fragColor = lightColor * fogFactor + uFogColor * (1.0 - fogFactor);
    } else {
        fragColor = uFogColor;
    }
}