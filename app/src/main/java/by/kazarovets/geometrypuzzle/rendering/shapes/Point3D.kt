package by.kazarovets.geometrypuzzle.rendering.shapes

import android.animation.TypeEvaluator
import by.kazarovets.geometrypuzzle.Axis
import kotlin.math.absoluteValue

data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun distanceToCover(from: Point3D, horizontalOrientation: Axis): Float {
        return when (horizontalOrientation) {
            Axis.Ox -> (from.x - x).absoluteValue
            Axis.Oy -> (from.y - y).absoluteValue
            Axis.Oz -> (from.z - z).absoluteValue
        }
    }
}

class Point3DTypeEvaluator : TypeEvaluator<Point3D> {
    override fun evaluate(fraction: Float, startValue: Point3D?, endValue: Point3D?): Point3D {
        if (startValue == null || endValue == null) {
            return startValue ?: Point3D(0f, 0f, 0f)
        }
        return Point3D(
            startValue.x + (endValue.x - startValue.x) * fraction,
            startValue.y + (endValue.y - startValue.y) * fraction,
            startValue.z + (endValue.z - startValue.z) * fraction
        )
    }

}