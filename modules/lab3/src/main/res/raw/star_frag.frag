precision mediump float;
uniform float vResolutionWidth; // Разрешение экрана
uniform float vResolutionHeight; // Разрешение экрана

void main() {
    float x = gl_FragCoord.x / vResolutionWidth;
    float y = gl_FragCoord.y / vResolutionHeight;

    float dist = 1.3 * distance(vec2(x, y), vec2(0.5, 0.5));
    dist = clamp(dist, 0.0, 1.0);

    gl_FragColor = vec4(vec3(dist), 1.0);
}