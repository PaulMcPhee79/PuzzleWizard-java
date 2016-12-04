attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_stencilCoord0;
varying vec2 v_stencilCoord1;

uniform float u_shieldAlpha;
uniform float u_stencilRotation0;
uniform float u_stencilRotation1;

void main() {
    v_color = a_color * u_shieldAlpha;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;

    float s = sin(u_stencilRotation0);
    float c = cos(u_stencilRotation0);
    mat2 rotMatrix = mat2(c, -s, s, c);
    rotMatrix *= 0.5;
    rotMatrix += 0.5;
    rotMatrix = rotMatrix * 2.0 - 1.0;
    v_stencilCoord0 = a_texCoord0 - 0.5;
    v_stencilCoord0 = v_stencilCoord0 * rotMatrix + 0.5;

    s = sin(u_stencilRotation1);
    c = cos(u_stencilRotation1);
    rotMatrix = mat2(c, -s, s, c);
    rotMatrix *= 0.5;
    rotMatrix += 0.5;
    rotMatrix = rotMatrix * 2.0 - 1.0;
    v_stencilCoord1 = a_texCoord0 - 0.5;
    v_stencilCoord1 = v_stencilCoord1 * rotMatrix + 0.5;
}
