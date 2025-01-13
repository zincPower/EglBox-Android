#version 300 es
precision mediump float;

uniform sampler2D sTextureColorChart;

in vec2 vTextureCoord;

out vec4 fragColor;

void main() {
    fragColor = texture(sTextureColorChart, vTextureCoord);
}