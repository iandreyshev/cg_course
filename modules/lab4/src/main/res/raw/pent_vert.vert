uniform mat4 uMVPMatrix;
uniform mat4 uModelMatrix;

attribute vec4 vPosition;
attribute vec4 vColor;
attribute vec3 vNormal;

varying vec4 fColor;
varying vec3 fNormal;
varying vec3 fWorldPos;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    fColor = vColor;
    fNormal = mat3(uModelMatrix) * vNormal;
    fWorldPos = vec3(uModelMatrix * vPosition);
}
