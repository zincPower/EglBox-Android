#version 300 es
precision mediump float;

uniform sampler2D sTexture;

in vec2 vTextureCoord;
in vec4 vAmbientLight;
in vec4 vScatteredLight;
in vec4 vSpecularLight;

out vec4 fragColor;

void main() {
    vec4 textureColor = texture(sTexture, vTextureCoord);
    fragColor = textureColor * vAmbientLight + textureColor * vScatteredLight + textureColor * vSpecularLight;
}