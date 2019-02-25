package by.kazarovets.geometrypuzzle.rendering.shapes

import android.opengl.GLES20

class Dot(private val point: Point3D) : BaseShape() {
    override val vertices: FloatArray = floatArrayOf(point.x, point.y, point.z)

    init {
        allocateData()
    }

    var color =  Triple(1f, 1f, 1f)

    override fun drawContent(matrix: FloatArray) {
        GLES20.glLineWidth(1f)

        GLES20.glUniform4f(uColorLocation, color.first, color.second, color.third, 1.0f)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }
}