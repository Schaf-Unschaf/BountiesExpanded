package de.schafunschaf.bountiesexpanded.util.ui

import com.fs.starfarer.E.F
import com.fs.starfarer.settings.StarfarerSettings
import java.awt.Color

fun renderScanlinesWithCorners(
    blX: Float,
    blY: Float,
    tlX: Float,
    tlY: Float,
    trX: Float,
    trY: Float,
    brX: Float,
    brY: Float,
    alphaMult: Float,
    additive: Boolean
) {
    val graphic = F(StarfarerSettings.Object("ui", "scanline11"), 10.0f, 10.0f)
    val color1 = Color(30, 114, 132, 100)
    val color2 = Color(10, 38, 44, 100)
    graphic.Ò00000(additive)
    graphic.o00000(color1, color2)
    graphic.o00000(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult)
}