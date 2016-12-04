attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_scrollCoords;

uniform vec2 u_displacementScroll;
uniform float u_scroll;

void main() {
    v_color = a_color;
    v_texCoords = u_displacementScroll + a_texCoord0;
	v_scrollCoords = vec2(a_texCoord0.x + u_scroll, a_texCoord0.y + u_scroll);
    gl_Position = u_projTrans * a_position;
}
