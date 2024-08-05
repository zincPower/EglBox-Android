#version 300 es
precision mediump float;

in vec3 vPosition;
in vec4 vAmbientLight;
in vec4 vScatteredLight;
in vec4 vSpecularLight;

out vec4 fragColor;

vec4 calColor() {
    float modX = mod(vPosition.x, 0.5);
    float modY = mod(vPosition.y, 0.5);
    vec4 color;
    if (modX < 0.25 && modY < 0.25) {
        color = vec4(128.0 / 255.0, 190.0 / 255.0, 245.0 / 255.0, 1.0);
    } else if (modX >= 0.25 && modY >= 0.25) {
        color = vec4(128.0 / 255.0, 190.0 / 255.0, 245.0 / 255.0, 1.0);
    } else {
        color = vec4(1.0);
    }
    return color;
}

void main() {
    vec4 orgColor = calColor();
    fragColor = orgColor * vAmbientLight + orgColor * vScatteredLight + orgColor * vSpecularLight;
}