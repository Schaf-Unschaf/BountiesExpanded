package de.schafunschaf.bountiesexpanded.util.ui.lwjgl

import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

object TextUtil {

    @Throws(FontException::class)
    fun drawString(posX: Float, posY: Float, string: String?, font: String?, size: Float, color: Color?) {
        val lazyFont = LazyFont.loadFont(font!!)
        val testString: LazyFont.DrawableString = lazyFont.createText(string!!, color!!, size)
        testString.draw(posX, posY)
    }
}