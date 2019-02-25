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
           val xEnd: Float, val yEnd: Float, val zEnd: Float): BaseShape() {

    override val vertices = floatArrayOf(xStart, yStart, zStart, xEnd, yEnd, zEnd)

    init {
        allocateData()
    }

    var color = Triple(1f, 1f, 1f)
    var lineWidth = 3f

    override fun drawContent(matrix: FloatArray) {
        GLES20.glUniform4f(uColorLocation, color.first, color.second, color.third, 1.0f)
        GLES20.glLineWidth(lineWidth)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
    }
}