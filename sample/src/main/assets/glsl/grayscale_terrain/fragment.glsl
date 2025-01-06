#version 300 es
precision mediump float;

uniform sampler2D sTextureLand;
uniform sampler2D sTextureMountain;

uniform float uBoundaryStart;
uniform float uBoundaryEnd;

in vec3 vPosition;
in vec2 vTextureCoord;
in vec4 vAmbientLight;
in vec4 vDiffuseLight;
in vec4 vSpecularLight;
in float altitude;

out vec4 fragColor;

void main() {
    vec4 orgColor;
    if (altitude < uBoundaryStart) {
        orgColor = texture(sTextureLand, vTextureCoord);
    } else if (altitude > uBoundaryEnd) {
        orgColor = texture(sTextureMountain, vTextureCoord);
    } else {
        vec4 landColor = texture(sTextureLand, vTextureCoord);
        vec4 mountainColor = texture(sTextureMountain, vTextureCoord);
        float ratio = (altitude - uBoundaryStart) / (uBoundaryEnd - uBoundaryStart);
        orgColor = (1.0 - ratio) * landColor + ratio * mountainColor;
    }
    fragColor = orgColor * vAmbientLight + orgColor * vDiffuseLight + orgColor * vSpecularLight;
//    fragColor = texture(sTextureLand, vTextureCoord);
}