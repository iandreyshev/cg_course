precision mediump float;

uniform vec3 uLightPosition;

varying vec4 fColor;
varying vec3 fNormal;
varying vec3 fWorldPos;

void main() {
    vec3 normal = normalize(fNormal);
    vec3 lightDir = normalize(uLightPosition - fWorldPos);

    float diffuse = max(dot(normal, lightDir), 0.0);

    float dist = length(uLightPosition - fWorldPos);
    float attenuation = 1.0 / (1.0 + 0.09 * dist + 0.032 * dist * dist);

    float ambient = 0.15;
    float brightness = (ambient + (1.0 - ambient) * diffuse) * attenuation;

    gl_FragColor = vec4(fColor.rgb * brightness, fColor.a);
}