#version 300 es
precision mediump float;

in vec2 vTexCoord;
in float currY;

uniform sampler2D sTextureGrass;
uniform sampler2D sTextureRock;

uniform float uBoundaryStartY;
uniform float uBoundaryEndY;

out vec4 fragColor;

void main() {
    vec4 finalColor;
    if (currY < uBoundaryStartY) {
        vec4 grassColor = texture(sTextureGrass, vTexCoord);
        finalColor = grassColor;
    } else if (currY > uBoundaryEndY) {
        vec4 rockColor = texture(sTextureRock, vTexCoord);
        finalColor = rockColor;
    } else {
        vec4 grassColor = texture(sTextureGrass, vTexCoord);
        vec4 rockColor = texture(sTextureRock, vTexCoord);
        float ratio = (currY - uBoundaryStartY) / (uBoundaryEndY - uBoundaryStartY);
        finalColor = (1.0 - ratio) * grassColor + ratio * rockColor;
    }
    fragColor = finalColor;
}