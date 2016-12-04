
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_scrollCoords;

uniform sampler2D u_texture;
uniform sampler2D u_displacementTexture;


void main() {
	float displacement = texture2D(u_displacementTexture, v_texCoords).x;
	vec2 texCoords = vec2(v_scrollCoords.x + displacement, v_scrollCoords.y + displacement);
    gl_FragColor = texture2D(u_texture, texCoords) * v_color;
}
