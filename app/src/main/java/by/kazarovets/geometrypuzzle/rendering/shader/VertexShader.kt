package by.kazarovets.geometrypuzzle.rendering.shader

import android.opengl.GLES20

const val VERTEX_SHADER_CODE = """attribute vec4 a_Position;
uniform mat4 u_Matrix;

void main()
{
    gl_Position = u_Matrix * a_Position;
    gl_PointSize = 5.0;
}"""

fun glGetPositionLocation(programId: Int): Int {
    return GLES20.glGetAttribLocation(programId, "a_Position")
}


fun glGetMatrixLocation(programId: Int): Int {
    return GLES20.glGetUniformLocation(programId, "u_Matrix")
}