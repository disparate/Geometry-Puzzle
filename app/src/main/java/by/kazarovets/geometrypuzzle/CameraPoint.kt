package by.kazarovets.geometrypuzzle

import android.animation.FloatEvaluator
import android.animation.TypeEvaluator
import kotlin.reflect.KProperty1

class CameraPoint constructor(val distance: Float = 0f) {
    val fromX: Float = 0f
    val fromY: Float = 0f
    val fromZ: Float = distance
    val toX: Float = 0f
    val toY: Float = 0f
    val toZ: Float = 0f
    val upX: Float = 0f
    val upY: Float = distance
    val upZ: Float = 0f



    val upVector = Vector(upX, upY, upZ)
    val fromVector = Vector(toX - fromX , toY - fromY, toZ - fromZ)
    val upPerpendicularVector = Vector(distance, 0f, 0f)
}

data class Vector(val x: Float, val y: Float, val z: Float) {

    fun invert() = Vector(-x, -y, -z)
}

enum class SwipeDirection {
    RIGHT, LEFT, UP, BOTTOM
}

class CameraPointEvaluator : TypeEvaluator<CameraPoint> {
    override fun evaluate(fraction: Float, startValue: CameraPoint?, endValue: CameraPoint?): CameraPoint {
        if (startValue == null || endValue == null) return startValue ?: CameraPoint()

        val floatEvaluator = FloatEvaluator()
        fun evaluate(field: KProperty1<CameraPoint, Float>): Float {
            return floatEvaluator.evaluate(fraction, field.get(startValue), field.get(endValue))
        }


        return CameraPoint(
            evaluate(CameraPoint::fromX)
        )
    }

}
