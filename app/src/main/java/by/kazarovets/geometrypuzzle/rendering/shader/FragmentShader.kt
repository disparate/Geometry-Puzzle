package by.kazarovets.geometrypuzzle.rendering.shader

import android.opengl.GLES20

const val FRAGMENT_SHADER_CODE = """ precision mediump float;
uniform vec4 u_Color;

void main()
{
    gl_FragColor = u_Color;
}"""


fun glGetColorLocation(programId: Int): Int {
    return GLES20.glGetUniformLocation(programId, "u_Color")
}
