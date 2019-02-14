package by.kazarovets.geometrypuzzle

import android.animation.FloatEvaluator
import android.animation.TypeEvaluator
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

data class CameraPoint(
    val fromX: Float = 0f,
    val fromY: Float = 0f,
    val fromZ: Float = 0f,
    val toX: Float = 0f,
    val toY: Float = 0f,
    val toZ: Float = 0f,
    val upX: Float = 0f,
    val upY: Float = 0f,
    val upZ: Float = 0f
) {

    companion object {

        fun oxy(fromZ: Float) = CameraPoint(fromZ = fromZ, upY = 1f)
        fun ozx(fromY: Float) = CameraPoint(fromY = fromY, upX = 1f)
        fun oyz(fromX: Float) = CameraPoint(fromX = fromX, upZ = 1f)
    }
}

class CameraPointEvaluator : TypeEvaluator<CameraPoint> {
    override fun evaluate(fraction: Float, startValue: CameraPoint?, endValue: CameraPoint?): CameraPoint {
        if (startValue == null || endValue == null) return startValue ?: CameraPoint()

        val floatEvaluator = FloatEvaluator()
        fun evaluate(field: KProperty1<CameraPoint, Float>): Float {
            return floatEvaluator.evaluate(fraction, field.get(startValue), field.get(endValue))
        }


        return CameraPoint(
            evaluate(CameraPoint::fromX),
            evaluate(CameraPoint::fromY),
            evaluate(CameraPoint::fromZ),

            evaluate(CameraPoint::toX),
            evaluate(CameraPoint::toY),
            evaluate(CameraPoint::toZ),

            evaluate(CameraPoint::upX),
            evaluate(CameraPoint::upY),
            evaluate(CameraPoint::upZ)
        )
    }

}
