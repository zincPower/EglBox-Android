#version 300 es
precision mediump float;

// 顶点位置
in vec3 vPosition;
// 环境光强度
in vec4 vAmbientLight;
// 散射光强度
in vec4 vDiffuseLight;
// 镜面光强度
in vec4 vSpecularLight;

//in vec2 vTextureCoord;

//uniform sampler2D sTexture;

out vec4 fragColor;

const float verticalSegments = 18.0;   // 纵向切分数
const float horizontalSegments = 36.0; // 横向切分数

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

    //    // 计算最终颜色
    //    if ((horizontalSegmentIndex + verticalSegmentIndex) % 2 == 0) {
    //        return vec4(128.0 / 255.0, 190.0 / 255.0, 245.0 / 255.0, 1.0);
    //    } else {
    //        return vec4(1.0);
    //    }

    return vec4(1.0);
}

void main() {
    vec4 orgColor = calColor();
    fragColor = orgColor * vAmbientLight + orgColor * vDiffuseLight + orgColor * vSpecularLight;
    //    fragColor = orgColor;//orgColor * vAmbientLight + orgColor * vScatteredLight + orgColor * vSpecularLight;
}