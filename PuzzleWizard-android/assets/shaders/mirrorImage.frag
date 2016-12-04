
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_shaderCoords;

uniform sampler2D u_texture;
uniform sampler2D u_gradientTex;
uniform float u_gradientCoord;

void main() {
    vec4 texSample = texture2D(u_texture, v_texCoords);
    vec4 gradientSample = texture2D(u_gradientTex, vec2(0.2 * v_shaderCoords.x + u_gradientCoord, v_shaderCoords.y));
    float mirror = 0.65 * gradientSample.a;
    gl_FragColor = vec4(texSample.rgb, texSample.a * mirror) * v_color;
}
