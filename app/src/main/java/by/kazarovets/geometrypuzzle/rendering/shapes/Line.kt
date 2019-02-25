package by.kazarovets.geometrypuzzle.rendering.shapes

import android.opengl.GLES20
import by.kazarovets.geometrypuzzle.rendering.OpenGLRenderer
import by.kazarovets.geometrypuzzle.rendering.shader.*
import by.kazarovets.geometrypuzzle.utils.createProgram
import by.kazarovets.geometrypuzzle.utils.createShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

data class Line(val xStart: Float, val yStart: Float,val zStart: Float,
           val xEnd: Float, val yEnd: Float, val zEnd: Float) {

    var buffer: FloatBuffer? = null

    val vertices = floatArrayOf(xStart, yStart, zStart, xEnd, yEnd, zEnd)

    private var uColorLocation: Int = 0
    private var aPositionLocation: Int = 0
    private var uMatrixLocation: Int = 0
    private var programId: Int = 0

    var color = Triple(1f, 1f, 1f)
    var lineWidth = 3f


    init {
        val vertexShaderId = createShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShaderId = createShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        programId = createProgram(vertexShaderId, fragmentShaderId)
    }

    fun allocateData() {
        buffer = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
    }

    fun draw(matrix: FloatArray) {
        GLES20.glUseProgram(programId)

        // матрица
        uMatrixLocation = glGetMatrixLocation(programId)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        // примитивы
        aPositionLocation = glGetPositionLocation(programId)

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(aPositionLocation, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            0, buffer)


        uColorLocation = glGetColorLocation(programId)
        GLES20.glUniform4f(uColorLocation, color.first, color.second, color.third, 1.0f)

        GLES20.glLineWidth(lineWidth)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)

        GLES20.glDisableVertexAttribArray(aPositionLocation)
    }

    companion object {

        private const val COORDS_PER_VERTEX = 3
    }
}