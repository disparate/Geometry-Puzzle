package by.kazarovets.geometrypuzzle


import android.content.Context
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINES
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glLineWidth
import by.kazarovets.geometrypuzzle.utils.createProgram
import by.kazarovets.geometrypuzzle.utils.createShader

class OpenGLRenderer(private val context: Context) : Renderer {

    private var vertexData: FloatBuffer? = null
    private var uColorLocation: Int = 0
    private var aPositionLocation: Int = 0
    private var uMatrixLocation: Int = 0
    private var programId: Int = 0

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)

    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        glClearColor(0f, 0f, 0f, 1f)
        glEnable(GL_DEPTH_TEST)
        val vertexShaderId = createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShaderId = createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = createProgram(vertexShaderId, fragmentShaderId)
        glUseProgram(programId)
        createViewMatrix()
        prepareData()
        bindData()
    }

    override fun onSurfaceChanged(arg0: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        createProjectionMatrix(width, height)
        bindMatrix()
    }

    private fun prepareData() {

        val s = 0.4f
        val d = 0.9f
        val l = 3f

        val vertices = floatArrayOf(

            // первый треугольник
            -2 * s, -s, d, 2 * s, -s, d, 0f, s, d,

            // второй треугольник
            -2 * s, -s, -d, 2 * s, -s, -d, 0f, s, -d,

            // третий треугольник
            d, -s, -2 * s, d, -s, 2 * s, d, s, 0f,

            // четвертый треугольник
            -d, -s, -2 * s, -d, -s, 2 * s, -d, s, 0f,

            // ось X
            -l, 0f, 0f, l, 0f, 0f,

            // ось Y
            0f, -l, 0f, 0f, l, 0f,

            // ось Z
            0f, 0f, -l, 0f, 0f, l
        )

        vertexData = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData!!.put(vertices)
    }

    private fun bindData() {
        // примитивы
        aPositionLocation = glGetAttribLocation(programId, "a_Position")
        vertexData!!.position(0)
        glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT, GL_FLOAT,
            false, 0, vertexData
        )
        glEnableVertexAttribArray(aPositionLocation)

        // цвет
        uColorLocation = glGetUniformLocation(programId, "u_Color")

        // матрица
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix")
    }

    private fun createProjectionMatrix(width: Int, height: Int) {
        var ratio = 1f
        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 2f
        val far = 8f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    private fun createViewMatrix() {
        // точка положения камеры
        val eyeX = 0f
        val eyeY = 0f
        val eyeZ = 3f

        // точка направления камеры
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // up-вектор
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
    }


    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

    override fun onDrawFrame(arg0: GL10) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // треугольники
        glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f)
        glDrawArrays(GL_TRIANGLES, 0, 3)

        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f)
        glDrawArrays(GL_TRIANGLES, 3, 3)

        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        glDrawArrays(GL_TRIANGLES, 6, 3)

        glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f)
        glDrawArrays(GL_TRIANGLES, 9, 3)

        // оси
        glLineWidth(1f)

        glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f)
        glDrawArrays(GL_LINES, 12, 2)

        glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f)
        glDrawArrays(GL_LINES, 14, 2)

        glUniform4f(uColorLocation, 1.0f, 0.5f, 0.0f, 1.0f)
        glDrawArrays(GL_LINES, 16, 2)
    }

    companion object {

        private val POSITION_COUNT = 3
    }

}