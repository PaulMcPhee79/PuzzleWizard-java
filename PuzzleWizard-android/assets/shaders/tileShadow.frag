
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_occTexCoords;

uniform sampler2D u_texture;
uniform sampler2D u_occTex;

void main() {
    float occSample = 1.0 - texture2D(u_occTex, v_occTexCoords).r;
    gl_FragColor = texture2D(u_texture, v_texCoords) * occSample * v_color;
}
