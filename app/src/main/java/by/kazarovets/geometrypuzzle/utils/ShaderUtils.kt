package by.kazarovets.geometrypuzzle.utils


import android.opengl.GLES20.*


fun createProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {

    val programId = glCreateProgram()
    if (programId == 0) {
        return 0
    }

    glAttachShader(programId, vertexShaderId)
    glAttachShader(programId, fragmentShaderId)

    glLinkProgram(programId)
    val linkStatus = IntArray(1)
    glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0)
    if (linkStatus[0] == 0) {
        glDeleteProgram(programId)
        return 0
    }
    return programId

}

internal fun createShader(type: Int, shaderText: String): Int {
    val shaderId = glCreateShader(type)
    if (shaderId == 0) {
        return 0
    }
    glShaderSource(shaderId, shaderText)
    glCompileShader(shaderId)
    val compileStatus = IntArray(1)
    glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0)
    if (compileStatus[0] == 0) {
        glDeleteShader(shaderId)
        return 0
    }
    return shaderId
}


