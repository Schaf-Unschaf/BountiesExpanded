package de.schafunschaf.bountiesexpanded.util.ui.plugins

import de.schafunschaf.bountiesexpanded.util.ui.lwjgl.ShapeUtil
import java.awt.Color

open class BorderBackgroundPlugin(
    private val borderSize: Float,
    private var borderColor: Color,
    private var bgColor: Color
) : BasePanelPlugin() {

    override fun render(alphaMult: Float) {
        if (p != null) {
            val position = p!!
            ShapeUtil.drawBorder(position.x, position.y, position.width, position.height, borderSize, borderColor)
        }
    }

    override fun renderBelow(alphaMult: Float) {
        if (p != null) {
            val position = p!!
            ShapeUtil.drawBox(position.x, position.y, position.width, position.height, bgColor)
        }
    }
}