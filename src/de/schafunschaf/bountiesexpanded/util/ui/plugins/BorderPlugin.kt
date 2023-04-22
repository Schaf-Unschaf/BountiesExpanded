package de.schafunschaf.bountiesexpanded.util.ui.plugins

import de.schafunschaf.bountiesexpanded.util.ui.lwjgl.ShapeUtil
import java.awt.Color

open class BorderPlugin(private val borderSize: Float, private var color: Color) : BasePanelPlugin() {

    override fun render(alphaMult: Float) {
        if (p != null) {
            val position = p!!
            ShapeUtil.drawBorder(position.x, position.y, position.width, position.height, borderSize, color)
        }
    }
}