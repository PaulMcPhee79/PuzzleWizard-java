
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_shaderCoords;

uniform sampler2D u_texture;
uniform sampler2D u_gradientTex;
uniform float u_gradientCoord;

void main() {
    vec4 texSample = texture2D(u_texture, v_texCoords);
    vec4 gradientSample = texture2D(u_gradientTex, vec2(0.25 * v_shaderCoords.x + u_gradientCoord, v_shaderCoords.y));
    float sparkle = 1.25 * texSample.a * gradientSample.a * gradientSample.b;
    gl_FragColor = vec4(texSample.rgb + sparkle, texSample.a) * v_color;
}
