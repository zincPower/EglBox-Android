#version 300 es
precision mediump float;

uniform sampler2D sTextureLand;
uniform sampler2D sTextureMountain;
uniform sampler2D sTextureSnow;

uniform float uMountainBoundaryStart;
uniform float uMountainBoundaryEnd;

uniform float uSnowBoundaryStart;
uniform float uSnowBoundaryEnd;

in vec3 vPosition;
in vec2 vTextureCoord;
in vec4 vAmbientLight;
in vec4 vDiffuseLight;
in vec4 vSpecularLight;
in float altitude;

out vec4 fragColor;

void main() {
    vec4 orgColor;
    if (altitude <= uMountainBoundaryStart) {
        orgColor = texture(sTextureLand, vTextureCoord);
    } else if (uMountainBoundaryStart < altitude && altitude < uMountainBoundaryEnd) {
        vec4 landColor = texture(sTextureLand, vTextureCoord);
        vec4 mountainColor = texture(sTextureMountain, vTextureCoord);
        float ratio = (altitude - uMountainBoundaryStart) / (uMountainBoundaryEnd - uMountainBoundaryStart);
        orgColor = (1.0 - ratio) * landColor + ratio * mountainColor;
    } else if (uMountainBoundaryEnd <= altitude && altitude <= uSnowBoundaryStart) {
        orgColor = texture(sTextureMountain, vTextureCoord);
    } else if (uSnowBoundaryStart < altitude && altitude < uSnowBoundaryEnd) {
        vec4 mountainColor = texture(sTextureMountain, vTextureCoord);
        vec4 snowColor = texture(sTextureSnow, vTextureCoord);
        float ratio = (altitude - uSnowBoundaryStart) / (uSnowBoundaryEnd - uSnowBoundaryStart);
        orgColor = (1.0 - ratio) * mountainColor + ratio * snowColor;
    } else {
        orgColor = texture(sTextureSnow, vTextureCoord);
    }
    fragColor = orgColor * vAmbientLight + orgColor * vDiffuseLight + orgColor * vSpecularLight;
}