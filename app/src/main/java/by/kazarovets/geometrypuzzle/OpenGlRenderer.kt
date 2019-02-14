package by.kazarovets.geometrypuzzle


import android.animation.ValueAnimator
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
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glLineWidth
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import by.kazarovets.geometrypuzzle.utils.createProgram
import by.kazarovets.geometrypuzzle.utils.createShader
import timber.log.Timber

class OpenGLRenderer(private val context: Context) : Renderer {

    private var vertexData: FloatBuffer? = null
    private var uColorLocation: Int = 0
    private var aPositionLocation: Int = 0
    private var uMatrixLocation: Int = 0
    private var programId: Int = 0

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)

    private var historyRotateMatrix = FloatArray(16)
    private val currRotateMatrix = FloatArray(16)
    private val deltaRotateMatrix = FloatArray(16)

    private var cameraPoint = CameraPoint(3f)

    private var rotateAngle = 0f
    private var rotateVector = Vector(1f, 0f, 0f)

    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        glClearColor(0f, 0f, 0f, 1f)
        glEnable(GL_DEPTH_TEST)
        val vertexShaderId = createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShaderId = createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = createProgram(vertexShaderId, fragmentShaderId)
        glUseProgram(programId)
        pointCamera()
        prepareData()
        bindData()
    }

    override fun onSurfaceChanged(arg0: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        createProjectionMatrix(width, height)
        Matrix.setRotateM(currRotateMatrix, 0, 0f, 0f, 0f, 1f)
        Matrix.setRotateM(historyRotateMatrix, 0, 0f, 0f, 0f, 1f)

        bindMatrix()
    }

    private fun prepareData() {

        val s = 1f
        val l = 10f

        val vertices = floatArrayOf(


            // ось X
            -l, 0f, 0f, l, 0f, 0f,

            // ось Y
            0f, -l, 0f, 0f, l, 0f,

            // ось Z
            0f, 0f, -l, 0f, 0f, l,

            // line 1
            s, 0f, 0f, s, s, 0f,

            // line 2
            -s, s, 0f, -s, 2 * s, 0f,

            //line 3
            0f, 0f, 0f, 0f, s, s


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
        var left = -4f
        var right = 4f
        var bottom = -4f
        var top = 4f
        val near = 1f
        val far = 9f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }

        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    fun swipeCamera(swipeDirection: SwipeDirection) {
        rotateVector = when(swipeDirection) {
            SwipeDirection.RIGHT -> cameraPoint.upVector
            SwipeDirection.LEFT -> cameraPoint.upVector.invert()
            SwipeDirection.UP -> cameraPoint.upPerpendicularVector
            SwipeDirection.BOTTOM -> cameraPoint.upPerpendicularVector.invert()
        }
        Timber.d("rotate $rotateVector")
        ValueAnimator.ofFloat(0f, 90f).apply {
            duration = 500
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                this@OpenGLRenderer.rotateAngle = this.animatedValue as Float
                pointCamera()
            }
            doOnEnd {
                rotateAngle = 0f
                historyRotateMatrix = currRotateMatrix.clone() //TODO: check
            }
            start()
        }
    }

    private fun pointCamera() {
        cameraPoint.apply {
            Matrix.setLookAtM(
                mViewMatrix, 0,
                fromX, fromY, fromZ,
                toX, toY, toZ,
                upX, upY, upZ
            )
        }

        Matrix.setRotateM(deltaRotateMatrix, 0, rotateAngle,
            rotateVector.x, rotateVector.y, rotateVector.z)
        Matrix.multiplyMM(currRotateMatrix, 0, deltaRotateMatrix, 0, historyRotateMatrix, 0)
    }


    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, currRotateMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix,  0, mMatrix, 0)
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

    override fun onDrawFrame(arg0: GL10) {
        bindMatrix()
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // оси
        glLineWidth(1f)

        glUniform4f(uColorLocation, 0.33f, 0.33f, 0.33f, 1.0f)
        glDrawArrays(GL_LINES, 0, 2)
        glDrawArrays(GL_LINES, 2, 2)
        glDrawArrays(GL_LINES, 4, 2)


        glLineWidth(3f)
        // lines
        glUniform4f(uColorLocation, 0f, 1.0f, 1.0f, 1.0f)
        glDrawArrays(GL_LINES, 6, 2)

        glUniform4f(uColorLocation, 1.0f, 1.0f, 0f, 1.0f)
        glDrawArrays(GL_LINES, 8, 2)

        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f)
        glDrawArrays(GL_LINES, 10, 2)

    }

    companion object {

        private val POSITION_COUNT = 3
    }

}