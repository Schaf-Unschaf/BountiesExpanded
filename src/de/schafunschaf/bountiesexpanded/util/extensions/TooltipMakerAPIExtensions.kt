package de.schafunschaf.bountiesexpanded.util.extensions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import de.schafunschaf.bountiesexpanded.util.ui.plugins.SpriteRenderPlugin
import java.awt.Color

fun TooltipMakerAPI.addSprite(
    sprite: SpriteAPI,
    width: Float,
    height: Float,
    withScanlines: Boolean,
    withBorder: Boolean,
    borderSize: Float,
    borderColor: Color?,
    padding: Float
) {
    val plugin = SpriteRenderPlugin(sprite, width, height)
    if (withBorder) plugin.withBorder(borderSize, borderColor!!)
    if (withScanlines) plugin.withScanLines()
    val panel = Global.getSettings().createCustom(width, height, plugin)
    this.addCustom(panel, padding)
}

fun TooltipMakerAPI.addSeparator(width: Float, height: Float, color: Color, padding: Float): PositionAPI {
    return this.addSectionHeading("", Misc.getTextColor(), color, Alignment.MID, padding)
        .position.setSize(width, height)
}
