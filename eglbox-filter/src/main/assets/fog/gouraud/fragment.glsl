#version 300 es
precision mediump float;

// 纹理()
uniform sampler2D uTexture;
// 颜色
uniform vec4 uColor;
// 纹理类型，1：纯色；2：格子颜色；3、纹理
uniform int uTextureType;
// 雾颜色
uniform vec4 uFogColor;

in vec3 vPosition;
in vec2 vTextureCoord;
in vec4 vAmbientLight;
in vec4 vDiffuseLight;
in vec4 vSpecularLight;
in float vFogFactor;

out vec4 fragColor;

const float verticalSegments = 18.0;   // 纵向切分数
const float horizontalSegments = 36.0; // 横向切分数

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

void main() {
    vec4 orgColor;
    if (uTextureType == 1) {
        orgColor = uColor;
    } else if (uTextureType == 2) {
        orgColor = calColor();
    } else {
        orgColor = texture(uTexture, vTextureCoord);
    }

    if (vFogFactor > 0.0) {
        vec4 lightColor = orgColor * vAmbientLight + orgColor * vDiffuseLight + orgColor * vSpecularLight;
        fragColor = lightColor * vFogFactor + uFogColor * (1.0 - vFogFactor);
    } else {
        fragColor = uFogColor;
    }
}