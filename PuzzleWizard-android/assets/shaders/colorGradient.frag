
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_gradientTex;
uniform vec2 u_gradientCoords;

void main() {
    vec4 texSample = texture2D(u_texture, v_texCoords);
    vec4 gradientSample = texture2D(u_gradientTex, u_gradientCoords);
    gl_FragColor = (0.65 * texSample + 0.7 * texSample.a * gradientSample); // * v_color;
}
