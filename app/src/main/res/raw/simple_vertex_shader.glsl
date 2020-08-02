attribute vec4 a_Position;
uniform float u_PointSize;
attribute vec4 a_Color;

uniform mat4 u_Matrix;

varying vec4 v_Color;
void main() {
    v_Color = a_Color;
    gl_Position = u_Matrix * a_Position;
    gl_PointSize = u_PointSize;
}