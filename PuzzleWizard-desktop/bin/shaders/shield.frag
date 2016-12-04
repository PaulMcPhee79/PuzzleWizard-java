
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_stencilCoord0;
varying vec2 v_stencilCoord1;

uniform vec2 u_displacementScroll;
uniform sampler2D u_texture;
uniform sampler2D u_plasmaTex;
uniform sampler2D u_sin2xTable;
uniform sampler2D u_stencilTex0;
uniform sampler2D u_stencilTex1;

void main() {
    vec4 stencilSample = texture2D(u_stencilTex0, v_stencilCoord0) * texture2D(u_stencilTex1, v_stencilCoord1);
    vec4 texSample = texture2D(u_texture, v_texCoords) * (stencilSample.r + stencilSample.g);
    vec2 sinSample = texture2D(u_sin2xTable, v_texCoords).rg;
    vec4 plasmaSample = texture2D(u_plasmaTex, u_displacementScroll + sinSample) * stencilSample.r;
    gl_FragColor = (texSample + 1.35 * texSample.r * texSample.a * plasmaSample) * v_color;
}
