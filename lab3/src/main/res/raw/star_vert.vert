uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
uniform float vPointSize;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    gl_PointSize = vPointSize;
}
