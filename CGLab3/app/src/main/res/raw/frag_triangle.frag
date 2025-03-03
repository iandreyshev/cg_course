precision mediump float;

uniform vec4 vColor;

void main() {
  vec2 u_resolution = vec2(1080, 2400);
  vec2 st = gl_FragCoord.xy/u_resolution;
//  st.x *= u_resolution.x/u_resolution.y;

  vec3 color = vec3(0.);
  color = vec3(st.x,st.y,0.5);

  gl_FragColor = vec4(color, 1.0);
}
