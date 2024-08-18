#version 300 es
precision mediump float;

uniform sampler2D sTextureDay;
uniform sampler2D sTextureNight;

in vec2 vTextureCoord;
in vec4 vAmbientLight;
in vec4 vDiffuseLight;
in vec4 vSpecularLight;

out vec4 fragColor;

void main() {
    vec4 textureDayColor = texture(sTextureDay, vTextureCoord);
    vec4 finalDayColor = textureDayColor * vAmbientLight + textureDayColor * vDiffuseLight + textureDayColor * vSpecularLight;

    vec4 finalNightColor =  texture(sTextureNight, vTextureCoord);

    if (vDiffuseLight.x > 0.25) {
        fragColor = finalDayColor;
    } else if (vDiffuseLight.x < 0.05) {
        fragColor = finalNightColor;
    } else {
        float ratio = (vDiffuseLight.x - 0.05) / 0.2;
        fragColor = ratio * finalDayColor + (1.0 - ratio) * finalNightColor;
    }
}