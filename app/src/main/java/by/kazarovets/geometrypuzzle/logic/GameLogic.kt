package by.kazarovets.geometrypuzzle.logic

import by.kazarovets.geometrypuzzle.Direction
import by.kazarovets.geometrypuzzle.rendering.shapes.Line
import by.kazarovets.geometrypuzzle.rendering.shapes.Point3D

class GameLogic {

    private val movementLogic = MovementLogic()

    fun getPointsForMovement(
        from: Point3D, lines: List<Line>,
        horizontalDirection: Direction,
        verticalDirection: Direction
    ): PointsToMove {
        return movementLogic.getPointsForMovement(from, lines, horizontalDirection, verticalDirection)
    }


}

class PointsToMove(
    val left: Point3D?,
    val right: Point3D?
)