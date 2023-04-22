package de.schafunschaf.bountiesexpanded.util.ui.plugins

import com.fs.starfarer.api.graphics.SpriteAPI
import de.schafunschaf.bountiesexpanded.util.ui.lwjgl.ShapeUtil
import de.schafunschaf.bountiesexpanded.util.ui.renderScanlinesWithCorners
import java.awt.Color

class SpriteRenderPlugin(var sprite: SpriteAPI, var width: Float, var height: Float) : BasePanelPlugin() {

    private var withScanlines = false
    private var withBorder = false
    private var borderSize = 0f
    private var borderColor: Color? = null

    override fun render(alphaMult: Float) {
        if (p != null) {
            val position = p!!
            val blX = position.x
            val blY = position.y
            val tlX = position.x
            val tlY = position.y + height
            val trX = position.x + width
            val trY = position.y + height
            val brX = position.x + width
            val brY = position.y
            sprite.setSize(width, height)
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY)

            if (withScanlines)
                renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, 2f, false)

            if (withBorder)
                ShapeUtil.drawBorder(blX, blY, width, height, borderSize, borderColor!!)
        }
    }

    fun withScanLines() {
        withScanlines = true
    }

    fun withBorder(size: Float, color: Color) {
        withBorder = true
        borderSize = size
        borderColor = color
    }
}