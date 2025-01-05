#version 300 es

uniform mat4 uMVPMatrix;

in vec3 aPosition;
in vec2 aTexCoord;

out vec2 vTexCoord;
out float currY;

void main() {
    gl_Position = uMVPMatrix * vec4(aPosition, 1);
    vTexCoord = aTexCoord;
    currY = aPosition.y;
}