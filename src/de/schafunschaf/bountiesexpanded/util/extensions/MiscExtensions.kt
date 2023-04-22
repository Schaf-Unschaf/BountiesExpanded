package de.schafunschaf.bountiesexpanded.util.extensions

import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.atan2

fun Vector2f.getAngleDegree(): Double {
    return getAngle() * 180 / PI
}

fun Vector2f.getAngle(): Float {
    return atan2(y, x)
}