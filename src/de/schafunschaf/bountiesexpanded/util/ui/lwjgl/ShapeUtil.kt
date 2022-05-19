package de.schafunschaf.bountiesexpanded.util.ui.lwjgl

import org.lwjgl.opengl.GL11
import java.awt.Color

object ShapeUtil {

    fun drawBox(posX: Float, posY: Float, width: Float, height: Float, color: Color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4ub(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte())
        GL11.glBegin(GL11.GL_QUADS)
        run {
            GL11.glVertex2f(posX, posY) // BL
            GL11.glVertex2f(posX, posY + height) // TL
            GL11.glVertex2f(posX + width, posY + height) // TR
            GL11.glVertex2f(posX + width, posY) // BR
        }
        GL11.glEnd()
    }

    fun drawBorderedBox(posX: Float, posY: Float, width: Float, height: Float, borderSize: Float, borderMargin: Float,
                        color: Color) {
        drawBox(posX, posY, width, height, color)
        drawBox(posX + borderSize, posY + borderSize, width - borderSize * 2, height - borderSize * 2, color.darker().darker())
        drawBox(posX + borderMargin * 2, posY + borderMargin * 2, width - borderMargin * 4, height - borderMargin * 4, color)
    }

    fun drawBorder(posX: Float, posY: Float, width: Float, height: Float, borderSize: Float, color: Color) {
        // BL to TL border
        drawBox(posX, posY, borderSize, height, color)
        // TL to TR border
        drawBox(posX + borderSize, posY + height - borderSize, width - borderSize * 2, borderSize, color)
        // TR to BR border
        drawBox(posX + width - borderSize, posY, borderSize, height, color)
        // BL to BR border
        drawBox(posX + borderSize, posY, width - borderSize * 2, borderSize, color)
    }
}