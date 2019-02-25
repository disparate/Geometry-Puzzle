package by.kazarovets.geometrypuzzle.logic

import android.animation.ValueAnimator
import android.opengl.Matrix
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import by.kazarovets.geometrypuzzle.*

class CameraLogic(val onRotate: () -> Unit) {

    private var rotateAngle = 0f
    private var rotateVector = Vector(1f, 0f, 0f)
    public var horizontalDirection = Direction(Axis.Ox)
    public var verticalDirection = Direction(Axis.Oy)
    public var cameraDirection = Direction(Axis.Oz)


    private var historyRotateMatrix = FloatArray(16)
    val currRotateMatrix = FloatArray(16)
    private val deltaRotateMatrix = FloatArray(16)


    public val cameraPoint = CameraPoint(3f)

    fun initMatrices() {
        Matrix.setRotateM(currRotateMatrix, 0, 0f, 0f, 0f, 1f)
        Matrix.setRotateM(historyRotateMatrix, 0, 0f, 0f, 0f, 1f)
    }

    fun changeDirections(swipeDirection: SwipeDirection) {
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
    }


    fun swipeCameraAnimator(swipeDirection: SwipeDirection): ValueAnimator {
        rotateVector = when (swipeDirection) {
            SwipeDirection.RIGHT -> cameraPoint.upVector
            SwipeDirection.LEFT -> cameraPoint.upVector.invert()
            SwipeDirection.UP -> cameraPoint.upPerpendicularVector.invert()
            SwipeDirection.BOTTOM -> cameraPoint.upPerpendicularVector
        }

        return ValueAnimator.ofFloat(0f, 90f).apply {
            duration = 500
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                this@CameraLogic.rotateAngle = this.animatedValue as Float
                onRotate()
            }
            doOnEnd {
                rotateAngle = 0f
                historyRotateMatrix = currRotateMatrix.clone()
            }
        }
    }


    fun calculateCurrRotateMatrix() {

        Matrix.setRotateM(
            deltaRotateMatrix, 0, rotateAngle,
            rotateVector.x, rotateVector.y, rotateVector.z
        )
        Matrix.multiplyMM(currRotateMatrix, 0, deltaRotateMatrix, 0, historyRotateMatrix, 0)
    }
}