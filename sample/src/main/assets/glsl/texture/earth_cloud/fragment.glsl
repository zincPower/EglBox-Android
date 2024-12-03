#version 300 es
precision mediump float;

uniform sampler2D uTexture;

in vec2 vTextureCoord;
in vec4 vAmbientLight;
in vec4 vDiffuseLight;
in vec4 vSpecularLight;

out vec4 fragColor;

void main() {
    vec4 textureColor = texture(uTexture, vTextureCoord);
    textureColor.a = (textureColor.r + textureColor.g + textureColor.b) / 3.0;
    fragColor = textureColor * vAmbientLight + textureColor * vDiffuseLight + textureColor * vSpecularLight;
}