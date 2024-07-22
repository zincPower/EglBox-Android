#version 300 es
precision mediump float;

in vec3 vPosition;
out vec4 fragColor;

const int verticalSegments = 18;   // 纵向切分数
const int horizontalSegments = 36; // 横向切分数

vec2 calculateUV(vec3 normal) {
    float u = 0.5 + atan(normal.z, normal.x) / (2.0 * 3.14159);
    float v = 0.5 - asin(normal.y) / 3.14159;
    return vec2(u, v);
}

/**
 * 计算两个向量间的夹角
 */
vec4 calColor() {
    vec3 point = normalize(vPosition.xyz);  // 归一化的顶点坐标

    // 计算纵向切分的颜色
    float verticalStep = 1.0 / float(verticalSegments);
    float v = calculateUV(point).y;
    int verticalSegmentIndex = int(v / verticalStep);

    // 计算横向切分的颜色
    float horizontalStep = 1.0 / float(horizontalSegments);
    float u = calculateUV(point).x;
    int horizontalSegmentIndex = int(u / horizontalStep);
    if ((horizontalSegmentIndex + verticalSegmentIndex) % 2 == 0) {
        return vec4(128.0 / 255.0, 190.0 / 255.0, 245.0 / 255.0, 1.0);
    } else {
        return vec4(1.0);
    }
}

void main() {
    vec4 orgColor = calColor();
    fragColor = orgColor;
}