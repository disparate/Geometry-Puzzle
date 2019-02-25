package by.kazarovets.geometrypuzzle.rendering.models

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import by.kazarovets.geometrypuzzle.rendering.shapes.BaseShape
import by.kazarovets.geometrypuzzle.rendering.shapes.Point3D

class Player(private val size: Float, var position: Point3D) : BaseShape() {

    private var prevPosition: Point3D? = null
    private var prevMatrix: FloatArray? = null

    override val vertices: FloatArray = floatArrayOf(
            0f, 0f, -size,
            -size, 0f, 0f,
            0f, -size, 0f,
            size, 0f, 0f,
            0f, size, 0f,
            -size, 0f, 0f,


            0f, 0f, size,
            -size, 0f, 0f,
            0f, -size, 0f,
            size, 0f, 0f,
            0f, size, 0f,
            -size, 0f, 0f
        )

    init {
        allocateData()
    }

    private fun moveMatrix(matrix: FloatArray, position: Point3D): FloatArray {
        var resMatrix = matrix
        if (prevPosition != position || prevMatrix == null) {
            resMatrix = matrix.clone()
            Matrix.translateM(resMatrix, 0, position.x, position.y, position.z)
        } else {
            resMatrix = prevMatrix ?: matrix
        }
        return resMatrix

    }


    override fun getTransformMatrix(matrix: FloatArray): FloatArray {
        return moveMatrix(matrix, position)
    }

    override fun drawContent(matrix: FloatArray) {
        GLES20.glUniform4f(uColorLocation, 0.5f, 0.5f, 0.5f, 1.0f)

        GLES20.glLineWidth(1f)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 6, 6)
    }

}