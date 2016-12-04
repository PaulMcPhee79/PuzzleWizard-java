attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_occTexCoords;
uniform vec4 u_occRegion;

void main() {
    v_color = a_color;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;

    v_occTexCoords = vec2(a_texCoord0.x - u_occRegion.x, 1.0 - (a_texCoord0.y + u_occRegion.y));
    mat2 scaleMatrix = mat2(1.0 / u_occRegion.z, 0.0, 0.0, 1.0 / u_occRegion.w);
    v_occTexCoords = v_occTexCoords * scaleMatrix;
}
