uniform mat4 uMVPMatrix;
uniform float vPointSize;
attribute vec4 vPosition;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    gl_PointSize = vPointSize;
}
