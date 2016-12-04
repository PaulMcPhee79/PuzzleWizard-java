
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_noiseTex;
uniform float u_threshold;

void main() {
	float noiseSample = texture2D(u_noiseTex, v_texCoords).r;
	float noiseThreshold = noiseSample - u_threshold;
    vec4 texSample = noiseSample < u_threshold
    	? vec4(0, 0, 0, 0)
    	: texture2D(u_texture, v_texCoords);
    texSample *= noiseThreshold < 0.25
    	? vec4(1.0, 1.0, 1.0, 2.0f * noiseThreshold) // Highlight dissolve edge
    	: vec4(1.0, 1.0, 1.0, 1.0);
    gl_FragColor = texSample * v_color;
}
