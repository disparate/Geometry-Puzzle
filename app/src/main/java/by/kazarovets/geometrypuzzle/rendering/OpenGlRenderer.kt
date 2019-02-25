package by.kazarovets.geometrypuzzle.rendering


import android.animation.ValueAnimator
import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import android.util.Log

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import by.kazarovets.geometrypuzzle.*
import by.kazarovets.geometrypuzzle.logic.GameLogic
import by.kazarovets.geometrypuzzle.rendering.shader.*
import by.kazarovets.geometrypuzzle.rendering.shapes.Line
import by.kazarovets.geometrypuzzle.rendering.shapes.Point3D
import by.kazarovets.geometrypuzzle.utils.createProgram
import by.kazarovets.geometrypuzzle.utils.createShader

class OpenGLRenderer(private val context: Context) : Renderer {

    private var vertexData: FloatBuffer? = null

    private val axis = mutableListOf<Line>()
    private val lines = mutableListOf<Line>()
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
    private var horizontalDirection = Direction(Axis.Ox)
    private var verticalDirection = Direction(Axis.Oy)
    private var cameraDirection = Direction(Axis.Oz)

    private val gameLogic = GameLogic()

    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        glClearColor(0f, 0f, 0f, 1f)
        glEnable(GL_DEPTH_TEST)
        val vertexShaderId = createShader(GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShaderId = createShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        programId = createProgram(vertexShaderId, fragmentShaderId)
        glUseProgram(programId)
        pointCamera()
        prepareData()
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
            //start point
            s, 0f, 0f,

            //end point
            -s, 2 * s, 0f
        )

        vertexData = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertices)
                position(0)
            }

        axis.addAll(
            listOf(
                Line(0f, 0f, 0f, l, 0f, 0f).apply { color = Triple(0.3f, 0f, 0f) },
                Line(0f, 0f, 0f, 0f, l, 0f).apply { color = Triple(0f, 0.3f, 0f) },
                Line(0f, 0f, 0f, 0f, 0f, l).apply { color = Triple(0f, 0f, 0.3f) }
             )
        )
        axis.forEach {
            it.lineWidth = 1f
            it.allocateData()
        }

        lines.addAll(
            listOf(
                Line(s, 0f, 0f, s, s, 0f),
                Line(-s, s, 0f, -s, 2 * s, 0f).apply { color = Triple(0f, 0.9f, 0.5f) },
                Line(0f, 0f, 0f, 0f, s, 2 * s).apply { color = Triple(0.4f, 0.9f, 0.9f) }
            )
        )

        lines.forEach {
            it.allocateData()
        }


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
        rotateVector = when (swipeDirection) {
            SwipeDirection.RIGHT -> cameraPoint.upVector
            SwipeDirection.LEFT -> cameraPoint.upVector.invert()
            SwipeDirection.UP -> cameraPoint.upPerpendicularVector.invert()
            SwipeDirection.BOTTOM -> cameraPoint.upPerpendicularVector
        }
        val prevHorizontal = horizontalDirection
        val prevVertical = verticalDirection
        val prevCamera = cameraDirection

        horizontalDirection = when (swipeDirection) {
            SwipeDirection.RIGHT -> prevCamera
            SwipeDirection.LEFT -> prevCamera.invert()
            SwipeDirection.UP -> prevHorizontal
            SwipeDirection.BOTTOM -> prevHorizontal
        }

        verticalDirection = when (swipeDirection) {
            SwipeDirection.RIGHT -> prevVertical
            SwipeDirection.LEFT -> prevVertical
            SwipeDirection.UP -> prevCamera
            SwipeDirection.BOTTOM -> prevCamera.invert()
        }

        cameraDirection = when (swipeDirection) {
            SwipeDirection.RIGHT -> prevHorizontal.invert()
            SwipeDirection.LEFT -> prevHorizontal
            SwipeDirection.UP -> prevVertical.invert()
            SwipeDirection.BOTTOM -> prevVertical
        }

        Log.d(
            "POINTS", " \n swipe = $swipeDirection \n camera = ${cameraDirection.axis} ${cameraDirection.inversed}"
                    + "\n horizontal = ${horizontalDirection.axis} ${horizontalDirection.inversed}" +
                    " \n vertical = ${verticalDirection.axis} ${verticalDirection.inversed}"
        )
        gameLogic.getPointsForMovement(
            Point3D(0f, 0f, 0f),
            lines,
            horizontalDirection,
            verticalDirection
        )

        ValueAnimator.ofFloat(0f, 90f).apply {
            duration = 500
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                this@OpenGLRenderer.rotateAngle = this.animatedValue as Float
                pointCamera()
            }
            doOnEnd {
                rotateAngle = 0f
                historyRotateMatrix = currRotateMatrix.clone()
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

        Matrix.setRotateM(
            deltaRotateMatrix, 0, rotateAngle,
            rotateVector.x, rotateVector.y, rotateVector.z
        )
        Matrix.multiplyMM(currRotateMatrix, 0, deltaRotateMatrix, 0, historyRotateMatrix, 0)
    }

    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, currRotateMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
    }

    override fun onDrawFrame(arg0: GL10) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        bindMatrix()


        axis.forEach { it.draw(mMatrix) }

        lines.forEach {
            it.draw(mMatrix)
        }


        glUseProgram(programId)

        uMatrixLocation = glGetMatrixLocation(programId)

        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)

        aPositionLocation = glGetPositionLocation(programId)
        glEnableVertexAttribArray(aPositionLocation)

        glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT, GL_FLOAT,
            false, 0, vertexData
        )


        uColorLocation = glGetColorLocation(programId)

        glLineWidth(5f)

        glUniform4f(uColorLocation, 0f, 1.0f, 0f, 1.0f)
        glDrawArrays(GL_POINTS, 0, 1)

        glUniform4f(uColorLocation, 1.0f, 0f, 0f, 1.0f)
        glDrawArrays(GL_POINTS, 1, 1)


        glDisableVertexAttribArray(programId)
    }

    companion object {

        const val POSITION_COUNT = 3
    }

}