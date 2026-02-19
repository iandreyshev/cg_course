precision mediump float;

uniform vec3 uLightDirection;

varying vec4 fColor;
varying vec3 fNormal;

void main() {
    vec3 normal = normalize(fNormal);
    vec3 lightDir = normalize(uLightDirection);

    float diffuse = max(dot(normal, lightDir), 0.0);

    float brightness = 0.3 + 0.7 * diffuse;

    gl_FragColor = vec4(fColor.rgb * brightness, fColor.a);
}