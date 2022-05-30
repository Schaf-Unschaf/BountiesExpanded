package de.schafunschaf.bountiesexpanded.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

object MathUtils {

    fun getAverage(minValue: Float, maxValue: Float): Float {
        return (maxValue - minValue) / 2 + minValue
    }

    fun roundWholeNumber(number: Int, numPlaces: Int): Int {
        val pow = 10.0.pow(numPlaces.toDouble())
        return ((number / pow).roundToInt() * pow).toInt()
    }

    fun roundWholeNumber(number: Float, numPlaces: Int): Int {
        val pow = 10.0.pow(numPlaces.toDouble())
        return ((number / pow).roundToInt() * pow).toInt()
    }

    fun roundWholeNumber(number: Double, numPlaces: Int): Int {
        val pow = 10.0.pow(numPlaces.toDouble())
        return ((number / pow).roundToInt() * pow).toInt()
    }

    fun roundDecimals(number: Long, numDecimals: Int): Float {
        val bigDecimal = BigDecimal(number)
        return bigDecimal.setScale(numDecimals, RoundingMode.HALF_UP).toFloat()
    }

    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceAtLeast(min).coerceAtMost(max)
    }

    fun rollSuccessful(chance: Int, random: Random?): Boolean {
        var rand = random
        if (rand == null) {
            rand = Random()
        }
        return rand.nextInt(100) < chance
    }
}