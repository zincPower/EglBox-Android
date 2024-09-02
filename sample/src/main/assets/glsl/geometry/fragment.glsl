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

in vec2 vTextureCoord;

uniform sampler2D sTexture;

out vec4 fragColor;

void main() {
    int i = int(floor(vPosition.x * 4.0));
    int j = int(floor(vPosition.y * 4.0));
    int k = int(floor(vPosition.z * 4.0));

//    vec4 color;
//    int whichColor = int(mod(float(i + j + k), 2.0));
//    if (whichColor == 1) {
//        color = vec4(128.0 / 255.0, 190.0 / 255.0, 245.0 / 255.0, 1.0);
//    } else {
//        color = vec4(1.0);
//    }

    vec4 color = texture(sTexture, vTextureCoord);

    fragColor = color * vAmbientLight + color * vDiffuseLight + color * vSpecularLight;
//    fragColor = color;
}