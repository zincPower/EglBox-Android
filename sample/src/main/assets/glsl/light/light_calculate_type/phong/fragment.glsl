#version 300 es
precision mediump float;

const float verticalSegments = 18.0;   // 纵向切分数
const float horizontalSegments = 36.0; // 横向切分数

in vec3 vPosition;
in vec3 vNormal;
// 物体变换矩阵，只包括物体的旋转、平移、缩放
uniform mat4 uMMatrix;
// 光源位置
uniform vec3 uLightPosition;
// 相机位置
uniform vec3 uCameraPosition;
// 光滑度
//in float aShininess;

// 控制三种光是否现实
uniform int uIsAddAmbientLight;
uniform int uIsAddScatteredLight;
uniform int uIsAddSpecularLight;

// 光源类型 1：定点光 2：定向光
uniform int uLightSourceType;

out vec4 fragColor;

vec2 calculateUV(vec3 normal) {
    // 横向，即在 xz 面，
    // atan(normal.z, normal.x) 计算出 normal 在 xz 面上与 (1, 0, 0) 的夹角，atan 结果为弧度
    // 2 * PI（即 360 度），atan/ 2 * PI 范围落在 [0, 1] 区间
    float u = 0.5 + atan(normal.z, normal.x) / (2.0 * 3.14159);
    // 纵向
    // asin(normal.y) 计算出 normal 与 xz 平面的夹角，asin 结果为弧度
    // asin(normal.y) / 3.14159 范围落在 [-0.5, 0.5] 区间
    float v = 0.5 + asin(normal.y) / 3.14159;
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
        return vec4(128.0 / 255.0, 190.0 / 255.0, 245.0 / 255.0, 1.0);
    } else {
        return vec4(1.0);
    }
}


// 计算该顶点的散射光最终强度
vec4 calScatteredLight(
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
    realNormal = normalize(realNormal);

    // 计算顶点到光源的向量
    vec3 lightVector;
    if (uLightSourceType == 1) {
        lightVector = normalize(lightLocation - (uMMatrix * vec4(vPosition, 1.0)).xyz);
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
    vec3 finalPosition = (uMMatrix * vec4(vPosition, 1.0)).xyz;

    // 对法向量进行矩阵转换处理，最终归一化
    // realNormal 才是跟随模型矩阵处理后的向量
    vec3 tempNormal = vPosition + normal;
    vec3 realNormal = (uMMatrix * vec4(tempNormal, 1)).xyz - finalPosition;
    realNormal = normalize(realNormal);

    // 顶点到相机的向量
    vec3 eyeVector = normalize(uCameraPosition - finalPosition);
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
    float powerResult = max(0.0, pow(dotResult, 50.0));
    return ligthIntensity * powerResult;
}

void main() {

    // 环境光
    vec4 ambientLight = vec4(0);
    if (uIsAddAmbientLight == 1) {
        ambientLight = vec4(0.15, 0.15, 0.15, 1.0);
    }

    // 散射光
    vec4 diffuseLight = vec4(0);
    if (uIsAddScatteredLight == 1) {
        vec4 scatteredLightIntensity = vec4(0.8, 0.8, 0.8, 1.0);
        diffuseLight = calScatteredLight(vNormal, uLightPosition, scatteredLightIntensity);
    }

    // 镜面光
    vec4 specularLight = vec4(0);
    if (uIsAddSpecularLight == 1) {
        vec4 specularLightIntensity = vec4(0.7, 0.7, 0.7, 1.0);
        specularLight = calSpecularLight(vNormal, uLightPosition, specularLightIntensity);
    }

    vec4 orgColor = calColor();
    fragColor = orgColor * ambientLight + orgColor * diffuseLight + orgColor * specularLight;
}