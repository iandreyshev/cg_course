precision mediump float;

void main() {
    vec2 resolution = vec2(1080, 2400);
    vec2 st = gl_FragCoord.xy / resolution;
    vec3 color = vec3(st.x, st.y, 0.5);

    gl_FragColor = vec4(color, 1.0);
}
