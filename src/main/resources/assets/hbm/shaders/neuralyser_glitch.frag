#version 120

uniform sampler2D tex;
uniform sampler2D trailTex;
uniform float time;
uniform float amount;
const float BITS = 4.0;
const float MOSAIC = 90.0;
const float SATURATION = 1.7;
const float TRAIL_DECAY = 0.72;
const vec3 TRAIL_TINT = vec3(0.6, 1.0, 0.75);
const float ZOOM = 0.55;
const vec3 CRT_TINT = vec3(0.45, 1.0, 0.6);

void main() {
	vec2 uv = gl_TexCoord[0].st;
	vec2 c = uv - 0.5;
	float r = length(c);
	float angle = time * 1.1 + r * 5.0 * sin(time * 0.6);
	float s = sin(angle);
	float co = cos(angle);
	vec2 warped = vec2(c.x * co - c.y * s, c.x * s + c.y * co) * (1.0 + 0.05 * sin(time * 3.0));

	warped *= 1.0 + ZOOM * amount;

	vec2 p = warped + 0.5;

	vec2 m = floor(p * MOSAIC) / MOSAIC;


	float ca = 0.009 + 0.005 * sin(time * 4.0);
	float wobble = 0.005 * sin(p.y * 40.0 + time * 7.0);
	vec3 col;
	col.r = texture2D(tex, vec2(m.x + wobble + ca, m.y)).r;
	col.g = texture2D(tex, vec2(m.x + wobble, m.y)).g;
	col.b = texture2D(tex, vec2(m.x + wobble - ca, m.y)).b;

	col = col - fract(col * BITS) / BITS;
	float luma = dot(col, vec3(0.3, 0.59, 0.11));
	col = mix(vec3(luma), col, SATURATION);

	vec3 trail = texture2D(trailTex, uv).rgb;
	col = max(trail * TRAIL_DECAY * TRAIL_TINT, col);
	col *= 0.9 + 0.1 * sin(gl_FragCoord.y * 1.5);

	col *= 0.88 + 0.12 * sin(time * 5.0);
	col.r *= 1.0 + 0.12 * sin(time * 2.0);
	col.g *= 1.0 + 0.12 * sin(time * 2.0 + 2.1);
	col.b *= 1.0 + 0.12 * sin(time * 2.0 + 4.0);

	vec3 base = texture2D(tex, uv).rgb;
	gl_FragColor = vec4(mix(base, clamp(col, 0.0, 1.0), amount), 1.0);
}
