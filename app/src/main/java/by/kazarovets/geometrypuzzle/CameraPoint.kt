package by.kazarovets.geometrypuzzle

import android.animation.FloatEvaluator
import android.animation.TypeEvaluator
import kotlin.math.absoluteValue
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
    val fromVector = Vector(toX - fromX, toY - fromY, toZ - fromZ)
    val upPerpendicularVector = Vector(distance, 0f, 0f)
}

data class Vector(val x: Float, val y: Float, val z: Float) {

    val eps = 0.0001f
    fun invert() = Vector(-x, -y, -z)

}

enum class SwipeDirection {
    RIGHT, LEFT, UP, BOTTOM
}

data class Direction(val axis: Axis,
                val inversed: Boolean = false) {
    fun invert(): Direction {
       return this.copy(inversed = this.inversed.not())
    }
}

enum class Axis {
    Ox, Oy, Oz
}
