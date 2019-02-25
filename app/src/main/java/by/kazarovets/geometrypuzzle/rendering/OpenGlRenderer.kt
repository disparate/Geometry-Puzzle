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
import by.kazarovets.geometrypuzzle.logic.CameraLogic
import by.kazarovets.geometrypuzzle.logic.GameLogic
import by.kazarovets.geometrypuzzle.logic.PointsToMove
import by.kazarovets.geometrypuzzle.rendering.models.Player
import by.kazarovets.geometrypuzzle.rendering.shader.*
import by.kazarovets.geometrypuzzle.rendering.shapes.Dot
import by.kazarovets.geometrypuzzle.rendering.shapes.Line
import by.kazarovets.geometrypuzzle.rendering.shapes.Point3D
import by.kazarovets.geometrypuzzle.rendering.shapes.Point3DTypeEvaluator
import by.kazarovets.geometrypuzzle.utils.createProgram
import by.kazarovets.geometrypuzzle.utils.createShader

class OpenGLRenderer : Renderer {

    private val axis = mutableListOf<Line>()
    private val lines = mutableListOf<Line>()
    private lateinit var player: Player
    private lateinit var finishDot: Dot

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)


    private var finishPoint = Point3D(0f, 0f, 0f)

    private var pointsToMove: PointsToMove = PointsToMove(null, null)

    private val gameLogic = GameLogic()

    private val cameraLogic = CameraLogic { pointCamera() }


    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        glClearColor(0f, 0f, 0f, 1f)
        glEnable(GL_DEPTH_TEST)

        pointCamera()
        prepareData()
    }

    override fun onSurfaceChanged(arg0: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        createProjectionMatrix(width, height)

        cameraLogic.initMatrices()

        bindMatrix()
    }

    private fun prepareData() {

        val s = 1f
        val l = 10f

        axis.addAll(
            listOf(
                Line(0f, 0f, 0f, l, 0f, 0f).apply { color = Triple(0.3f, 0f, 0f) },
                Line(0f, 0f, 0f, 0f, l, 0f).apply { color = Triple(0f, 0.3f, 0f) },
                Line(0f, 0f, 0f, 0f, 0f, l).apply { color = Triple(0f, 0f, 0.3f) }
            )
        )
        axis.forEach {
            it.lineWidth = 1f
        }

        lines.addAll(
            listOf(
                Line(s, 0f, 0f, s, s, 0f),
                Line(-s, s, 0f, -s, 2 * s, 0f).apply { color = Triple(0f, 0.9f, 0.5f) },
                Line(0f, 0f, 0f, 0f, s, 2 * s).apply { color = Triple(0.4f, 0.9f, 0.9f) }
            )
        )



        finishPoint = Point3D(-s, 2 * s, 0f)
        finishDot = Dot(finishPoint).apply {
            color = Triple(1f, 0.2f, 0.2f)
        }

        val startPoint = Point3D(s, 0f, 0f)
        player = Player(0.2f, startPoint)

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
        cameraLogic.changeDirections(swipeDirection)

        pointsToMove = gameLogic.getPointsForMovement(
            player.position,
            lines,
            cameraLogic.horizontalDirection,
            cameraLogic.verticalDirection
        )

        cameraLogic.swipeCameraAnimator(swipeDirection).start()
    }


    private fun pointCamera() {
        cameraLogic.cameraPoint.apply {
            Matrix.setLookAtM(
                mViewMatrix, 0,
                fromX, fromY, fromZ,
                toX, toY, toZ,
                upX, upY, upZ
            )
        }
        cameraLogic.calculateCurrRotateMatrix()
    }

    fun tryMoveLeft() {
        tryMoveTo(pointsToMove.left, cameraLogic.horizontalDirection.axis)
    }

    fun tryMoveRight() {
        tryMoveTo(pointsToMove.right, cameraLogic.horizontalDirection.axis)
    }

    private fun tryMoveTo(point: Point3D?, horizontalAxis: Axis) {
        val distance = point?.distanceToCover(player.position, horizontalAxis)
        if (distance != null && distance > 0.0001f) {
            movePlayerTo(distance, point)
        }
    }

    private fun movePlayerTo(distance: Float, point3D: Point3D) {
        val time = distance * 300
        val initPosition = player.position
        ValueAnimator.ofObject(Point3DTypeEvaluator(), initPosition, point3D).apply {
            duration = time.toLong()
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                player.position = it.animatedValue as Point3D
            }
            start()
        }
    }

    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, cameraLogic.currRotateMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
    }

    override fun onDrawFrame(arg0: GL10) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        bindMatrix()

        axis.forEach { it.draw(mMatrix) }

        lines.forEach {
            it.draw(mMatrix)
        }

        player.draw(mMatrix)

        finishDot.draw(mMatrix)
    }

}