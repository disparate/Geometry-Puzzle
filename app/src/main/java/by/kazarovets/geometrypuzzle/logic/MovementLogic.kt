package by.kazarovets.geometrypuzzle.logic

import android.util.Log
import by.kazarovets.geometrypuzzle.Axis
import by.kazarovets.geometrypuzzle.Direction
import by.kazarovets.geometrypuzzle.rendering.shapes.Line
import by.kazarovets.geometrypuzzle.rendering.shapes.Point3D

class MovementLogic {


    fun getPointsForMovement(
        from: Point3D, lines: List<Line>,
        horizontalDirection: Direction,
        verticalDirection: Direction
    ): PointsToMove {
        val fixedHorizontal = when (verticalDirection.axis) {
            Axis.Ox -> from.x
            Axis.Oy -> from.y
            Axis.Oz -> from.z
        }

        val changingHorizontal = when (horizontalDirection.axis) {
            Axis.Ox -> from.x
            Axis.Oy -> from.y
            Axis.Oz -> from.z
        }

        val horizontalLines = lines.filter { it.isHorizontal(verticalDirection) }
            .filter { it.start(verticalDirection).equalTo(fixedHorizontal) }

        val left = findLeftPoint(changingHorizontal, horizontalLines, horizontalDirection)
        val right = findRightPoint(changingHorizontal, horizontalLines, horizontalDirection)

        return PointsToMove(
            if (horizontalDirection.inversed) right else left,
            if (horizontalDirection.inversed) left else right
        ).also {
            Log.d(
                "Movement logic", "can move from = ${from}, left = ${it.left}, right = ${it.right} "
            )
        }
    }

    private fun findLeftPoint(point: Float, lines: List<Line>, horizontalDirection: Direction): Point3D? {
        return findSidePoint(point, lines, horizontalDirection, true)
    }

    private fun findRightPoint(point: Float, lines: List<Line>, horizontalDirection: Direction): Point3D? {
        return findSidePoint(point, lines, horizontalDirection, false)
    }

    private fun findSidePoint(
        point: Float,
        lines: List<Line>,
        horizontalDirection: Direction,
        isLeft: Boolean
    ): Point3D? {
        val startSide = if (isLeft) Line::start else Line::end
        val endSide = if (isLeft) Line::end else Line::start

        val sorted = lines.sortedBy { startSide(it, horizontalDirection) }

        var lineContaining: Line? = null
        for (line in sorted) {
            if (lineContaining != null) {
                if (endSide(lineContaining, horizontalDirection) < startSide(line, horizontalDirection)) {
                    lineContaining = line // connected with line containing dot
                }
            } else if (line.contains(point, horizontalDirection)) {
                lineContaining = line
            }
        }
        return lineContaining?.let {
            if (isLeft) {
                Point3D(it.xStart, it.yStart, it.zStart)
            } else {
                Point3D(it.xEnd, it.yEnd, it.zEnd)
            }
        }
    }



    fun Line.contains(point: Float, horizontalDirection: Direction): Boolean {
        return start(horizontalDirection) <= point && point <= end(horizontalDirection)
    }

}


fun Float.equalTo(another: Float): Boolean {
    return Math.abs(this - another) < 0.0001f
}


fun Line.isHorizontal(verticalDirection: Direction): Boolean {
    return start(verticalDirection).equalTo(end(verticalDirection))
}


fun Line.start(horizontalDirection: Direction): Float {
    return when (horizontalDirection.axis) {
        Axis.Ox -> xStart
        Axis.Oy -> yStart
        Axis.Oz -> zStart
    }
}

fun Line.end(horizontalDirection: Direction): Float {
    return when (horizontalDirection.axis) {
        Axis.Ox -> xEnd
        Axis.Oy -> yEnd
        Axis.Oz -> zEnd
    }
}