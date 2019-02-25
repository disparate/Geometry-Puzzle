package by.kazarovets.geometrypuzzle.rendering.shapes

import android.opengl.GLES20
import android.util.Log
import by.kazarovets.geometrypuzzle.rendering.shader.*
import by.kazarovets.geometrypuzzle.utils.createProgram
import by.kazarovets.geometrypuzzle.utils.createShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class BaseShape {

    var buffer: FloatBuffer? = null

    abstract val vertices: FloatArray


    protected var uColorLocation: Int = 0
    protected var aPositionLocation: Int = 0
    protected var uMatrixLocation: Int = 0
    protected var programId: Int = 0


    init {
        val vertexShaderId = createShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShaderId = createShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        programId = createProgram(vertexShaderId, fragmentShaderId)
    }

    //Need to be called inside child's constructor
    protected fun allocateData() {
        buffer = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
    }

    open fun getTransformMatrix(matrix: FloatArray): FloatArray = matrix

    fun draw(matrix: FloatArray) {
        GLES20.glUseProgram(programId)

        uMatrixLocation = glGetMatrixLocation(programId)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, getTransformMatrix(matrix), 0)

        aPositionLocation = glGetPositionLocation(programId)

        GLES20.glEnableVertexAttribArray(aPositionLocation)

        GLES20.glVertexAttribPointer(
            aPositionLocation, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            0, buffer
        )

        uColorLocation = glGetColorLocation(programId)

        drawContent(matrix)

        GLES20.glDisableVertexAttribArray(aPositionLocation)
    }

    protected abstract fun drawContent(matrix: FloatArray)


    companion object {
        private const val COORDS_PER_VERTEX = 3
    }
}