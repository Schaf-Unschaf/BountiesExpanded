package de.schafunschaf.bountiesexpanded.util.ui.plugins

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI

open class BasePanelPlugin : CustomUIPanelPlugin {

    var p: PositionAPI? = null
    override fun positionChanged(position: PositionAPI) {
        p = position
    }

    override fun renderBelow(alphaMult: Float) {}
    override fun render(alphaMult: Float) {}
    override fun advance(amount: Float) {}
    override fun processInput(events: List<InputEventAPI>) {}
}